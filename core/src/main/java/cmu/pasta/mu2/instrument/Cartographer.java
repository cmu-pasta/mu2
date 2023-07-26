package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.mutators.Mutator;
import janala.instrument.SafeClassWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@link ClassVisitor} which collects
 * data about what mutation opportunities are available, and which run during the initial
 * "exploration".
 */
public class Cartographer extends ClassVisitor {

  /**
   * The set of mutant opportunities
   */
  private Map<Mutator, List<MutationInstance>> opportunities;

  /**
   * The API Version
   */
  private static final int API = Opcodes.ASM8;

  /**
   * The name of the class we're visiting
   */
  private String className = null;

  /** The opt level to be used. */
  private final OptLevel optLevel;

  /**
   * Creates a Cartographer for a specific {@link ClassReader}, which allows for optimization
   *
   * @param classReader the class which will be instrumented
   * @param cl          the ClassLoader to use for reading comparison classes
   * @note {@code cl} should be able to find all the classes that may want to be instrumented. They
   * aren't loaded, they're just read.
   */
  public Cartographer(ClassReader classReader, ClassLoader cl, OptLevel optLevel) {
    super(API, new SafeClassWriter(classReader, cl,
        ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES));

    this.opportunities = new HashMap<>(Mutator.allMutators.size());
    for (Mutator mutator : Mutator.allMutators) {
      this.opportunities.put(mutator, new ArrayList<>());
    }

    this.optLevel = optLevel;
  }

  /**
   * Creates and runs a cartographer for a given class, using a specified classloader.
   *
   * @param classBytes the bytecode for the class to map
   * @return the cartographer which has walked the class
   */
  public static Cartographer explore(byte[] classBytes, CartographyClassLoader ccl) {
    ClassReader reader = new ClassReader(classBytes);
    Cartographer c = new Cartographer(reader, ccl, ccl.getOptLevel());
    reader.accept(c, 0);
    return c;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    // Change from the ASM's internal naming scheme to the package naming scheme
    this.className = name.replace("/", ".");
    super.visit(version, access, name, signature, superName, interfaces);
  }

  String fileName = "<unknown>";
  @Override
  public void visitSource(String source, String debug) {
    if(source != null) fileName = source;
    super.visitSource(source, debug);
  }

  /**
   * Gets the possible opportunities the mutators
   *
   * @return the opportunities for each mutator
   */
  public Map<Mutator, List<MutationInstance>> getOpportunities() {
    return opportunities;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
      String[] exceptions) {
    return new MethodVisitor(API, cv.visitMethod(access, name, descriptor, signature, exceptions)) {

      private void dup(Type operandType, int numArgs) {
        if (operandType.getSize() == 1) {
          if (numArgs == 1) {
            super.visitInsn(Opcodes.DUP);
          } else if (numArgs == 2) {
            super.visitInsn(Opcodes.DUP2);
          }
        } else if (operandType.getSize() == 2) {
          if (numArgs == 1) {
            super.visitInsn(Opcodes.DUP2);
          } else if (numArgs == 2) {
            throw new AssertionError("Cannot duplicate 2 arguments of size 2!");
          }
        }
      }

      /**
       * Inserts mutator object into the stack below numArgs operands. Cannot be used
       * for category 2 types with 2 operands.
       * @param mut Mutator object
       * @param numArgs Number of operands for mutator
       */
      private void insertMutatorObject(Mutator mut, int numArgs) {
        Class mutatorClass = mut.getClass();
        Type operandType = mut.getOperandType();
        super.visitLdcInsn(mut.hashCode());
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(mutatorClass),
                "getMutator",
                "(I)L"+Type.getInternalName(mutatorClass)+";",
                false);
        if (numArgs == 0) {
          return;
        }
        if (operandType.getSize() == 1) {
          if (numArgs == 1) {
            super.visitInsn(Opcodes.DUP_X1);
          } else if (numArgs == 2) {
            super.visitInsn(Opcodes.DUP_X2);
          }
        } else if (operandType.getSize() == 2 && numArgs == 1) {
          if (numArgs == 1) {
            super.visitInsn(Opcodes.DUP_X2);
          } else {
            throw new AssertionError("Cannot insert object below 2 arguments of size 2!");
          }
        }
        super.visitInsn(Opcodes.POP);
      }

      /**
       * Instruments infection logic to invoke and log mutator function output values.
       * Stack:
       *    ..., args ->
       *    ..., args, duplicatedArgs ->
       *    ..., args, mut, duplicatedArgs ->
       *    ..., args, outputValue ->
       *    ..., args, outputValue, id ->
       *    ..., args
       * @param mut The mutator to be logged
       * @param mutationId The id of the mutation instance
       * @param operandType The operand type of the mutator
       * @param numArgs The number of operands of the mutator
       * @param runMutated Whether to run mutated function for mutator
       */
      private void logInfectionValue(Mutator mut, int mutationId, Type operandType, int numArgs, boolean runMutated) {
        String funcName = "runOriginal";
        if (runMutated) {
          funcName = "runMutated";
        }
        dup(operandType, numArgs);
        insertMutatorObject(mut, numArgs);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(mut.getClass()),
                funcName,
                mut.getMethodDescriptor(),
                false);
        super.visitLdcInsn(mutationId);
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(MutationSnoop.class),
                "logValue",
                mut.getLogMethodDescriptor(),
                false);
      }

      /**
       * Logs that a mutator can be used at the current location in the tree.
       *
       * @param mut The mutator to be logged
       */
      private void logMutOp(Mutator mut) {
        List<MutationInstance> ops = opportunities.get(mut);
        MutationInstance mi = new MutationInstance(Cartographer.this.className, mut, ops.size(), lineNum, fileName);
        ops.add(mi);

        if (optLevel == OptLevel.EXECUTION || (optLevel == OptLevel.INFECTION && !mut.isUseInfection())) {
          super.visitLdcInsn(mi.id);
          super.visitMethodInsn(Opcodes.INVOKESTATIC,
              Type.getInternalName(MutationSnoop.class),
              "logMutant",
              "(I)V",
              false);
          return;
        }

        if (optLevel == OptLevel.INFECTION) {
          Class mutatorClass = mut.getClass();
          Type operandType = mut.getOperandType();
          int numArgs = mut.getNumArgs();

          // Special handling for double/long binary mutators due to JVM stack limitation. Reads the second operand of
          // the original instruction and stores it in the mutator object to invoke original and mutated functions.
          if (operandType.getSize() == 2 && numArgs == 2) {
            insertMutatorObject(mut, 1);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(mutatorClass),
                    "readSecondArg",
                    String.format("(%s)V", operandType.getDescriptor()),
                    false);
            logInfectionValue(mut, mi.id, operandType, 1, false);
            logInfectionValue(mut, mi.id, operandType, 1, true);
            insertMutatorObject(mut, 0);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(mutatorClass),
                    "writeSecondArg",
                    String.format("()%s", operandType.getDescriptor()),
                    false);
          } else {
            logInfectionValue(mut, mi.id, operandType, numArgs, false);
            logInfectionValue(mut, mi.id, operandType, numArgs, true);
          }
        }
      }

      /**
       * Checks if the opcode/descriptor could be the target of a mutation
       *
       * @param opcode     The opcode of the mutation to be performed
       * @param descriptor The descriptor of the method, if it has one
       */
      private void check(int opcode, String descriptor) {
          for (Mutator m : Mutator.allMutators) {
              if (m.isOpportunity(opcode, descriptor)) {
                  logMutOp(m);
              }
          }
      }

      /**
       * Checks if the opcode could be the target of a mutant
       *
       * @param opcode The opcode of the instruction
       */
      private void check(int opcode) {
        check(opcode, descriptor);
      }

      // TODO: Remove this duplication, somehow

      int lineNum = 0;
      @Override
      public void visitLineNumber(int line, Label start) {
        lineNum = line;
        super.visitLineNumber(line, start);
      }

      @Override
      public void visitJumpInsn(int opcode, Label label) {
        check(opcode);
        super.visitJumpInsn(opcode, label);
      }

      @Override
      public void visitLdcInsn(Object value) {
        check(Opcodes.LDC);
        super.visitLdcInsn(value);
      }

      @Override
      public void visitIincInsn(int var, int inc) {
        check(Opcodes.IINC);
        super.visitIincInsn(var, inc);
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
          boolean isInterface) {
        check(opcode, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      }

      @Override
      public void visitInsn(int opcode) {
        check(opcode);
        super.visitInsn(opcode);
      }
    };
  }

  ;

  byte[] toByteArray() {
    return ((ClassWriter) cv).toByteArray();
  }
}

package cmu.pasta.mu2.instrument;

import cmu.pasta.mu2.MutationInstance;
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
import org.objectweb.asm.commons.Method;

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

    this.opportunities = new HashMap<>(Mutator.values().length);
    for (Mutator mutator : Mutator.values()) {
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
        for (int i = 0; i < numArgs; i++) {
          if (operandType.getSize() == 2) {
            super.visitInsn(Opcodes.DUP2);
          } else {
            super.visitInsn(Opcodes.DUP);
          }
        }
      }

      // TODO: figure out more understandable logic
      private void insertTopBelow(Type operandType, int insertBelow) {
        switch (operandType.getSize() * insertBelow) {
          case 1:
            super.visitInsn(Opcodes.SWAP);
            return;
          case 2:
            super.visitInsn(Opcodes.DUP_X2);
            super.visitInsn(Opcodes.POP);
            return;
          case 4:
            // TODO: translate from existing dups
          default:
            return;
        }
      }

      private Type getBoxedType(final Type type) {
        switch (type.getSort()) {
          case Type.BYTE:
            return Type.getObjectType("java/lang/Byte");
          case Type.BOOLEAN:
            return Type.getObjectType("java/lang/Boolean");
          case Type.SHORT:
          return Type.getObjectType("java/lang/Short");
          case Type.CHAR:
            return Type.getObjectType("java/lang/Character");
          case Type.INT:
            return Type.getObjectType("java/lang/Integer");
          case Type.FLOAT:
            return Type.getObjectType("java/lang/Float");
          case Type.LONG:
            return Type.getObjectType("java/lang/Long");
          case Type.DOUBLE:
            return Type.getObjectType("java/lang/Double");
          default:
            return type;
        }
      }


      private void box(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
          return;
        }
        if (type == Type.VOID_TYPE) {
          super.visitLdcInsn((String) null);
        } else {
          Type boxedType = getBoxedType(type);
          super.visitTypeInsn(Opcodes.NEW, boxedType.getInternalName());
          if (type.getSize() == 2) {
            // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
            super.visitInsn(Opcodes.DUP_X2);
            super.visitInsn(Opcodes.DUP_X2);
            super.visitInsn(Opcodes.POP);
          } else {
            // p -> po -> opo -> oop -> o
            super.visitInsn(Opcodes.DUP_X1);
            super.visitInsn(Opcodes.SWAP);
          }
          String owner = boxedType.getSort() == Type.ARRAY ? boxedType.getDescriptor() : boxedType.getInternalName();
          Method constructorMethod = new Method("<init>", Type.VOID_TYPE, new Type[] {type});
          super.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, constructorMethod.getName(), constructorMethod.getDescriptor(), false);
        }
      }

      /**
       * Logs that a mutator can be used at the current location in the tree.
       *
       * @param mut The mutator to be logged
       */
      private void logMutOp(Mutator mut) {
        List<MutationInstance> ops = opportunities.get(mut);
        MutationInstance mi = new MutationInstance(Cartographer.this.className, mut, ops.size());
        ops.add(mi);

        if (optLevel == OptLevel.EXECUTION) {
          super.visitLdcInsn(mi.id);
          super.visitMethodInsn(Opcodes.INVOKESTATIC,
              Type.getInternalName(MutationSnoop.class),
              "logMutant",
              "(I)V",
              false);
        }

        if (optLevel == OptLevel.INFECTION) {
          if (mut.isInfectionImplemented()) {
            dup(mut.getOperandType(), mut.getNumArgs());
            dup(mut.getOperandType(), mut.getNumArgs());
            super.visitInsn(mut.toReplace());
            box(mut.getTypedReturnType());
            insertTopBelow(mut.getOperandType(), mut.getNumArgs());
            for (InstructionCall ic : mut.replaceWith()) {
              super.visitInsn(ic.getOpcode());
            }
            box(mut.getTypedReturnType());
            super.visitLdcInsn(mi.id);

            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(MutationSnoop.class),
                    "logInfectedMutant",
                    "(Ljava/lang/Object;Ljava/lang/Object;I)V",
                    false);
          } else {
            super.visitLdcInsn(mi.id);
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(MutationSnoop.class),
                    "logMutant",
                    "(I)V",
                    false);
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
          for (Mutator m : Mutator.values()) {
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

package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public abstract class Mutator {

    private final int toReplace;
    private final String instructionReturnType;
    private final List<InstructionCall> replaceWith;
    public static int cvArg = Opcodes.ASM8;
    public static List<Mutator> allMutators = new ArrayList<>();
    public static Map<Integer, Mutator> allMutatorsMap = new HashMap<>();

    Mutator(int toReplace, String instructionReturnType, InstructionCall... replaceWith) {
        this.toReplace = toReplace;
        this.instructionReturnType = instructionReturnType;
        this.replaceWith = new ArrayList<>();
        this.replaceWith.addAll(Arrays.asList(replaceWith));
        allMutators.add(this);

        allMutatorsMap.put(this.hashCode(), this);
    }

    public abstract Type getOperandType();
    public abstract Type getReturnType();
    public abstract int getNumArgs();
    public abstract String getMethodDescriptor();

    public String getLogMethodDescriptor() {
        return "(" + getReturnType().getDescriptor() + "I)V";
    }

    public int toReplace() {
        return toReplace;
    }

    public boolean isOpportunity(int opcode, String descriptor) {
        return opcode == toReplace
                && (instructionReturnType  == null || Type.getReturnType(descriptor).getDescriptor()
                .equals(instructionReturnType));
    }

    public List<InstructionCall> replaceWith() {
        return replaceWith;
    }

    public static void initializeMutators() {
        allMutators.clear();

        //Math Ops (MATH) (note logic is bitwise, not to be confused with bool ops)
        new IntBinaryOperatorMutator((int x, int y) -> x + y, (int x, int y) -> x - y, Opcodes.IADD, null, new InstructionCall(Opcodes.ISUB));
        new IntBinaryOperatorMutator((int x, int y) -> x - y, (int x, int y) -> x + y, Opcodes.ISUB, null, new InstructionCall(Opcodes.IADD));
        new IntBinaryOperatorMutator((int x, int y) -> x * y, (int x, int y) -> x / y, Opcodes.IMUL, null, new InstructionCall(Opcodes.IDIV));
        new IntBinaryOperatorMutator((int x, int y) -> x / y, (int x, int y) -> x * y, Opcodes.IDIV, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator((int x, int y) -> x % y, (int x, int y) -> x * y, Opcodes.IREM, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator((int x, int y) -> x | y, (int x, int y) -> x & y, Opcodes.IOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator((int x, int y) -> x & y, (int x, int y) -> x | y, Opcodes.IAND, null, new InstructionCall(Opcodes.IOR));
        new IntBinaryOperatorMutator((int x, int y) -> x ^ y, (int x, int y) -> x & y, Opcodes.IXOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator((int x, int y) -> x << y, (int x, int y) -> x >> y, Opcodes.ISHL, null, new InstructionCall(Opcodes.ISHR));
        new IntBinaryOperatorMutator((int x, int y) -> x >> y, (int x, int y) -> x << y, Opcodes.ISHR, null, new InstructionCall(Opcodes.ISHL));
        new IntBinaryOperatorMutator((int x, int y) -> x >>> y, (int x, int y) -> x << y, Opcodes.IUSHR, null, new InstructionCall(Opcodes.ISHL));

        new FloatBinaryOperatorMutator((float x, float y) -> x + y, (float x, float y) -> x - y, Opcodes.FADD, null, new InstructionCall(Opcodes.FSUB));
        new FloatBinaryOperatorMutator((float x, float y) -> x - y, (float x, float y) -> x + y, Opcodes.FSUB, null, new InstructionCall(Opcodes.FADD));
        new FloatBinaryOperatorMutator((float x, float y) -> x * y, (float x, float y) -> x / y, Opcodes.FMUL, null, new InstructionCall(Opcodes.FDIV));
        new FloatBinaryOperatorMutator((float x, float y) -> x / y, (float x, float y) -> x * y, Opcodes.FDIV, null, new InstructionCall(Opcodes.FMUL));
        new FloatBinaryOperatorMutator((float x, float y) -> x % y, (float x, float y) -> x * y, Opcodes.FREM, null, new InstructionCall(Opcodes.FMUL));
        // TODO: Add long and double

        //Removing Negations (INVERT_NEGS):
        new IntUnaryOperatorMutator((int x) -> -x, (int x) -> x, Opcodes.INEG, null, new InstructionCall(Opcodes.NOP));
        new DoubleUnaryOperatorMutator((double x) -> -x, (double x) -> x, Opcodes.DNEG, null, new InstructionCall(Opcodes.NOP));
        new FloatUnaryOperatorMutator((float x) -> -x, (float x) -> x, Opcodes.FNEG, null, new InstructionCall(Opcodes.NOP));
        new LongUnaryOperatorMutator((long x) -> -x, (long x) -> x, Opcodes.LNEG, null, new InstructionCall(Opcodes.NOP));

        //Conditional Ops (CONDITIONALS_BOUNDARY):
        new IntUnaryPredicateMutator((int x) -> x <= 0, (int x) -> x > 0, Opcodes.IFLE, null, new InstructionCall(Opcodes.IFGE, null));
        new IntUnaryPredicateMutator((int x) -> x > 0, (int x) -> x >= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFGE, null));
        new IntUnaryPredicateMutator((int x) -> x < 0, (int x) -> x <= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFLE, null));
        new IntBinaryPredicateMutator((int x, int y) -> x <= y, (int x, int y) -> x < y, Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPLT, null));
        new IntBinaryPredicateMutator((int x, int y) -> x >= y, (int x, int y) -> x > y, Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPGT, null));
        new IntBinaryPredicateMutator((int x, int y) -> x > y, (int x, int y) -> x >= y, Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPGE, null));
        new IntBinaryPredicateMutator((int x, int y) -> x < y, (int x, int y) -> x <= y, Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPLE, null));

        //Conditional Negation (NEGATE_CONDITIONALS):
        new IntUnaryPredicateMutator((int x) -> x == 0, (int x) -> x != 0, Opcodes.IFEQ, null, new InstructionCall(Opcodes.IFNE, null));
        new IntUnaryPredicateMutator((int x) -> x != 0, (int x) -> x > 0, Opcodes.IFNE, null, new InstructionCall(Opcodes.IFGT, null));
        new IntUnaryPredicateMutator((int x) -> x >= 0, (int x) -> x < 0, Opcodes.IFGE, null, new InstructionCall(Opcodes.IFLT, null));
        new IntUnaryPredicateMutator((int x) -> x > 0, (int x) -> x <= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFLE, null));
        new IntUnaryPredicateMutator((int x) -> x < 0, (int x) -> x >= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFGE, null));
        new IntBinaryPredicateMutator((int x, int y) -> x != y, (int x, int y) -> x == y, Opcodes.IF_ICMPNE, null, new InstructionCall(Opcodes.IF_ICMPEQ, null));
        new IntBinaryPredicateMutator((int x, int y) -> x == y, (int x, int y) -> x != y, Opcodes.IF_ICMPEQ, null, new InstructionCall(Opcodes.IF_ICMPNE, null));
        new IntBinaryPredicateMutator((int x, int y) -> x <= y, (int x, int y) -> x > y, Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPGT, null));
        new IntBinaryPredicateMutator((int x, int y) -> x >= y, (int x, int y) -> x < y, Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPLT, null));
        new IntBinaryPredicateMutator((int x, int y) -> x > y, (int x, int y) -> x <= y, Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPLE, null));
        new IntBinaryPredicateMutator((int x, int y) -> x < y, (int x, int y) -> x >= y, Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPGE, null));

        //Boolean Replace Return (FALSE_RETURNS and TRUE_RETURNS):
        new IntUnaryPredicateMutator((int x) -> x != 0, (int x) -> false, Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), new InstructionCall(Opcodes.IRETURN));
        new IntUnaryPredicateMutator((int x) -> x != 0, (int x) -> true, Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_1), new InstructionCall(Opcodes.IRETURN));

        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> false, Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
                        "valueOf", "(Z)Ljava/lang/Boolean;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> true, Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_1),
                new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
                        "valueOf", "(Z)Ljava/lang/Boolean;", false),
                new InstructionCall(Opcodes.ARETURN));

        //Return empty object (EMPTY_RETURNS)
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> 0, Opcodes.ARETURN, "Ljava/lang/Integer;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Integer.class.getName().replace('.', '/'),
                        "valueOf", "(I)Ljava/lang/Integer;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> (short) 0, Opcodes.ARETURN, "Ljava/lang/Short;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
                new InstructionCall(Opcodes.INVOKESTATIC, Short.class.getName().replace('.', '/'), "valueOf",
                        "(S)Ljava/lang/Short;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> (char) 0, Opcodes.ARETURN, "Ljava/lang/Character;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
                new InstructionCall(Opcodes.INVOKESTATIC, Character.class.getName().replace('.', '/'),
                        "valueOf", "(C)Ljava/lang/Character;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> (long) 0, Opcodes.ARETURN, "Ljava/lang/Long;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Long.class.getName().replace('.', '/'), "valueOf",
                        "(J)Ljava/lang/Long;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> (float) 0, Opcodes.ARETURN, "Ljava/lang/Float;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.FCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Float.class.getName().replace('.', '/'), "valueOf",
                        "(F)Ljava/lang/Float;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> (double) 0, Opcodes.ARETURN, "Ljava/lang/Double;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.DCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Double.class.getName().replace('.', '/'), "valueOf",
                        "(D)Ljava/lang/Double;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> "", Opcodes.ARETURN, "Ljava/lang/String;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LDC, ""),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> Collections.emptyList(), Opcodes.ARETURN, "Ljava/util/List;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptyList",
                        "()Ljava/util/List;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> Collections.emptySet(), Opcodes.ARETURN, "Ljava/util/Set;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptySet",
                        "()Ljava/util/Set;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator((Object x) -> x, (Object x) -> Optional.empty(), Opcodes.ARETURN, "Ljava/util/Optional;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Optional", "empty",
                        "()Ljava/util/Optional;", false),
                new InstructionCall(Opcodes.ARETURN));
    }

}

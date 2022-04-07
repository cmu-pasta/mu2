package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public abstract class Mutator {

    private final String name;
    private final boolean useInfection;
    public final int id;
    private final int toReplace;
    private final String instructionReturnType;
    private final List<InstructionCall> replaceWith;
    public static int cvArg = Opcodes.ASM8;
    public static List<Mutator> allMutators = new ArrayList<>();

    Mutator(String name, boolean useInfection, int toReplace, String instructionReturnType, InstructionCall... replaceWith) {
        this.name = name;
        this.useInfection = useInfection;
        this.toReplace = toReplace;
        this.instructionReturnType = instructionReturnType;
        this.replaceWith = new ArrayList<>();
        this.replaceWith.addAll(Arrays.asList(replaceWith));
        this.id = allMutators.size();
        allMutators.add(this);
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

    public boolean isUseInfection() {
        return useInfection;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static void initializeMutators() {
        allMutators.clear();

        //Math Ops (MATH) (note logic is bitwise, not to be confused with bool ops)
        new IntBinaryOperatorMutator("I_ADD_TO_SUB", true, (int x, int y) -> x + y, (int x, int y) -> x - y, Opcodes.IADD, null, new InstructionCall(Opcodes.ISUB));
        new IntBinaryOperatorMutator("I_SUB_TO_ADD", true, (int x, int y) -> x - y, (int x, int y) -> x + y, Opcodes.ISUB, null, new InstructionCall(Opcodes.IADD));
        new IntBinaryOperatorMutator("I_MUL_TO_DIV", true, (int x, int y) -> x * y, (int x, int y) -> x / y, Opcodes.IMUL, null, new InstructionCall(Opcodes.IDIV));
        new IntBinaryOperatorMutator("I_DIV_TO_MUL", true, (int x, int y) -> x / y, (int x, int y) -> x * y, Opcodes.IDIV, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator("I_REM_TO_MUL", true, (int x, int y) -> x % y, (int x, int y) -> x * y, Opcodes.IREM, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator("I_OR_TO_AND", true, (int x, int y) -> x | y, (int x, int y) -> x & y, Opcodes.IOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator("I_AND_TO_OR", true, (int x, int y) -> x & y, (int x, int y) -> x | y, Opcodes.IAND, null, new InstructionCall(Opcodes.IOR));
        new IntBinaryOperatorMutator("I_XOR_TO_AND", true, (int x, int y) -> x ^ y, (int x, int y) -> x & y, Opcodes.IXOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator("I_SHL_TO_SHR", true, (int x, int y) -> x << y, (int x, int y) -> x >> y, Opcodes.ISHL, null, new InstructionCall(Opcodes.ISHR));
        new IntBinaryOperatorMutator("I_SHR_TO_SHL", true, (int x, int y) -> x >> y, (int x, int y) -> x << y, Opcodes.ISHR, null, new InstructionCall(Opcodes.ISHL));
        new IntBinaryOperatorMutator("I_USHR_TO_SHL", true, (int x, int y) -> x >>> y, (int x, int y) -> x << y, Opcodes.IUSHR, null, new InstructionCall(Opcodes.ISHL));

        new FloatBinaryOperatorMutator("F_ADD_TO_SUB", true, (float x, float y) -> x + y, (float x, float y) -> x - y, Opcodes.FADD, null, new InstructionCall(Opcodes.FSUB));
        new FloatBinaryOperatorMutator("F_SUB_TO_ADD", true, (float x, float y) -> x - y, (float x, float y) -> x + y, Opcodes.FSUB, null, new InstructionCall(Opcodes.FADD));
        new FloatBinaryOperatorMutator("F_MUL_TO_DIV", true, (float x, float y) -> x * y, (float x, float y) -> x / y, Opcodes.FMUL, null, new InstructionCall(Opcodes.FDIV));
        new FloatBinaryOperatorMutator("F_DIV_TO_MUL", true, (float x, float y) -> x / y, (float x, float y) -> x * y, Opcodes.FDIV, null, new InstructionCall(Opcodes.FMUL));
        new FloatBinaryOperatorMutator("F_REM_TO_MUL", true, (float x, float y) -> x % y, (float x, float y) -> x * y, Opcodes.FREM, null, new InstructionCall(Opcodes.FMUL));
        // TODO: Add long and double
        new LongBinaryOperatorMutator("L_ADD_TO_SUB", true, (long x, long y) -> x + y, (long x, long y) -> x - y, Opcodes.LADD, null, new InstructionCall(Opcodes.LSUB));
        new LongBinaryOperatorMutator("L_SUB_TO_ADD", true, (long x, long y) -> x - y, (long x, long y) -> x + y, Opcodes.LSUB, null, new InstructionCall(Opcodes.LADD));
        new LongBinaryOperatorMutator("L_MUL_TO_DIV", true, (long x, long y) -> x * y, (long x, long y) -> x / y, Opcodes.LMUL, null, new InstructionCall(Opcodes.LDIV));
        new LongBinaryOperatorMutator("L_DIV_TO_MUL", true, (long x, long y) -> x / y, (long x, long y) -> x * y, Opcodes.LDIV, null, new InstructionCall(Opcodes.LMUL));
        new LongBinaryOperatorMutator("L_REM_TO_MUL", true, (long x, long y) -> x % y, (long x, long y) -> x * y, Opcodes.LREM, null, new InstructionCall(Opcodes.LMUL));
        new LongBinaryOperatorMutator("L_OR_TO_AND", true, (long x, long y) -> x | y, (long x, long y) -> x & y, Opcodes.LOR, null, new InstructionCall(Opcodes.LAND));
        new LongBinaryOperatorMutator("L_AND_TO_OR", true, (long x, long y) -> x & y, (long x, long y) -> x | y, Opcodes.LAND, null, new InstructionCall(Opcodes.LOR));
        new LongBinaryOperatorMutator("L_XOR_TO_AND", true, (long x, long y) -> x ^ y, (long x, long y) -> x & y, Opcodes.LXOR, null, new InstructionCall(Opcodes.LAND));
        new LongBinaryOperatorMutator("L_SHL_TO_SHR", true, (long x, long y) -> x << y, (long x, long y) -> x >> y, Opcodes.LSHL, null, new InstructionCall(Opcodes.LSHR));
        new LongBinaryOperatorMutator("L_SHR_TO_SHL", true, (long x, long y) -> x >> y, (long x, long y) -> x << y, Opcodes.LSHR, null, new InstructionCall(Opcodes.LSHL));
        new LongBinaryOperatorMutator("L_USHR_TO_SHL", true, (long x, long y) -> x >>> y, (long x, long y) -> x << y, Opcodes.LUSHR, null, new InstructionCall(Opcodes.LSHL));

        new DoubleBinaryOperatorMutator("D_ADD_TO_SUB", true, (double x, double y) -> x + y, (double x, double y) -> x - y, Opcodes.DADD, null, new InstructionCall(Opcodes.DSUB));
        new DoubleBinaryOperatorMutator("D_SUB_TO_ADD", true, (double x, double y) -> x - y, (double x, double y) -> x + y, Opcodes.DSUB, null, new InstructionCall(Opcodes.DADD));
        new DoubleBinaryOperatorMutator("D_MUL_TO_DIV", true, (double x, double y) -> x * y, (double x, double y) -> x / y, Opcodes.DMUL, null, new InstructionCall(Opcodes.DDIV));
        new DoubleBinaryOperatorMutator("D_DIV_TO_MUL", true, (double x, double y) -> x / y, (double x, double y) -> x * y, Opcodes.DDIV, null, new InstructionCall(Opcodes.DMUL));
        new DoubleBinaryOperatorMutator("D_REM_TO_MUL", true, (double x, double y) -> x % y, (double x, double y) -> x * y, Opcodes.DREM, null, new InstructionCall(Opcodes.DMUL));

        //Removing Negations (INVERT_NEGS):
        new IntUnaryOperatorMutator("I_NEG_TO_NOP", true, (int x) -> -x, (int x) -> x, Opcodes.INEG, null, new InstructionCall(Opcodes.NOP));
        new DoubleUnaryOperatorMutator("D_NEG_TO_NOP", true, (double x) -> -x, (double x) -> x, Opcodes.DNEG, null, new InstructionCall(Opcodes.NOP));
        new FloatUnaryOperatorMutator("F_NEG_TO_NOP", true, (float x) -> -x, (float x) -> x, Opcodes.FNEG, null, new InstructionCall(Opcodes.NOP));
        new LongUnaryOperatorMutator("L_NEG_TO_NOP", true, (long x) -> -x, (long x) -> x, Opcodes.LNEG, null, new InstructionCall(Opcodes.NOP));

        //Conditional Ops (CONDITIONALS_BOUNDARY):
        new IntUnaryPredicateMutator("IF_LE_TO_LT", true, (int x) -> x <= 0, (int x) -> x < 0, Opcodes.IFLE, null, new InstructionCall(Opcodes.IFLT, null));
        new IntUnaryPredicateMutator("IF_GE_TO_GT", true, (int x) -> x >= 0, (int x) -> x > 0, Opcodes.IFGE, null, new InstructionCall(Opcodes.IFGT, null));
        new IntUnaryPredicateMutator("IF_GT_TO_GE", true, (int x) -> x > 0, (int x) -> x >= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFGE, null));
        new IntUnaryPredicateMutator("IF_LT_TO_LE", true, (int x) -> x < 0, (int x) -> x <= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFLE, null));
        new IntBinaryPredicateMutator("IF_ICMP_LE_TO_LT", true, (int x, int y) -> x <= y, (int x, int y) -> x < y, Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPLT, null));
        new IntBinaryPredicateMutator("IF_ICMP_GE_TO_GT", true, (int x, int y) -> x >= y, (int x, int y) -> x > y, Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPGT, null));
        new IntBinaryPredicateMutator("IF_ICMP_GT_TO_GE", true, (int x, int y) -> x > y, (int x, int y) -> x >= y, Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPGE, null));
        new IntBinaryPredicateMutator("IF_ICMP_LT_TO_LE", true, (int x, int y) -> x < y, (int x, int y) -> x <= y, Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPLE, null));

        //Conditional Negation (NEGATE_CONDITIONALS):
        //TODO: Nonnull to null, null to nonnull
        new IntUnaryPredicateMutator("IF_EQ_TO_NE", false, (int x) -> x == 0, (int x) -> x != 0, Opcodes.IFEQ, null, new InstructionCall(Opcodes.IFNE, null));
        new IntUnaryPredicateMutator("IF_NE_TO_EQ", false, (int x) -> x != 0, (int x) -> x == 0, Opcodes.IFNE, null, new InstructionCall(Opcodes.IFEQ, null));
        new IntUnaryPredicateMutator("IF_LE_TO_GT", false, (int x) -> x <= 0, (int x) -> x > 0, Opcodes.IFLE, null, new InstructionCall(Opcodes.IFGT, null));
        new IntUnaryPredicateMutator("IF_GE_TO_LT", false, (int x) -> x >= 0, (int x) -> x < 0, Opcodes.IFGE, null, new InstructionCall(Opcodes.IFLT, null));
        new IntUnaryPredicateMutator("IF_GT_TO_LE", false, (int x) -> x > 0, (int x) -> x <= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFLE, null));
        new IntUnaryPredicateMutator("IF_LT_TO_GE", false, (int x) -> x < 0, (int x) -> x >= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFGE, null));
        new IntBinaryPredicateMutator("IF_ICMP_NE_TO_EQ", false, (int x, int y) -> x != y, (int x, int y) -> x == y, Opcodes.IF_ICMPNE, null, new InstructionCall(Opcodes.IF_ICMPEQ, null));
        new IntBinaryPredicateMutator("IF_ICMP_EQ_TO_NE", false, (int x, int y) -> x == y, (int x, int y) -> x != y, Opcodes.IF_ICMPEQ, null, new InstructionCall(Opcodes.IF_ICMPNE, null));
        new IntBinaryPredicateMutator("IF_ICMP_LE_TO_GT", false, (int x, int y) -> x <= y, (int x, int y) -> x > y, Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPGT, null));
        new IntBinaryPredicateMutator("IF_ICMP_GE_TO_LT", false, (int x, int y) -> x >= y, (int x, int y) -> x < y, Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPLT, null));
        new IntBinaryPredicateMutator("IF_ICMP_GT_TO_LE", false, (int x, int y) -> x > y, (int x, int y) -> x <= y, Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPLE, null));
        new IntBinaryPredicateMutator("IF_ICMP_LT_TO_GE", false, (int x, int y) -> x < y, (int x, int y) -> x >= y, Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPGE, null));
        new ObjectUnaryPredicateMutator("IF_NULL_TO_NON", false, (Object x) -> x == null, (Object x) -> x != null, Opcodes.IFNULL, null, new InstructionCall(Opcodes.IFNONNULL, null));
        new ObjectUnaryPredicateMutator("IF_NON_TO_NULL", false, (Object x) -> x != null, (Object x) -> x == null, Opcodes.IFNONNULL, null, new InstructionCall(Opcodes.IFNULL, null));

        //Boolean Replace Return (FALSE_RETURNS and TRUE_RETURNS):
        new IntUnaryPredicateMutator("IRETURN_TO_FALSE", true, (int x) -> x != 0, (int x) -> false, Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), new InstructionCall(Opcodes.IRETURN));
        new IntUnaryPredicateMutator("IRETURN_TO_TRUE", true, (int x) -> x != 0, (int x) -> true, Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_1), new InstructionCall(Opcodes.IRETURN));

        new ObjectUnaryOperatorMutator("ARETURN_TO_FALSE", true, (Object x) -> x, (Object x) -> false, Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
                        "valueOf", "(Z)Ljava/lang/Boolean;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("ARETURN_TO_TRUE", true, (Object x) -> x, (Object x) -> true, Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_1),
                new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
                        "valueOf", "(Z)Ljava/lang/Boolean;", false),
                new InstructionCall(Opcodes.ARETURN));

        //Swap increments (INCREMENTS):
        new NoOpMutator("IINC_SWAP", false, Opcodes.IINC, null, new InstructionCall(Opcodes.NOP)); //More symbolic

        //Return empty object (EMPTY_RETURNS)
        new ObjectUnaryOperatorMutator("I_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> 0, Opcodes.ARETURN, "Ljava/lang/Integer;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Integer.class.getName().replace('.', '/'),
                        "valueOf", "(I)Ljava/lang/Integer;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("S_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> (short) 0, Opcodes.ARETURN, "Ljava/lang/Short;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
                new InstructionCall(Opcodes.INVOKESTATIC, Short.class.getName().replace('.', '/'), "valueOf",
                        "(S)Ljava/lang/Short;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("C_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> (char) 0, Opcodes.ARETURN, "Ljava/lang/Character;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
                new InstructionCall(Opcodes.INVOKESTATIC, Character.class.getName().replace('.', '/'),
                        "valueOf", "(C)Ljava/lang/Character;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("L_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> (long) 0, Opcodes.ARETURN, "Ljava/lang/Long;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Long.class.getName().replace('.', '/'), "valueOf",
                        "(J)Ljava/lang/Long;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("F_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> (float) 0, Opcodes.ARETURN, "Ljava/lang/Float;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.FCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Float.class.getName().replace('.', '/'), "valueOf",
                        "(F)Ljava/lang/Float;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("D_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> (double) 0, Opcodes.ARETURN, "Ljava/lang/Double;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.DCONST_0),
                new InstructionCall(Opcodes.INVOKESTATIC, Double.class.getName().replace('.', '/'), "valueOf",
                        "(D)Ljava/lang/Double;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("STR_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> "", Opcodes.ARETURN, "Ljava/lang/String;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LDC, ""),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("LST_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> Collections.emptyList(), Opcodes.ARETURN, "Ljava/util/List;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptyList",
                        "()Ljava/util/List;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("SET_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> Collections.emptySet(), Opcodes.ARETURN, "Ljava/util/Set;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptySet",
                        "()Ljava/util/Set;", false),
                new InstructionCall(Opcodes.ARETURN));
        new ObjectUnaryOperatorMutator("OPT_ARETURN_TO_EMPTY", true, (Object x) -> x, (Object x) -> Optional.empty(), Opcodes.ARETURN, "Ljava/util/Optional;", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Optional", "empty",
                        "()Ljava/util/Optional;", false),
                new InstructionCall(Opcodes.ARETURN));

        //Return null (NULL_RETURNS)
        new ObjectUnaryOperatorMutator("ARETURN_TO_NULL", true, (Object x) -> x, (Object x) -> null, Opcodes.ARETURN, null, new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ACONST_NULL), new InstructionCall(Opcodes.ARETURN));

        //Return 0 instead of primitive (PRIMITIVE_RETURNS)
        new IntUnaryOperatorMutator("I_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "I", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("B_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "B", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("C_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "C", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("S_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "S", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.ICONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("F_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "F", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.FCONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("L_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "L", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LCONST_0),
                new InstructionCall(Opcodes.IRETURN));
        new IntUnaryOperatorMutator("D_IRETURN_TO_0", true, (int x) -> x, (int x) -> 0, Opcodes.IRETURN, "D", new InstructionCall(Opcodes.POP),
                new InstructionCall(Opcodes.LCONST_0),
                new InstructionCall(Opcodes.IRETURN));
    }

}

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

        //Conditional Ops (CONDITIONALS_BOUNDARY):
        new IntUnaryPredicateMutator((int x) -> x <= 0, (int x) -> x > 0, Opcodes.IFLE, null, new InstructionCall(Opcodes.IFGE, null));
        new IntUnaryPredicateMutator((int x) -> x > 0, (int x) -> x >= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFGE, null));
        new IntUnaryPredicateMutator((int x) -> x < 0, (int x) -> x <= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFLE, null));

        //Conditional Negation (NEGATE_CONDITIONALS):
        new IntUnaryPredicateMutator((int x) -> x == 0, (int x) -> x != 0, Opcodes.IFEQ, null, new InstructionCall(Opcodes.IFNE, null));
        new IntUnaryPredicateMutator((int x) -> x != 0, (int x) -> x > 0, Opcodes.IFNE, null, new InstructionCall(Opcodes.IFGT, null));
        new IntUnaryPredicateMutator((int x) -> x >= 0, (int x) -> x < 0, Opcodes.IFGE, null, new InstructionCall(Opcodes.IFLT, null));
        new IntUnaryPredicateMutator((int x) -> x > 0, (int x) -> x <= 0, Opcodes.IFGT, null, new InstructionCall(Opcodes.IFLE, null));
        new IntUnaryPredicateMutator((int x) -> x < 0, (int x) -> x >= 0, Opcodes.IFLT, null, new InstructionCall(Opcodes.IFGE, null));
    }

}

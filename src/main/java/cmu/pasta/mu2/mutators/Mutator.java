package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Mutator {

    private final int toReplace;
    private final String instructionReturnType;
    private final List<InstructionCall> replaceWith;
    public static int cvArg = Opcodes.ASM8;
    public static List<Mutator> allMutators = new ArrayList<>();

    Mutator(int toReplace, String instructionReturnType, InstructionCall... replaceWith) {
        this.toReplace = toReplace;
        this.instructionReturnType = instructionReturnType;
        this.replaceWith = new ArrayList<>();
        this.replaceWith.addAll(Arrays.asList(replaceWith));
        allMutators.add(this);
    }

    public abstract Type getOperandType();
    public abstract Type getReturnType();
    public abstract int getNumArgs();
    public abstract String getMethodDescriptor();
    public String getOriginalOperatorName() {
        return "";
    }
    public String getMutatedOperatorName() {
        return "";
    }

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
        new IntBinaryOperatorMutator("intAdd", "intSub", Opcodes.IADD, null, new InstructionCall(Opcodes.ISUB));
        new IntBinaryOperatorMutator("intSub", "intAdd", Opcodes.ISUB, null, new InstructionCall(Opcodes.IADD));
        new IntBinaryOperatorMutator("intMul", "intDiv", Opcodes.IMUL, null, new InstructionCall(Opcodes.IDIV));
        new IntBinaryOperatorMutator("intDiv", "intMul", Opcodes.IDIV, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator("intRem", "intMul", Opcodes.IREM, null, new InstructionCall(Opcodes.IMUL));
        new IntBinaryOperatorMutator("intOr", "intAnd", Opcodes.IOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator("intAnd", "intOr", Opcodes.IAND, null, new InstructionCall(Opcodes.IOR));
        new IntBinaryOperatorMutator("intXOr", "intAnd", Opcodes.IXOR, null, new InstructionCall(Opcodes.IAND));
        new IntBinaryOperatorMutator("intShiftLeft", "intShiftRight", Opcodes.ISHL, null, new InstructionCall(Opcodes.ISHR));
        new IntBinaryOperatorMutator("intShiftRight", "intShiftLeft", Opcodes.ISHR, null, new InstructionCall(Opcodes.ISHL));
        new IntBinaryOperatorMutator("intUShiftRight", "intShiftLeft", Opcodes.IUSHR, null, new InstructionCall(Opcodes.ISHL));
    }

}

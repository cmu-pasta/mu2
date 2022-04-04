package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntBinaryOperator;

public class IntBinaryOperatorMutator extends Mutator {

    private IntBinaryOperator originalOperator;
    private IntBinaryOperator mutatedOperator;

    public IntBinaryOperatorMutator(IntBinaryOperator originalOperator, IntBinaryOperator mutatedOperator, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalOperator = originalOperator;
        this.mutatedOperator = mutatedOperator;
    }

    @Override
    public Type getOperandType() {
        return Type.INT_TYPE;
    }

    @Override
    public Type getReturnType() {
        return Type.INT_TYPE;
    }

    @Override
    public int getNumArgs() {
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(II)I";
    }

    public static IntBinaryOperatorMutator getMutator(int id) {
        return (IntBinaryOperatorMutator) Mutator.allMutatorsMap.get(id);
    }

    public int runOriginal(int arg1, int arg2) {
        return originalOperator.applyAsInt(arg1, arg2);
    }

    public int runMutated(int arg1, int arg2) {
        return mutatedOperator.applyAsInt(arg1, arg2);
    }
}

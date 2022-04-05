package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

public class FloatBinaryOperatorMutator extends Mutator {

    private FloatBinaryOperator originalOperator;
    private FloatBinaryOperator mutatedOperator;

    public FloatBinaryOperatorMutator(FloatBinaryOperator originalOperator, FloatBinaryOperator mutatedOperator, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalOperator = originalOperator;
        this.mutatedOperator = mutatedOperator;
    }

    @Override
    public Type getOperandType() {
        return Type.FLOAT_TYPE;
    }

    @Override
    public Type getReturnType() {
        return Type.FLOAT_TYPE;
    }

    @Override
    public int getNumArgs() {
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(FF)F";
    }

    public static FloatBinaryOperatorMutator getMutator(int id) {
        return (FloatBinaryOperatorMutator) Mutator.allMutatorsMap.get(id);
    }

    public float runOriginal(float arg1, float arg2) {
        return originalOperator.applyAsFloat(arg1, arg2);
    }

    public float runMutated(float arg1, float arg2) {
        return mutatedOperator.applyAsFloat(arg1, arg2);
    }
}

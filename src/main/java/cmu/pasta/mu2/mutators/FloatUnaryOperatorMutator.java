package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

public class FloatUnaryOperatorMutator extends Mutator {

    private FloatUnaryOperator originalFunction;
    private FloatUnaryOperator mutatorFunction;

    FloatUnaryOperatorMutator(FloatUnaryOperator originalFunction, FloatUnaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatorFunction = mutatorFunction;
    }

    @Override
    public Type getOperandType() {
        return Type.DOUBLE_TYPE;
    }

    @Override
    public Type getReturnType() {
        return Type.DOUBLE_TYPE;
    }

    @Override
    public int getNumArgs() {
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(F)F";
    }

    public static FloatUnaryOperatorMutator getMutator(int id) {
        return (FloatUnaryOperatorMutator) Mutator.allMutatorsMap.get(id);
    }

    public float runOriginal(float arg) {
        return originalFunction.applyAsFloat(arg);
    }

    public float runMutated(float arg) {
        return mutatorFunction.applyAsFloat(arg);
    }
}

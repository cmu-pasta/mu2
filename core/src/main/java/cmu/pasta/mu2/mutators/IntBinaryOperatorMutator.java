package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntBinaryOperator;

public class IntBinaryOperatorMutator extends Mutator {

    private IntBinaryOperator originalFunction;
    private IntBinaryOperator mutatedFunction;

    public IntBinaryOperatorMutator(String name, boolean useInfection, IntBinaryOperator originalFunction, IntBinaryOperator mutatedFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, useInfection, toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatedFunction = mutatedFunction;
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
        return (IntBinaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public int runOriginal(int arg1, int arg2) {
        return originalFunction.applyAsInt(arg1, arg2);
    }

    public int runMutated(int arg1, int arg2) {
        return mutatedFunction.applyAsInt(arg1, arg2);
    }
}

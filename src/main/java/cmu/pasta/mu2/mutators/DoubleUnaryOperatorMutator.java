package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.DoubleUnaryOperator;

public class DoubleUnaryOperatorMutator extends Mutator {

    private DoubleUnaryOperator originalFunction;
    private DoubleUnaryOperator mutatorFunction;

    DoubleUnaryOperatorMutator(String name, boolean useInfection, DoubleUnaryOperator originalFunction, DoubleUnaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, useInfection, toReplace, returnType, replaceWith);
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
        return "(D)D";
    }

    public static DoubleUnaryOperatorMutator getMutator(int id) {
        return (DoubleUnaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public double runOriginal(double arg) {
        return originalFunction.applyAsDouble(arg);
    }

    public double runMutated(double arg) {
        return mutatorFunction.applyAsDouble(arg);
    }
}

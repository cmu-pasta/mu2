package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorMutator extends Mutator {

    private DoubleBinaryOperator originalFunction;
    private DoubleBinaryOperator mutatorFunction;

    DoubleBinaryOperatorMutator(DoubleBinaryOperator originalFunction, DoubleBinaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(DD)D";
    }

    public double runOriginal(double arg1, double arg2) {
        return originalFunction.applyAsDouble(arg1, arg2);
    }

    public double runMutated(double arg1, double arg2) {
        return mutatorFunction.applyAsDouble(arg1, arg2);
    }
}

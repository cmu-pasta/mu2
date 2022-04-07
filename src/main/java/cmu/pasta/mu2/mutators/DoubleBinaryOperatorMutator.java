package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorMutator extends Mutator {

    private DoubleBinaryOperator originalFunction;
    private DoubleBinaryOperator mutatorFunction;
    private double secondArg;

    DoubleBinaryOperatorMutator(String name, boolean useInfection, DoubleBinaryOperator originalFunction, DoubleBinaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(D)D";
    }

    public static DoubleBinaryOperatorMutator getMutator(int id) {
        return (DoubleBinaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public void readSecondArg(double arg) {
        this.secondArg = arg;
    }

    public double writeSecondArg() {
        return secondArg;
    }

    public double runOriginal(double arg1) {
        return originalFunction.applyAsDouble(arg1, this.secondArg);
    }

    public double runMutated(double arg1) {
        return mutatorFunction.applyAsDouble(arg1, this.secondArg);
    }
}

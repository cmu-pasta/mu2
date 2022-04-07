package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorMutator extends Mutator {

    private LongBinaryOperator originalFunction;
    private LongBinaryOperator mutatorFunction;
    private long secondArg;

    LongBinaryOperatorMutator(String name, boolean useInfection, LongBinaryOperator originalFunction, LongBinaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, useInfection, toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatorFunction = mutatorFunction;
    }

    @Override
    public Type getOperandType() {
        return Type.LONG_TYPE;
    }

    @Override
    public Type getReturnType() {
        return Type.LONG_TYPE;
    }

    @Override
    public int getNumArgs() {
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(J)J";
    }

    public static LongBinaryOperatorMutator getMutator(int id) {
        return (LongBinaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public void readSecondArg(long arg) {
        this.secondArg = arg;
    }

    public long writeSecondArg() {
        return secondArg;
    }

    public long runOriginal(long arg) {
        return originalFunction.applyAsLong(arg, this.secondArg);
    }

    public long runMutated(long arg) {
        return mutatorFunction.applyAsLong(arg, this.secondArg);
    }
}

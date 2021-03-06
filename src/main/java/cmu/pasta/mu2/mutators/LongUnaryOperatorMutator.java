package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.LongUnaryOperator;

public class LongUnaryOperatorMutator extends Mutator {

    private LongUnaryOperator originalFunction;
    private LongUnaryOperator mutatorFunction;

    LongUnaryOperatorMutator(String name, boolean useInfection, LongUnaryOperator originalFunction, LongUnaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(J)J";
    }

    public static LongUnaryOperatorMutator getMutator(int id) {
        return (LongUnaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public long runOriginal(long arg) {
        return originalFunction.applyAsLong(arg);
    }

    public long runMutated(long arg) {
        return mutatorFunction.applyAsLong(arg);
    }
}

package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorMutator extends Mutator {

    private LongBinaryOperator originalFunction;
    private LongBinaryOperator mutatorFunction;

    LongBinaryOperatorMutator(LongBinaryOperator originalFunction, LongBinaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
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
        return "(LL)L";
    }

    public long runOriginal(long arg1, long arg2) {
        return originalFunction.applyAsLong(arg1, arg2);
    }

    public long runMutated(long arg1, long arg2) {
        return mutatorFunction.applyAsLong(arg1, arg2);
    }
}

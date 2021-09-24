package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntUnaryOperator;

public class IntUnaryOperatorMutator extends Mutator {

    private IntUnaryOperator originalFunction;
    private IntUnaryOperator mutatorFunction;

    IntUnaryOperatorMutator(IntUnaryOperator originalFunction, IntUnaryOperator mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatorFunction = mutatorFunction;
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
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(I)I";
    }

    public int runOriginal(int arg) {
        return originalFunction.applyAsInt(arg);
    }

    public int runMutated(int arg) {
        return mutatorFunction.applyAsInt(arg);
    }
}

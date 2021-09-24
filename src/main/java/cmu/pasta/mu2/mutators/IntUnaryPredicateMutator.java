package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntPredicate;

public class IntUnaryPredicateMutator extends Mutator {

    private IntPredicate originalFunction;
    private IntPredicate mutatorFunction;

    IntUnaryPredicateMutator(IntPredicate originalFunction, IntPredicate mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return Type.BOOLEAN_TYPE;
    }

    @Override
    public int getNumArgs() {
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(I)B";
    }

    public boolean runOriginal(int arg) {
        return originalFunction.test(arg);
    }

    public boolean runMutated(int arg) {
        return mutatorFunction.test(arg);
    }
}

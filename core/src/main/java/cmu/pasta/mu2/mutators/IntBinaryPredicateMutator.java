package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

public class IntBinaryPredicateMutator extends Mutator {

    private IntBinaryPredicate originalOperator;
    private IntBinaryPredicate mutatedOperator;

    public IntBinaryPredicateMutator(String name, boolean useInfection, IntBinaryPredicate originalOperator, IntBinaryPredicate mutatedOperator, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, useInfection, toReplace, returnType, replaceWith);
        this.originalOperator = originalOperator;
        this.mutatedOperator = mutatedOperator;
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
        return 2;
    }

    @Override
    public String getMethodDescriptor() {
        return "(II)Z";
    }

    public static IntBinaryPredicateMutator getMutator(int id) {
        return (IntBinaryPredicateMutator) Mutator.allMutators.get(id);
    }

    public boolean runOriginal(int arg1, int arg2) {
        return originalOperator.test(arg1, arg2);
    }

    public boolean runMutated(int arg1, int arg2) {
        return mutatedOperator.test(arg1, arg2);
    }
}

package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.Predicate;

public class ObjectUnaryPredicateMutator extends Mutator {

    private Predicate<Object> originalFunction;
    private Predicate<Object> mutatorFunction;

    ObjectUnaryPredicateMutator(String name, boolean useInfection, Predicate<Object> originalFunction, Predicate<Object> mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, useInfection, toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatorFunction = mutatorFunction;
    }

    @Override
    public Type getOperandType() {
        return Type.getObjectType("java/lang/Object");
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
        return "(Ljava/lang/Object;)Z";
    }

    public boolean runOriginal(Object arg) {
        return originalFunction.test(arg);
    }

    public boolean runMutated(Object arg) {
        return mutatorFunction.test(arg);
    }
}

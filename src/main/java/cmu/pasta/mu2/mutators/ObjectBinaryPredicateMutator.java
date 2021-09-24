package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.BiPredicate;

public class ObjectBinaryPredicateMutator extends Mutator {

    private BiPredicate<Object, Object> originalFunction;
    private BiPredicate<Object, Object> mutatorFunction;

    ObjectBinaryPredicateMutator(BiPredicate<Object, Object> originalFunction, BiPredicate<Object, Object> mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalFunction = originalFunction;
        this.mutatorFunction = mutatorFunction;
    }

    @Override
    public Type getOperandType() {
        return Type.getObjectType("java/lang/Object");
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
        return "(Ljava/lang/Object;Ljava/lang/Object)B";
    }

    public boolean runOriginal(Object arg1, Object arg2) {
        return originalFunction.test(arg1, arg2);
    }

    public boolean runMutated(Object arg1, Object arg2) {
        return mutatorFunction.test(arg1, arg2);
    }
}

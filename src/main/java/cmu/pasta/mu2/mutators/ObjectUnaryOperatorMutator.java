package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.UnaryOperator;

public class ObjectUnaryOperatorMutator extends Mutator {

    private UnaryOperator<Object> originalFunction;
    private UnaryOperator<Object> mutatorFunction;

    ObjectUnaryOperatorMutator(UnaryOperator<Object> originalFunction, UnaryOperator<Object> mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return Type.getObjectType("java/lang/Object");
    }

    @Override
    public int getNumArgs() {
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(Ljava/lang/Object)Ljava/lang/Object;";
    }

    public static IntUnaryOperatorMutator getMutator(int id) {
        return (IntUnaryOperatorMutator) Mutator.allMutatorsMap.get(id);
    }

    public Object runOriginal(Object arg) {
        return originalFunction.apply(arg);
    }

    public Object runMutated(Object arg) {
        return mutatorFunction.apply(arg);
    }
}

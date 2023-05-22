package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.UnaryOperator;

public class ObjectUnaryOperatorMutator extends Mutator {

    private UnaryOperator<Object> originalFunction;
    private UnaryOperator<Object> mutatorFunction;

    ObjectUnaryOperatorMutator(String name, boolean useInfection, UnaryOperator<Object> originalFunction, UnaryOperator<Object> mutatorFunction, int toReplace, String returnType, InstructionCall... replaceWith) {
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
        return Type.getObjectType("java/lang/Object");
    }

    @Override
    public int getNumArgs() {
        return 1;
    }

    @Override
    public String getMethodDescriptor() {
        return "(Ljava/lang/Object;)Ljava/lang/Object;";
    }

    public static ObjectUnaryOperatorMutator getMutator(int id) {
        return (ObjectUnaryOperatorMutator) Mutator.allMutators.get(id);
    }

    public Object runOriginal(Object arg) {
        return originalFunction.apply(arg);
    }

    public Object runMutated(Object arg) {
        return mutatorFunction.apply(arg);
    }
}

package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntBinaryOperator;

public class NoOpMutator extends Mutator {

    public NoOpMutator(String name, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(name, false, toReplace, returnType, replaceWith);
    }

    @Override
    public Type getOperandType() {
        return null;
    }

    @Override
    public Type getReturnType() {
        return null;
    }

    @Override
    public int getNumArgs() {
        return 0;
    }

    @Override
    public String getMethodDescriptor() {
        return null;
    }

    public static NoOpMutator getMutator(int id) {
        return (NoOpMutator) Mutator.allMutators.get(id);
    }
}

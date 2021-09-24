package cmu.pasta.mu2.mutators;

import cmu.pasta.mu2.instrument.InstructionCall;
import org.objectweb.asm.Type;

import java.util.function.IntBinaryOperator;

public class IntBinaryOperatorMutator extends Mutator {

    private String originalOperator;
    private String mutatedOperator;

    public IntBinaryOperatorMutator(String originalOperator, String mutatedOperator, int toReplace, String returnType, InstructionCall... replaceWith) {
        super(toReplace, returnType, replaceWith);
        this.originalOperator = originalOperator;
        this.mutatedOperator = mutatedOperator;
    }

    @Override
    public Type getOperandType() {
        return Type.INT_TYPE;
    }

    @Override
    public Type getReturnType() {
        return Type.getObjectType("java/lang/Integer");
    }

    @Override
    public int getNumArgs() {
        return 2;
    }

    @Override
    public String getOriginalOperatorName() {
        return originalOperator;
    }

    @Override
    public String getMutatedOperatorName() {
        return mutatedOperator;
    }

    @Override
    public String getMethodDescriptor() {
        return "(II)Ljava/lang/Integer;";
    }
}

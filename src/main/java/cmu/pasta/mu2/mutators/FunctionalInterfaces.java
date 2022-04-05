package cmu.pasta.mu2.mutators;

@FunctionalInterface
interface IntBinaryPredicate {
    boolean test(int a, int b);
}

@FunctionalInterface
interface FloatBinaryOperator {
    float applyAsFloat(float a, float b);
}

@FunctionalInterface
interface FloatUnaryOperator {
    float applyAsFloat(float a);
}

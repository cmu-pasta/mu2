package cmu.pasta.mu2.mutators;

public class Operators {

    public static Integer intAdd(int arg1, int arg2) {
        return arg1 + arg2;
    }

    public static Integer intSub(int arg1, int arg2) {
        return arg1 - arg2;
    }

    public static Integer intMul(int arg1, int arg2) {
        return arg1 * arg2;
    }

    public static Integer intDiv(int arg1, int arg2) {
        return arg1 / arg2;
    }

    public static Integer intRem(int arg1, int arg2) {
        return arg1 % arg2;
    }

    public static Integer intAnd(int arg1, int arg2) {
        return arg1 & arg2;
    }

    public static Integer intOr(int arg1, int arg2) {
        return arg1 | arg2;
    }

    public static Integer intXOr(int arg1, int arg2) {
        return arg1 ^ arg2;
    }

    public static Integer intShiftLeft(int arg1, int arg2) {
        return arg1 << arg2;
    }

    public static Integer intShiftRight(int arg1, int arg2) {
        return arg1 >> arg2;
    }

    public static Integer intUShiftRight(int arg1, int arg2) {
        return arg1 >>> arg2;
    }
}

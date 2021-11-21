package cmu.pasta.mu2;

public class ValidityDifferenceException extends RuntimeException {
    String message;
    public ValidityDifferenceException(String m) {
        super();
        message = m;
    }

    public String getMessage() {
        return message;
    }
}

package oop.ex6.main;


/**
 * The main parser exception class. Inherited in the various parsing components
 * to indicate different parsing errors. Other than the error message, this
 * class also has the error code, printed to stdout at the end of the program
 * when catching the exception - 1 for invalid code, 2 for an I/O error.
 */
public class ParserException extends Exception {
    private static final int ILLEGAL_CODE_ERROR = 1;
    public static final int IO_ERROR = 2;

    private int errorCode;

    public ParserException(String message) {
        this(message, ILLEGAL_CODE_ERROR);
    }

    public ParserException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getExceptionCode() {
        return errorCode;
    }
}

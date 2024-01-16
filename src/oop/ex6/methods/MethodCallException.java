package oop.ex6.methods;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of method call, inherits from ParseError
 */

public class MethodCallException extends ParserException {
    /**
     * @param message that appears when the exception is thrown
     */
    public MethodCallException(String message) {
        super(message);
    }
}

package oop.ex6.methods;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of method call, inherits from ParseError
 */

public class MethodParamsException extends ParserException {
    /**
     * @param message hat appears when the exception is thrown
     */
    public MethodParamsException(String message) {
        super(message);
    }
}

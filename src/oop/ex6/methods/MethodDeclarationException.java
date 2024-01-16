package oop.ex6.methods;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of method signature, inherits from ParseError
 */

public class MethodDeclarationException extends ParserException {
    /**
     * @param message that appears when the exception is thrown
     */
    public MethodDeclarationException(String message) {
        super(message);
    }
}

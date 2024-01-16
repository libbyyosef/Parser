package oop.ex6.conditions;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of if statement, inherits from ParseError
 */

public class IfException extends ParserException {
    /**
     * @param message that appears when the exception is thrown
     */

    public IfException(String message) {
        super(message);
    }
}



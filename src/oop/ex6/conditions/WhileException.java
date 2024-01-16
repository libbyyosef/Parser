package oop.ex6.conditions;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of while statement, inherits from ParseError
 */

public class WhileException extends ParserException {
    /**
     * @param message that appears when the exception is thrown
     */
    public WhileException(String message) {
        super(message);
    }

}

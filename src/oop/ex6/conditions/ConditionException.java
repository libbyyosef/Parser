package oop.ex6.conditions;

import oop.ex6.main.ParserException;

/**
 * an exception that represent invalidity of condition statement, inherits from ParseError
 */
public class ConditionException extends ParserException {
    /**
     * @param message that appears when the exception is thrown
     */
    public ConditionException(String message) {
        super(message);
    }

}

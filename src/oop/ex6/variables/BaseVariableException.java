package oop.ex6.variables;

import oop.ex6.main.ParserException;

/**
 * The base class for variable related exceptions - the usage of a variable doesn't match the type,
 * use of an uninit variable, write to final, etc.
 */
public class BaseVariableException extends ParserException {
    /**
     * Create a new base variable exception
     *
     * @param message The exception message
     */
    public BaseVariableException(String message) {
        super(message);
    }
}

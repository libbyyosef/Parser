package oop.ex6.variables;

import oop.ex6.main.CommonPatterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static oop.ex6.variables.VariableParser.*;

/**
 * An exception representing a type not matching it's usage. For instance used
 * when assigning a string to an int, or passing a string to a method that takes
 * an int.
 */
class BadValueException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Value usage doesn't match the declared type";

    public BadValueException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception that's thrown when we have an assignment that doesn't
 * actually assign any value.
 */
class MissingValueException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "No value assigned to variable";

    public MissingValueException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception thrown when trying to read or write an uninitialized or nonexistent
 * variable.
 */
class UninitVariableReadException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Trying to read/write to uninit/non-existent variable";

    public UninitVariableReadException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * Thrown when attempting to write to a final variable
 */
class WriteToFinalException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Trying to write to a final variable after declaration";

    public WriteToFinalException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * The main class used for verifying types and usages of variables. Used
 * when assigning or declaring variables, using variables in if/while statements,
 * method calls, etc. Its purpose is to save each user from having to verify types
 * or references to variables by himself
 */
public class VariableVerifier {
    private static final String INT = "int";
    private static final String DOUBLE = "double";
    private static final String STRING = "String";
    private static final String BOOLEAN = "boolean";
    private static final String CHAR = "char";
    private static final String NAME = "name";


    private VariableScope currentScope, parentScope;

    /**
     * Create a new variable verifier
     *
     * @param parentScope  The current parent scope
     * @param currentScope The current local scope
     */
    public VariableVerifier(VariableScope parentScope, VariableScope currentScope) {
        this.parentScope = parentScope;
        this.currentScope = currentScope;
    }

    /**
     * The main helper - verifying if a value corresponds to a type.
     * This works whether value is a constant, or a reference to a variable name.
     *
     * @param typeName The required value type
     * @param value    The value to verify aginst the passed typeName
     * @throws BadValueException           In case the value doesn't match the type
     * @throws UninitVariableReadException If the value references an uninitialized variable
     */
    public void verifyVariableUsage(String typeName, String value) throws BadValueException,
            UninitVariableReadException {
        // First, check the case where we refer to an existing variable. It luckily
        // doesn't conflict with the assignment, unless it's a boolean. If we do, go to the "variable read"
        // flow,
        // otherwise to the const assignment flow.
        Matcher varNameMatcher = ONLY_NAME_PATTERN.matcher(value);
        // Sadly a boolean is a valid name. We need to catch that here and treat it as a
        // literal/
        Matcher booleanLiteralPattern = BOOLEAN_LITERALS_PATTERN.matcher(value);
        if (varNameMatcher.find() && !booleanLiteralPattern.find()) {
            String varName = varNameMatcher.group(NAME).trim();
            verifyReferenceAssignment(typeName, varName);
        } else {
            verifyConstAssignment(typeName, value);
        }
    }

    /**
     * Verify a variable assignment
     *
     * @param assignment the variable assignment
     * @return The assigned variable if the assignment is valid - no reference to uninit variable, the
     * value is correct, etc.
     * @throws BaseVariableException If the assignment is illegal
     */
    Variable verifyAssignment(VariableAssignment assignment) throws BaseVariableException {
        // Verify that we have indeed assigned something
        if (assignment.getAssignment() == null) {
            throw new MissingValueException();
        }
        // Make sure we write to an existing, non-final variable(in any of the scopes)
        Variable assigningTo = ScopeHelpers.findRelevantVariable(assignment.getName(),
                currentScope, parentScope);
        if (assigningTo != null) {
            if (assigningTo.getFinal()) {
                throw new WriteToFinalException();
            }
        } else {
            throw new UninitVariableReadException();
        }
        // Verify the assignment value
        verifyVariableUsage(assigningTo.getType(), assignment.getAssignment());
        return assigningTo;
    }

    /**
     * Verify a single variable declaration - the variable isn't already in scope,
     * if there is an assignemnt it's valid, etc.
     *
     * @param declaration The variable declaration
     * @return The declared variable if the declaration is valid
     * @throws BaseVariableException Thrown if the declaration is illegal for reasons
     *                               described above.
     */
    Variable verifyDeclaration(VariableDeclaration declaration) throws BaseVariableException {
        // Verify that the variable isn't a re-declaration of a variable in the
        // current scope
        if (currentScope.isVariableInScope(declaration.getName())) {
            throw new VariableAlreadyExistsException();
        }
        // If we're final, we must have an assigned value
        if (declaration.getFinal() && declaration.getAssignment() == null) {
            throw new MissingValueException();
        }

        // Verify the assignment, if there is one
        if (declaration.getAssignment() != null) {
            verifyVariableUsage(declaration.getType(), declaration.getAssignment());
        }
        Variable declaredVariable = new Variable(declaration.getType(), declaration.getName(),
                declaration.getFinal());
        return declaredVariable;
    }

    /**
     * A helper for verifying that a variable reference is valid
     *
     * @param toType  what type we're trying to write/use
     * @param varName what is the variable we're looking up
     * @throws BadValueException           If the variable doesn't match the type
     * @throws UninitVariableReadException If the variable is uninitialized
     */
    private void verifyReferenceAssignment(String toType, String varName) throws BadValueException,
            UninitVariableReadException {
        Variable readingFrom = ScopeHelpers.findRelevantVariable(varName, currentScope,
                parentScope);
        if (readingFrom != null && readingFrom.isInitialized()) {
            if (!isCastLegal(readingFrom.getType(), toType)) {
                throw new BadValueException();
            }
        } else {
            throw new UninitVariableReadException();
        }
    }


    /**
     * Verify that a constant value matches a type, using the value regexes from
     * the variable parser.
     *
     * @param typeName The type we're trying to write/use
     * @param value    The value we're trying to "cast" to the passed type
     * @throws BadValueException If the value doesn't match the type
     */
    private void verifyConstAssignment(String typeName, String value) throws BadValueException {
        String verifyingPattern;
        switch (typeName) {
            case INT:
                verifyingPattern = CommonPatterns.INTEGER_PATTERN;
                break;
            case DOUBLE:
                verifyingPattern = CommonPatterns.DOUBLE_PATTERN;
                break;
            case STRING:
                verifyingPattern = CommonPatterns.STRING_PATTERN;
                break;
            case BOOLEAN:
                verifyingPattern = CommonPatterns.BOOLEAN_PATTERN;
                break;
            case CHAR:
                verifyingPattern = CommonPatterns.CHAR_PATTERN;
                break;
            default:
                return;
        }
        Matcher valueMatch = Pattern.compile("^" + verifyingPattern + "$").matcher(value);
        if (!valueMatch.find()) {
            throw new BadValueException();
        }
    }

    /**
     * Returns true if a cast from one type to another is legal,
     * false otherwise
     *
     * @param fromType The type to cast from
     * @param toType   The type we're casting to
     * @return true if the cast is legal, false otherwise
     */
    private boolean isCastLegal(String fromType, String toType) {
        if (fromType.equals(toType))
            return true;
        if (toType.equals(BOOLEAN) && (fromType.equals(DOUBLE) || fromType.equals(INT)))
            return true;
        if (toType.equals(DOUBLE) && fromType.equals(INT))
            return true;
        return false;
    }

    /**
     * Update the scopes used in the verifier. This is called by the main parser when entering or
     * exiting in a scope.
     *
     * @param parentScope  The new parent scope
     * @param currentScope The new local scope
     */
    public void updateScopes(VariableScope parentScope, VariableScope currentScope) {
        this.parentScope = parentScope;
        this.currentScope = currentScope;
    }

}

package oop.ex6.variables;

import oop.ex6.main.CommonPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An exception thrown in statement of kind "a;"
 */
class NoDeclarationTypeException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Missing type in variable declaration";

    /**
     * Creates a new "no declaration type" exception.
     */
    public NoDeclarationTypeException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception thrown in statement of kind " = 2;"
 */
class NoAssignmentNameException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Missing name in variable assignment";

    /**
     * Create a new "no name in assignment" exception
     */
    public NoAssignmentNameException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception thrown in the case where we try to declare again a value already
 * in scope, like "int a; \n int a".
 */
class VariableAlreadyExistsException extends BaseVariableException {
    private static final String ERROR_MESSAGE = "Variable already declared in scope";

    /**
     * Create a new variable already exists error
     */
    public VariableAlreadyExistsException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * A helper for declaring a new variable. Contains the name, type and whether it
 * was delcared as final. if the declaration had a value, like "int a = 1;"
 * the value is also stored.
 */
class VariableDeclaration {
    private String name;
    private String type;
    private String possibleAssignment;
    private boolean isFinal;

    /**
     * Creates a new variable declaration
     *
     * @param type    the declaration type
     * @param name    the declaration name
     * @param isFinal is the variable final
     */
    public VariableDeclaration(String type, String name, boolean isFinal) {
        this.type = type;
        this.name = name;
        this.isFinal = isFinal;
    }

    /**
     * Returns the declared name
     *
     * @return the declared name
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the declared type
     *
     * @return the declared type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the declared assignment value
     *
     * @return the declared assignment value
     */
    public String getAssignment() {
        return possibleAssignment;
    }

    /**
     * Returns true if the variable was declared as final, false otherwise.
     *
     * @return true if the variable was declared as final, false otherwise.
     */
    public boolean getFinal() {
        return isFinal;
    }

    /**
     * Set the variable assignment value
     *
     * @param assignment the assigned value
     */
    public void setAssignment(String assignment) {
        this.possibleAssignment = assignment;
    }


    /**
     * Returns a string representation of the assignment, for debugging.
     *
     * @return a pretty string representation of the assignment
     */
    @Override
    public String toString() {
        return "name = " + name + " type = " + type + " value = " + possibleAssignment
                + " final = " + isFinal;
    }
}

/**
 * The main facade implementation for parsing variable declaration and assignment
 * lines. Has two main helpers - one for detecting a variable declaration/assignment,
 * and another one which actually parses the line, updating the passed variable scope
 * in the process.
 */
public class VariableParser {
    // Follows are a collection of regexes used for parsing types, their values, var names,
    // and assignments. Some of these are used in other places in the code that need them,
    // for example in the variable verifier.
    private static final String TYPE_FINAL_PATTERN = "(?<typeFinal>int|double|String|boolean|char|" +
            "(final\\s))";
    private static final Pattern STARTS_WITH_TYPE_PATTERN = Pattern.compile("^\\s*" +
            TYPE_FINAL_PATTERN);
    private static final String VALID_NAME_CAPTURE_GROUP = "(?<name>" + CommonPatterns.VALID_NAME + ")";

    // Used to distinguish "true/false" from a variable name in verifyAssignment
    static final Pattern BOOLEAN_LITERALS_PATTERN = Pattern.compile("^true|false");

    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*" +
            CommonPatterns.VALID_TYPE_PATTERN + "\\s+");
    private static final String ASSIGN_REGEX = "=" + "(\\s*" + CommonPatterns.VALUE_PATTERN + ")";
    private static final Pattern ASSIGN_PATTERN = Pattern.compile("^\\s*" + ASSIGN_REGEX);
    private static final Pattern LINE_END_PATTERN = Pattern.compile("^\\s*;$");
    static final Pattern ONLY_NAME_PATTERN = Pattern.compile("^\\s*" + VALID_NAME_CAPTURE_GROUP);
    private static final Pattern COMMA_NAME_PATTERN = Pattern.compile("^\\s*,\\s*" +
            VALID_NAME_CAPTURE_GROUP);
    private static final String TYPE_FINAL = "typeFinal";
    private static final String NAME = "name";
    private static final String FINAL = "final";
    private static final String TYPE = "type";
    private static final String VALUE = "value";

    /**
     * Parse a variable declaration or assignment line. It might contain multiple assignments
     * or declarations, which this function also handles
     *
     * @param line             the line to parse
     * @param variableVerifier the verifier used for checking references to variables, types, etc.
     *                         See VariableVerifier docs for more info
     * @param addToScope       What scope to add the parsed variables to/update with info on assignments
     * @throws BaseVariableException in case any parsing error occurs.
     */
    public static void parseVariablesLine(String line, VariableVerifier variableVerifier,
                                          VariableScope addToScope) throws BaseVariableException {
        Matcher typeMatch = STARTS_WITH_TYPE_PATTERN.matcher(line);
        if (typeMatch.find()) {
            String typeOrFinal = typeMatch.group(TYPE_FINAL);
            boolean isFinal = false;
            // Advance over the final
            if (typeOrFinal.startsWith(FINAL)) {
                line = line.substring(typeMatch.end());
                isFinal = true;
            }
            List<VariableDeclaration> declarations = parseVariableDeclarationsLine(line, isFinal,
                    variableVerifier, addToScope);
        } else {
            List<VariableAssignment> assignments = parseVariableAssignments(line);
            verifyVariableAssignments(assignments, variableVerifier);
        }
    }

    /***
     * Parse a line that contains variable declarations
     * @param line the line to parse
     * @param isFinal whether the first variable in the line was final, which
     *                means all are final
     * @param variableVerifier The verifier to verify assignments with
     * @param addToScope The scope to add the variables to
     * @return a list of variable declarations in the line
     * @throws BaseVariableException in case of a parsing/reference to uninitialized error.
     */
    private static List<VariableDeclaration> parseVariableDeclarationsLine(String line,
                                                                           boolean isFinal,
                                                                           VariableVerifier variableVerifier,
                                                                           VariableScope addToScope)
            throws BaseVariableException {
        ArrayList<VariableDeclaration> declarations = new ArrayList<VariableDeclaration>();
        Matcher typeMatcher = TYPE_PATTERN.matcher(line);
        if (typeMatcher.find()) {
            String type = typeMatcher.group(TYPE);
            String remainder = line.substring(typeMatcher.end());
            List<VariableAssignment> assignments = parseVariableAssignments(remainder);
            for (VariableAssignment assignment : assignments) {
                VariableDeclaration declaration = new VariableDeclaration(type,
                        assignment.getName(), isFinal);
                if (assignment.getAssignment() != null) {
                    declaration.setAssignment(assignment.getAssignment());
                }
                declarations.add(declaration);
            }
            verifyAddVariableDeclarations(declarations, variableVerifier, addToScope);
        } else {
            throw new NoDeclarationTypeException();
        }
        return declarations;
    }


    /***
     * Parse a line of variable assignments. Parsing validation is done here while value
     * verification happens at the caller.
     * @param line the line to parse
     * @return The variable assignments detected
     * @throws NoAssignmentNameException in case of a parsing error
     */
    private static List<VariableAssignment> parseVariableAssignments(String line) throws
            NoAssignmentNameException {
        ArrayList<VariableAssignment> assignments = new ArrayList<VariableAssignment>();
        Pattern currentPattern = ONLY_NAME_PATTERN;
        while (!LINE_END_PATTERN.matcher(line).matches()) {
            Matcher nameMatcher = currentPattern.matcher(line);
            if (nameMatcher.find()) {
                String name = nameMatcher.group(NAME);
                line = line.substring(nameMatcher.end());
                Matcher assignmentMatcher = ASSIGN_PATTERN.matcher(line);
                String value = null;
                if (assignmentMatcher.find()) {
                    value = assignmentMatcher.group(VALUE);
                    line = line.substring(assignmentMatcher.end());
                }
                VariableAssignment assignment = new VariableAssignment(name, value);
                assignments.add(assignment);
            } else {
                throw new NoAssignmentNameException();
            }
            currentPattern = COMMA_NAME_PATTERN;
        }
        return assignments;
    }

    /**
     * Verify that the variable declarations are valid - the types match,
     * no uninit variables are used, no duplicate declarations, etc. Note that
     * the syntax itself is correct at this point.
     *
     * @param declarations     The parsed declarations
     * @param variableVerifier The variable verifier
     * @param addToScope       What scope to add the variables to
     * @throws BaseVariableException in case of a validity error
     */
    private static void verifyAddVariableDeclarations(List<VariableDeclaration> declarations,
                                                      VariableVerifier variableVerifier,
                                                      VariableScope addToScope) throws BaseVariableException {
        for (VariableDeclaration declaration : declarations) {
            Variable declaredVariable = variableVerifier.verifyDeclaration(declaration);
            if (declaration.getAssignment() != null)
                declaredVariable.setInitialized();
            addToScope.addVariable(declaredVariable);
        }
    }

    /**
     * Verify a list of variable assignments for validity - whether all the values are
     * initialized, there is no write to final, etc.
     *
     * @param assignments      The parsed assignments
     * @param variableVerifier The variable verifier used for verifying validity
     * @throws BaseVariableException in case of a validity error
     */
    private static void verifyVariableAssignments(List<VariableAssignment> assignments,
                                                  VariableVerifier variableVerifier)
            throws BaseVariableException {
        for (VariableAssignment assignment : assignments) {
            Variable assigningTo = variableVerifier.verifyAssignment(assignment);
            // We are now no longer uninit as we've just written a value, write it.
            assigningTo.setInitialized();
        }
    }

}

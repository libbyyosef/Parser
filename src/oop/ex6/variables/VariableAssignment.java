package oop.ex6.variables;

/**
 * A variable assignment class, simply representing an expression of type name=assignment
 */
class VariableAssignment {

    private static final String NAME = "name = ";
    private static final String ASSIGNMENT = " assignment = ";
    private String name;
    private String assignment;

    /**
     * Create a new variable assignment
     *
     * @param name       the name of the variable we're assigning to
     * @param assignment the value we're assigning to it, can be
     *                   another variable name
     */
    public VariableAssignment(String name, String assignment) {
        this.name = name;
        this.assignment = assignment;
    }

    /**
     * Get the variable name
     *
     * @return the name we're assigning to
     */
    public String getName() {
        return name;
    }

    /**
     * Get the assignment value we're writing
     *
     * @return the assignment value
     */
    public String getAssignment() {
        return assignment;
    }

    /**
     * Creates a pretty representation of the assignment a string, used for debugging.
     *
     * @return the string representation of the assignment
     */
    @Override
    public String toString() {
        return NAME + name + ASSIGNMENT + assignment;
    }
}


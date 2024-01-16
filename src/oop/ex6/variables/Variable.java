package oop.ex6.variables;

/***
 * A simple container for a variable, containing its type, name, whether it was
 * initialized, and whether it's final.
 */
public class Variable {
    private static final String NAME = "name = ";
    private static final String TYPE = " type = ";
    private static final String INITILIZED = " initialized = ";
    private static final String FINAL = " final = ";
    private String name;
    private String type;
    private boolean initialized;
    private boolean isFinal;

    /**
     * Create a new variable.
     *
     * @param type    the variable type
     * @param name    the variable name
     * @param isFinal was the variable declared as final
     */
    public Variable(String type, String name, boolean isFinal) {
        this.name = name.strip();
        this.type = type.strip();
        this.isFinal = isFinal;
    }

    /**
     * Returns true if the variable was initialized sometime in the program.
     *
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /***
     * Returns true if the variable is final, false otherwise
     * @return true if the variable is final, false otherwise
     */
    public boolean getFinal() {
        return isFinal;
    }

    /**
     * Sets the variable to initialized. Called on a method parameter or when
     * assigning a value.
     */
    public void setInitialized() {
        initialized = true;
    }

    /**
     * Returns the variable name
     *
     * @return the variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the variable type
     *
     * @return the variable type
     */
    public String getType() {
        return type;
    }

    /**
     * A helper for printing the variable
     *
     * @return A pretty string representation of the variable.
     */
    @Override
    public String toString() {
        return NAME + name + TYPE + type + INITILIZED + initialized
                + FINAL + isFinal;
    }

    /***
     * Clone the variable
     * @return The exact same variable, cloned.
     */
    public Variable clone() {
        Variable cloned = new Variable(type, name, isFinal);
        cloned.initialized = initialized;
        return cloned;
    }
}

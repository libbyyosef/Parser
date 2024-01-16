package oop.ex6.variables;

import oop.ex6.main.CommonPatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper representing the variables available in a scope. In our implementation
 * we always have 2 live scopes - the parent scope to the current one, and the current
 * scope. Has helpers for retrieving the variable itself, checking if a name is in scope,
 * etc.
 */
public class VariableScope implements Cloneable {
    private static final String NEW_LINE = "\n";
    private HashMap<String, Variable> variables;

    /**
     * Create a new variable scope with no variables declared.
     */
    public VariableScope() {
        variables = new HashMap<String, Variable>();
    }

    /**
     * Add a variable to the scope
     *
     * @param variable the variable to add to the scope
     */
    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

    /**
     * Get a variable by name in the scope, returning null if the variable doesn't
     * exist in the scope
     *
     * @param name The variable name to lookup
     * @return The variable if it's in scope, or null if it isn't.
     */
    public Variable getVariableByName(String name) {
        return variables.get(name);
    }

    /**
     * Checks if the passed variable name is in scope.
     *
     * @param name The name of the variable
     * @return true if the variable is in scope, false otherwise
     */
    public boolean isVariableInScope(String name) {
        return getVariableByName(name) != null;
    }

    /**
     * Merge this scope with another scope, keeping variables introduced in the
     * current scope and adding ones from the parent scope. This is used to merge
     * for example the global + parameters scope when entering an if inside a function.
     *
     * @param parentScope The parent scope to merge with
     * @return The new merged scope
     */
    public VariableScope mergeWithScope(VariableScope parentScope) {
        for (Map.Entry<String, Variable> nameVariable : parentScope.variables.entrySet()) {
            Variable overwritingVariable = getVariableByName(nameVariable.getKey());
            if (overwritingVariable == null) {
                addVariable(nameVariable.getValue());
            }
        }
        return this;
    }

    /**
     * Creates a new variable scope from a list of variables representing method
     * parameters. This is used to add the method params to the method scope.
     *
     * @param params The list of parameters
     * @return The filled scope
     */
    public static VariableScope fromMethodParameters(ArrayList<Variable> params) {
        VariableScope paramsScope = new VariableScope();
        for (Variable param : params) {
            param.setInitialized();
            paramsScope.addVariable(param);
        }
        return paramsScope;
    }

    /**
     * A pretty representation of all the variables in the scope, for debugging
     * purposes.
     *
     * @return A string representation of the scope.
     */
    @Override
    public String toString() {
        String result = CommonPatterns.EMPTY_STRING;
        for (Map.Entry<String, Variable> nameVariable : variables.entrySet()) {
            result += nameVariable.getValue() + NEW_LINE;
        }
        return result;
    }

    /**
     * Perform a deep clone of the scope. sadly the regular hashmap clone
     * doesn't cut it, since we need to copy the inner variable as well.
     *
     * @return The cloned scope
     */
    public VariableScope clone() {
        VariableScope cloned = new VariableScope();
        for (Map.Entry<String, Variable> nameVariable : variables.entrySet()) {
            cloned.variables.put(nameVariable.getKey(), nameVariable.getValue().clone());
        }
        return cloned;
    }
}

/**
 * A simple helpers class, currently only has a helper for looking up
 * variable in 2 possible scopes.
 */
class ScopeHelpers {

    /**
     * Lookup a variable, first in the current scope, then in the parent scope.
     * If a variable is found in the current scope is returned, otherwise the
     * parent scope is looked up. If none is found, null is returned.
     *
     * @param name         The variable name to lookup
     * @param currentScope The local scope to look in
     * @param parentScope  The parent scope to look in
     * @return the variable that was found in one of the scopes, or null if none
     * was found.
     */
    public static Variable findRelevantVariable(String name, VariableScope currentScope,
                                                VariableScope parentScope) {
        Variable currentVariable = currentScope.getVariableByName(name);
        if (currentVariable != null)
            return currentVariable;
        return parentScope.getVariableByName(name);
    }
}
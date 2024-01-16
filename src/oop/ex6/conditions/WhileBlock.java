package oop.ex6.conditions;

import oop.ex6.variables.VariableVerifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * handles while block validity
 */
public class WhileBlock {
    private static final String WHILE_START = "^(\\s*while\\s*)([(]\\s*)";

    private static final Pattern whilePattern = Pattern.compile(WHILE_START);

    /**
     * check if the current line seems to start while block
     *
     * @param line string possibly starts a while block
     * @return true if it starts as while block, false otherwise
     */
    public static boolean isWhileBlockStart(String line) {
        Matcher whileMatcher = whilePattern.matcher(line);
        return whileMatcher.find();
    }

    /**
     * for a while statement check if the condition statements is valid
     *
     * @param line     to check if it is a while statement
     * @param verifier variable verifier that check for validity and right usage of a variable
     * @throws WhileException n case of invalidity throws while exception
     */
    public static void handleConditions(String line, VariableVerifier verifier) throws WhileException {
        try {
            ConditionBlocksParser.handleConditions(line, verifier, whilePattern);
        } catch (ConditionException exception) {
            throw new WhileException(exception.getMessage());
        }
    }
}



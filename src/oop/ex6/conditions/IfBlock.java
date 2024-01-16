package oop.ex6.conditions;

import oop.ex6.variables.VariableVerifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * handles if block validity
 */
public class IfBlock {
    //------private fields--------//
    private static final String IF_START = "^(\\s*if\\s*)([(]\\s*)";
    private static final Pattern ifStartPattern = Pattern.compile(IF_START);


    /**
     * check if the current line seems to be an if statement
     *
     * @param line string possibly starts an if block
     * @return true if it starts as if block, false otherwise
     */
    public static boolean isIfStart(String line) {
        Matcher ifStartMatcher = ifStartPattern.matcher(line);
        return ifStartMatcher.find();
    }


    /**
     * for an if statement check whether the condition statements is valid or nor
     *
     * @param line     to check if it is an if statement
     * @param verifier variable verifier that check for validity and right usage of a variable
     * @throws IfException in case of invalidity throws if exception
     */
    public static void handleConditions(String line, VariableVerifier verifier) throws IfException {
        try {
            ConditionBlocksParser.handleConditions(line, verifier, ifStartPattern);

        } catch (ConditionException exception) {
            throw new IfException(exception.getMessage());
        }
    }
}

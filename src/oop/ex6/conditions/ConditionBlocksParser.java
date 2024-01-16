package oop.ex6.conditions;

import oop.ex6.main.CommonPatterns;
import oop.ex6.variables.BaseVariableException;
import oop.ex6.variables.VariableVerifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class that parses the condition statement (if or while)
 */
public class ConditionBlocksParser {
    //------private fields--------//
    private static final String DOUBLE_PATTERN = "(([+\\-]((\\d+\\.?\\d*)|(\\d*\\.?\\d+)))|((\\d+\\.?\\d*)|" +
            "(\\d*\\.?\\d+)))";
    private static final String BOOLEAN_PATTERN = "((true|false|" + DOUBLE_PATTERN + "))";

    private static final String CONDITION =
            "(?<value>" + BOOLEAN_PATTERN + "|" + CommonPatterns.VALID_NAME + ")";
    private static final String OR_AND = "(^(\\s*(\\|\\|)\\s*|\\s*(&&)\\s*))";
    private static final String END_OF_STATEMENT = "^(\\s*[)]\\s*[{]\\s*$)";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final Pattern endPattern = Pattern.compile(END_OF_STATEMENT);
    private static final String ERROR_MSG = "Invalid statements";
    private static final Pattern conditionPattern = Pattern.compile(CONDITION);
    private static final Pattern andOr = Pattern.compile(OR_AND);
    private static String conditions;


    /**
     * for a condition statement ( if/while) check if the condition statements is valid
     *
     * @param line         a string of the condition statement
     * @param verifier     variable verifier - help to check variable validity
     * @param startPattern the start of the pattern of the statements (if or while)
     * @throws ConditionException in case of invalidity throws condition exception
     */
    public static void handleConditions(String line, VariableVerifier verifier, Pattern startPattern)
            throws ConditionException {
        Matcher startMatcher = startPattern.matcher(line);

        if (!startMatcher.find()) {
            throw new ConditionException(ERROR_MSG);
        } else {
            conditions = line.substring(startMatcher.end());
            parseConditionOp(verifier);
        }
    }

    /**
     * check the validity of a single condition in the conditions statements
     *
     * @param verifier verify the variable usage - type
     * @throws ConditionException in case of invalidity throws condition exception
     */
    private static void parseConditionOp(VariableVerifier verifier) throws ConditionException {
        if (!handleCondition(verifier)) {
            throw new ConditionException(ERROR_MSG);
        }
        while (handleOperatorCondition()) {
            if (!handleCondition(verifier)) {
                throw new ConditionException(ERROR_MSG);
            }
        }
        handleEnd();
    }

    /**
     * check the validity of the end of the line - ' ) { '
     *
     * @throws ConditionException in case of invalidity throws condition exception
     */
    private static void handleEnd() throws ConditionException {
        Matcher endMatcher = endPattern.matcher(conditions);
        if (!endMatcher.find()) {
            throw new ConditionException(ERROR_MSG);
        }
        conditions = conditions.substring(endMatcher.end()).strip();
        if (!conditions.equals(CommonPatterns.EMPTY_STRING)) {
            throw new ConditionException(ERROR_MSG);
        }
    }

    /**
     * check for the validity of condition
     *
     * @param verifier variable verifier - help to check variable validity
     * @return true if the current condition is valid, false otherwise
     * @throws ConditionException in case of invalidity throws condition exception
     */
    private static boolean handleCondition(VariableVerifier verifier) throws ConditionException {
        Matcher conditionMatcher = conditionPattern.matcher(conditions);
        boolean isMatch = conditionMatcher.lookingAt();
        if (isMatch) {
            String condition = conditions.substring(conditionMatcher.start(), conditionMatcher.end());
            try {
                verifier.verifyVariableUsage(BOOLEAN_TYPE, condition);
            } catch (BaseVariableException error) {
                throw new ConditionException(error.getMessage());
            }
            conditions = conditions.substring(conditionMatcher.end());
        }
        return isMatch;
    }


    /**
     * check for the validity of operator
     *
     * @return true if the current operator is valid, false otherwise
     */
    private static boolean handleOperatorCondition() {
        Matcher operatorMatcher = andOr.matcher(conditions);
        boolean isMatch = operatorMatcher.lookingAt();
        if (isMatch) {
            conditions = conditions.substring(operatorMatcher.end());
        }
        return isMatch;
    }
}

package oop.ex6.methods;


import oop.ex6.main.CommonPatterns;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse the parameters of method signature or method declaration
 */
public class ParseParameters {

    private static final String RIGHT_PARENTHESES = "(^\\s*[)]\\s*)";
    private static final String PARAMS_ERROR_MSG = "Invalid parameters";
    private static final Pattern patternRightParentheses = Pattern.compile(RIGHT_PARENTHESES);
    private static final String COMMA = ",";
    private ArrayList<String> parametersList;

    private String parameters;

    /**
     * parse the parameters in a given parameters string
     */
    public ParseParameters() {
        this.parametersList = new ArrayList<>();
    }

    /**
     * check for parameters validity
     *
     * @param parameters       string of the parameters to parse
     * @param paramComma       pattern in the middle of the string, parameter and comma
     * @param matcherMiddle    matcher of the first parameter comma
     * @param patternLastParam pattern of the last parameter
     * @param paramEnd         pattern of the end of the line (in our case will be '){' or ');'
     * @return array list of strings - of the parameters , if valid
     * @throws MethodParamsException in case of invalid parameters
     */

    public ArrayList<String> checkParamsValidity(String parameters, Pattern paramComma,
                                                 Matcher matcherMiddle,
                                                 Pattern patternLastParam, Pattern paramEnd) throws
            MethodParamsException {
        this.parameters = parameters;
        boolean isThereMiddleMatch = checkMiddleMatch(paramComma, matcherMiddle);
        Matcher lastParam = patternLastParam.matcher(this.parameters);
        Matcher matcherParenthesesEnd = patternRightParentheses.matcher(this.parameters);
        boolean lastParamExists = false;
        if (lastParam.find()) {
            lastParamExists = true;
            String param = this.parameters.substring(lastParam.start(), lastParam.end());
            param = param.replace(COMMA, CommonPatterns.EMPTY_STRING);
            parametersList.add(param.strip());
            this.parameters = this.parameters.substring(lastParam.end());
            matcherParenthesesEnd = patternRightParentheses.matcher(this.parameters);
        }
        handleRightParentheses(matcherParenthesesEnd);
        caseCommaNoLastParam(!lastParamExists && isThereMiddleMatch);
        handleEndOfLine(paramEnd);
        return parametersList;
    }

    /**
     * check for middle match
     *
     * @param paramComma    pattern in the middle of the string, parameter and comma
     * @param matcherMiddle matcher of the first parameter comma
     * @return true if there is match with paramComma pattern, false otherwise
     */
    private boolean checkMiddleMatch(Pattern paramComma, Matcher matcherMiddle) {
        boolean isThereMiddleMatch = false;
        while (matcherMiddle.find()) {
            isThereMiddleMatch = true;
            String param = this.parameters.substring(matcherMiddle.start(), matcherMiddle.end() - 1);
            param = param.strip().replace(COMMA, CommonPatterns.EMPTY_STRING);
            parametersList.add(param.strip());
            this.parameters = this.parameters.substring(matcherMiddle.end());
            matcherMiddle = paramComma.matcher(this.parameters);
        }
        return isThereMiddleMatch;
    }

    /**
     * parse end of line and throws exception in case of invalid end
     *
     * @param paramEnd pattern of end of line
     * @throws MethodParamsException in case of invalid end of line
     */

    private void handleEndOfLine(Pattern paramEnd) throws MethodParamsException {
        Matcher matcherEnd = paramEnd.matcher(this.parameters);
        if (!matcherEnd.find()) {
            throw new MethodParamsException(PARAMS_ERROR_MSG);
        }
        this.parameters = this.parameters.substring(matcherEnd.end());
        if (!this.parameters.strip().equals(CommonPatterns.EMPTY_STRING)) {
            throw new MethodParamsException(PARAMS_ERROR_MSG);
        }
    }

    /**
     * check for last parameter validity
     *
     * @param lastParamExists boolean that indicates whether there is parameter in the end of line (that is
     *                        only variable ith no comma)
     * @throws MethodParamsException in case of invalid parameters
     */

    private static void caseCommaNoLastParam(boolean lastParamExists) throws MethodParamsException {
        if (lastParamExists) {
            throw new MethodParamsException(PARAMS_ERROR_MSG);
        }
    }

    /**
     * check for right parentheses validity
     *
     * @param matcherParenthesesEnd matcher right parentheses
     * @throws MethodParamsException in case of invalid right parentheses
     */
    private void handleRightParentheses(Matcher matcherParenthesesEnd) throws MethodParamsException {
        boolean isMatchEnd = matcherParenthesesEnd.find();
        if (!isMatchEnd) {
            throw new MethodParamsException(PARAMS_ERROR_MSG);
        }
        this.parameters = this.parameters.substring(matcherParenthesesEnd.end());
    }
}

package oop.ex6.methods;

import oop.ex6.main.CommonPatterns;
import oop.ex6.variables.BaseVariableException;
import oop.ex6.variables.Variable;
import oop.ex6.variables.VariableVerifier;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class that handles method call - checks for its validity
 */
public class MethodCall {
    //------private fields--------//

    private static final String METHOD_CALL_END = "(^\\s*[;]\\s*)";
    private static final String METHOD_CALL_MIDDLE = "(\\s*(" + CommonPatterns.VALUE_PATTERN + ")\\s*[,])";
    private static final Pattern patternMethodName = Pattern.compile(CommonPatterns.METHOD_START);
    private static final Pattern paramComma = Pattern.compile(METHOD_CALL_MIDDLE);
    private static final Pattern paramEnd = Pattern.compile(METHOD_CALL_END);
    private static final Pattern patternLastParam = Pattern.compile(CommonPatterns.VALUE_PATTERN);
    private static final String METHOD_CALL_ERROR_MSG = "Invalid method call";
    private static ArrayList<String> givenValues;


    /**
     * @param parameters string of the argument the function receives
     * @param methodName the name of the method of the method signature
     * @param verifier   variable verifier that helps to declare if a usage of the variable is valid
     * @throws MethodCallException in case of parameters invalidity
     */
    private static void handleParameters(String parameters,
                                         String methodName, VariableVerifier verifier) throws
            MethodCallException {
        Matcher matcherMiddle = paramComma.matcher(parameters);
        ParseParameters parser = new ParseParameters();
        try {

            givenValues = parser.checkParamsValidity(parameters, paramComma, matcherMiddle,
                    patternLastParam, paramEnd);
        } catch (MethodParamsException paramsException) {
            throw new MethodCallException(paramsException.getMessage());

        }
        if (!MethodSignature.methodCallNameExist(methodName)) {
            throw new MethodCallException(METHOD_CALL_ERROR_MSG);
        }
        if (!isParamsMatchSignature(verifier, methodName)) {
            throw new MethodCallException(METHOD_CALL_ERROR_MSG);
        }
    }

    /**
     * check whether the line seems to be a method call
     *
     * @param line that possibly a method call
     * @return true if the line starts as method call, false otherwise
     */
    public static boolean isMethodCallStart(String line) {
        Matcher matcherMethodName = patternMethodName.matcher(line);
        if (matcherMethodName.find()) {
            String methodName =
                    line.substring(matcherMethodName.start(),
                            line.indexOf(CommonPatterns.LEFT_PARENTHESIS)).strip();
            return MethodSignature.methodCallNameExist(methodName);
        }
        return false;
    }

    /**
     * checks for the validity if a method call
     *
     * @param line     string to check whether it is a method call
     * @param verifier variable verifier that helps to declare if a usage of the variable is valid
     * @throws MethodCallException in case of method call invalidity
     */
    public static void handleMethodCall(String line, VariableVerifier verifier) throws MethodCallException {
        String parameters;
        Matcher matcherMethodName = patternMethodName.matcher(line);
        if (matcherMethodName.find()) {
            String methodName =
                    line.substring(matcherMethodName.start(),
                            line.indexOf(CommonPatterns.LEFT_PARENTHESIS)).strip();
            parameters = line.substring(matcherMethodName.end());
            handleParameters(parameters, methodName, verifier);
        } else {
            throw new MethodCallException(METHOD_CALL_ERROR_MSG);
        }
    }

    /**
     * checks if the parameters match the signature
     *
     * @param verifier   variable verifier that helps to declare if a usage of the variable is valid
     * @param methodName the name of the method
     * @return true if the parameters match the signature, false otherwise
     * @throws MethodCallException in case that the parameters doesn't match the signature
     */
    public static boolean isParamsMatchSignature(VariableVerifier verifier, String methodName) throws
            MethodCallException {
        ArrayList<Variable> vars = MethodSignature.getSignatureVariables().get(methodName);
        if (givenValues.size() != vars.size()) {
            throw new MethodCallException(METHOD_CALL_ERROR_MSG);
        }
        int size = givenValues.size();
        for (int i = 0; i < size; i++) {
            try {
                verifier.verifyVariableUsage(vars.get(i).getType(), givenValues.get(i));
            } catch (BaseVariableException baseVariableException) {
                throw new MethodCallException(baseVariableException.getMessage());
            }
        }
        givenValues.clear();
        return true;
    }
}

package oop.ex6.methods;

import oop.ex6.main.CommonPatterns;
import oop.ex6.variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class that handles method signature - checks for its validity
 */
public class MethodSignature {
    //------private fields--------//

    private static final String VOID = "(^\\s*(void)\\s+)";
    private static final String VALID_VARIABLE_NAME = "([a-zA-Z\\d]+)|([_][a-zA-Z\\d_]+)";
    private static final String METHOD_DECLARE_END = "(^\\s*[{]\\s*)";
    private static final String LAST_PARAM =
            "^(\\s*(final\\s+)*(" + CommonPatterns.VALID_TYPE_PATTERN + "\\s+" + VALID_VARIABLE_NAME + ")" +
                    "\\s*)";
    private static final String METHOD_DECLARE_MIDDLE =
            "^(\\s*(final\\s+)*(" + CommonPatterns.VALID_TYPE_PATTERN + "\\s+" + VALID_VARIABLE_NAME + ")" +
                    "\\s*[,])";
    private static final String METHOD_DECLARATION_ERROR_MSG = "Invalid method signature";
    private static final Pattern patternMiddle = Pattern.compile(METHOD_DECLARE_MIDDLE);
    private static final Pattern patternStart = Pattern.compile(CommonPatterns.METHOD_START);
    private static final Pattern voidPattern = Pattern.compile(VOID);
    private static final Pattern patternEnd = Pattern.compile(METHOD_DECLARE_END);
    private static final Pattern patternLastParam = Pattern.compile(LAST_PARAM);
    private static final String FINAL = "final ";
    private static final String SPACE = " ";
    private static HashMap<String, ArrayList<Variable>> signatureVariables = new HashMap<>();

    /**
     * check if a given string seems to be a method string
     *
     * @param methodSignature string that possibly a method signature
     * @return true if the line starts as method signature, false otherwise
     */
    public static boolean isMethodSignatureStart(String methodSignature) {
        Matcher voidMatcher = voidPattern.matcher(methodSignature);
        return voidMatcher.find();
    }

    /**
     * @param methodSignature string to check if it is a valid method signature
     * @return an array list of variables that declare in that method signature
     * @throws MethodDeclarationException in case of method signature invalidity
     */
    public static ArrayList<Variable> isMethodSignatureLegal(String methodSignature) throws
            MethodDeclarationException {
        Matcher voidMatcher = voidPattern.matcher(methodSignature);
        boolean isReturnValValid = voidMatcher.find();
        if (!isReturnValValid) {
            throw new MethodDeclarationException(METHOD_DECLARATION_ERROR_MSG);
        } else {
            return handleMethodDeclaration(methodSignature.substring(voidMatcher.end()));
        }
    }


    /**
     * parse the parameters and insert them to arraylist of variables, return them if they are valid,
     * otherwise throws MethodDeclarationException
     *
     * @param parameters    string of the argument the function receives
     * @param matcherMiddle matcher of parameters with comma (not including the last parameter)
     * @param methodName    the name of the method of the method signature
     * @return an array list of variables that declare in that method signature
     * @throws MethodDeclarationException in case of Invalid parameter
     */
    private static ArrayList<Variable> handleParameters(String parameters,
                                                        Matcher matcherMiddle,
                                                        String methodName) throws MethodDeclarationException {
        ParseParameters parser = new ParseParameters();
        try {
            ArrayList<String> paramsList = parser.checkParamsValidity(parameters, patternMiddle,
                    matcherMiddle,
                    patternLastParam, patternEnd);
            ArrayList<Variable> vars = getSignatureVariables(paramsList);
            signatureVariables.put(methodName, vars);
            return vars;
        } catch (MethodParamsException paramsException) {
            throw new MethodDeclarationException(paramsException.getMessage());
        }

    }


    /**
     * @param line method signature to check for its validity
     * @return arraylist of variables if valid
     * @throws MethodDeclarationException in case of invalid method call
     */
    private static ArrayList<Variable> handleMethodDeclaration(String line) throws
            MethodDeclarationException {
        String parameters;
        Matcher matcherMethodName = patternStart.matcher(line);
        if (matcherMethodName.find()) {
            String methodName =
                    line.substring(matcherMethodName.start(),
                            line.indexOf(CommonPatterns.LEFT_PARENTHESIS)).strip();
            parameters = line.substring(matcherMethodName.end());
            Matcher matcherMiddle = patternMiddle.matcher(parameters);
            return handleParameters(parameters, matcherMiddle, methodName);
        }
        throw new MethodDeclarationException(METHOD_DECLARATION_ERROR_MSG);
    }


    /**
     * check if method call exists
     *
     * @param methodName method name
     * @return true if given method name was declared, false otherwise
     */
    public static boolean methodCallNameExist(String methodName) {
        return signatureVariables.containsKey(methodName);
    }

    /**
     * gets the signature variables
     *
     * @param params method signature to check for its validity
     * @return array list of the variable from the signature
     */
    public static ArrayList<Variable> getSignatureVariables(ArrayList<String> params) throws
            MethodDeclarationException {
        ArrayList<Variable> varList = new ArrayList<>();
        // Ensure there's no duplicate name declared
        HashSet<String> seenVariableNames = new HashSet<>();
        for (String param : params) {
            boolean isFinal = param.strip().startsWith(FINAL);
            String currentVar = param.strip();
            String type, name;
            if (isFinal) {
                currentVar = currentVar.replace(FINAL, CommonPatterns.EMPTY_STRING).strip();
            }
            type = currentVar.substring(0, param.indexOf(SPACE));
            name = currentVar.replace(type, CommonPatterns.EMPTY_STRING).strip();
            if (!seenVariableNames.add(name)) {
                throw new MethodDeclarationException(METHOD_DECLARATION_ERROR_MSG);
            }
            Variable variable = new Variable(type, name, isFinal);
            varList.add(variable);
        }
        return varList;
    }


    public static HashMap<String, ArrayList<Variable>> getSignatureVariables() {
        return signatureVariables;
    }
}


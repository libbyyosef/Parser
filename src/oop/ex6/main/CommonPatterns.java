package oop.ex6.main;

/**
 * class of common pattens
 */
public class CommonPatterns {
    public static final String VALID_TYPE_PATTERN = "(?<type>int|double|String|boolean|char)";
    public static final String VALID_NAME = "(([a-zA-Z][a-zA-Z\\d_]*)|([_][a-zA-Z\\d_]+))";
    public static final String INTEGER_PATTERN = "(([+\\-]\\d+)|(\\d+))";
    public static final String DOUBLE_PATTERN =
            "(([+\\-]((\\d+\\.?\\d*)|(\\d*\\.?\\d+)))|((\\d+\\.?\\d*)|(\\d*\\.?\\d+)))";
    public static final String CHAR_PATTERN = "('[^\\\\'\",]')";
    public static final String STRING_PATTERN = "(\"[^\\\\'\",]*\")";
    public static final String BOOLEAN_PATTERN = "((true|false|" + CommonPatterns.DOUBLE_PATTERN + "))";
    public static final String VALUE_PATTERN =
            "(?<value>" + CommonPatterns.DOUBLE_PATTERN + "|" + CommonPatterns.CHAR_PATTERN + "|"
                    + CommonPatterns.STRING_PATTERN
                    + "|" + CommonPatterns.INTEGER_PATTERN + "|" + BOOLEAN_PATTERN + "|"
                    + CommonPatterns.VALID_NAME + ")";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String METHOD_NAME = "^\\s*(?<methodName>[a-zA-Z][a-zA-Z\\d_]*)";
    public static final String METHOD_START = CommonPatterns.METHOD_NAME + "\\s*[(]";
    public static final String EMPTY_STRING = "";


}

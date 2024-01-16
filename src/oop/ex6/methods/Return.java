package oop.ex6.methods;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * handle return statement
 */
public class Return {
    private static final String RETURN = "^\\s*return\\s*;\\s*$";
    private static final Pattern returnPattern = Pattern.compile(RETURN);

    /**
     * check if return statment
     *
     * @param line string to check if return statement
     * @return true if it is, false otherwise
     */
    public static boolean isReturn(String line) {
        Matcher returnMatcher = returnPattern.matcher(line);
        return returnMatcher.find();
    }

}

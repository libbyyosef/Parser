package oop.ex6.main;


import oop.ex6.conditions.IfBlock;
import oop.ex6.conditions.WhileBlock;
import oop.ex6.methods.*;
import oop.ex6.variables.*;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * An exception thrown when the {} are unbalanced - whether we have an
 * open { without a closing }, or an } without a matching {.
 */
class BracketsBalanceException extends ParserException {
    private static final String ERROR_MESSAGE = "The {} brackets aren't balanced";

    public BracketsBalanceException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception thrown when trying to declare a method inside another method.
 */
class NestedMethodException extends ParserException {
    private static final String ERROR_MESSAGE = "Invalid nested method";

    public NestedMethodException() {
        super(ERROR_MESSAGE);
    }
}

/**
 * An exception thrown when a method doesn't end with a return.
 */
class NoReturnInMethodEnd extends ParserException {
    private static final String ERROR_MESSAGE = "Missing return in method end";

    public NoReturnInMethodEnd() {
        super(ERROR_MESSAGE);
    }
}

/**
 * The main parsing class, called by Main.
 * It's connecting all the different parsing components:
 * Variables, Method declarations, Method calls, If/While, Ignoring comments
 * and whitespace.
 * Parse is the main parsing API, throwing an error indicating a parse error,
 * indicating a valid code file if no error is thrown.
 */
public class Parser {
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
    private static final Pattern COMMENT_LINE = Pattern.compile("^//.*$");
    private static final Pattern BAD_COMMENT_LINE = Pattern.compile("^.+//$");
    private static final String BLOCK_BODY_LINE_END = ";";
    private static final String SCOPE_START_LINE_END = "{", SCOPE_END_LINE_END = "}";
    private static final String IO_ERROR_MESSAGE = "An error occured when trying to read the file, exiting.";
    private static final String INVALID_LINE_ERROR_MESSAGE = "Invalid line - not a valid code line or " +
            "comment";
    private static final String GLOBAL_METHOD_CALL_ERROR = "Calling a method from the global scope is " +
            "undefined";
    private static final int ONE_LEVEL_DEEP_NESTING = 1;

    // parentScope - the scope just above what we are currently declaring and using.
    // currentScope - the current working scope
    // globalScope - the scope of globals. This is also the initial parentScope,
    // and the parentScope is restored to it when returning from a function.
    private VariableScope parentScope, currentScope, globalScope;
    private String inputFilename;
    private String prevLine;
    private ArrayList<String> allLines;
    // Used for verifying that every { has a matching } and vice versa.
    private LinkedList<String> bracketStack;
    // The stack of scopes. Pushed to when entering a new scope on '{',
    // and popped when exiting a scope.
    private LinkedList<VariableScope> scopeStack;
    // The currently used variable verifier, used by (almost) all parsing components
    // to check validity of variables and types.
    private VariableVerifier subroutineVerifier;

    /**
     * Create a new parser
     *
     * @param inputFilename the filename of the file to parse.
     */
    public Parser(String inputFilename) {
        this.inputFilename = inputFilename;
        parentScope = new VariableScope();
        allLines = new ArrayList<>();
        bracketStack = new LinkedList<>();
        scopeStack = new LinkedList<>();
    }

    /**
     * Parse the file.
     *
     * @throws ParserException In case of a parsing error in any of the components.
     */
    public void parse() throws ParserException {
        try {
            readCodeLines();
        } catch (IOException error) {
            throw new ParserException(IO_ERROR_MESSAGE, ParserException.IO_ERROR);
        }
        globalsMethodsPass();
        verifyPass();
    }

    /**
     * Read all the file lines, to be used later in the 2 parsing passes.
     *
     * @throws IOException In case an I/O exception occured when reading the file
     */
    private void readCodeLines() throws IOException {
        File inputFile = new File(inputFilename);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = reader.readLine()) != null) {
            allLines.add(line);
        }
    }

    /***
     * We first want to populate the global variables scope and the methods
     * table, as these can be used everywhere without relevance to ordering.
     * So we do this initial first pass before parsing the bodies of subroutines.
     * This pass also checks balancing - throws an error if the {} aren't balanced,
     * and that no methods calls happen on the global scope.
     */
    private void globalsMethodsPass() throws ParserException {
        VariableVerifier globalVariableVerifier = new VariableVerifier(parentScope, parentScope);
        for (String line : allLines) {
            if (isEmptyOrComment(line))
                continue;
            line = line.trim();
            if (inGlobalScope()) {
                if (MethodSignature.isMethodSignatureStart(line)) {
                    MethodSignature.isMethodSignatureLegal(line);
                }
                if (line.endsWith(BLOCK_BODY_LINE_END)) {
                    // Can't call methods in the global scope
                    if (MethodCall.isMethodCallStart(line)) {
                        throw new MethodCallException(GLOBAL_METHOD_CALL_ERROR);
                    }
                    VariableParser.parseVariablesLine(line, globalVariableVerifier, parentScope);
                }
            }
            if (line.endsWith(SCOPE_START_LINE_END)) {
                onNewScopeBracket();
            } else if (line.endsWith(SCOPE_END_LINE_END)) {
                if (!verifyScopeCloseBracket()) {
                    throw new BracketsBalanceException();
                }
            }
        }
        globalScope = parentScope.clone();
    }

    /**
     * Called when encountering a '{', pushing it to the bracket stack.
     */
    private void onNewScopeBracket() {
        bracketStack.push(SCOPE_START_LINE_END);
    }

    /**
     * Called when encountering a '}', also verifying that the brackets are matching
     *
     * @return true if this '}' has a matching '{', false otherwise
     */
    private boolean verifyScopeCloseBracket() {
        if (bracketStack.peek() == SCOPE_START_LINE_END) {
            bracketStack.pop();
            return true;
        }
        return false;
    }

    /**
     * Returns true if we're currently in the global scope - trivially
     * done by checking if the bracket stack is empty.
     *
     * @return true if we're currently in the global scope, false otherwise
     */
    private boolean inGlobalScope() {
        return bracketStack.isEmpty();
    }

    /**
     * Returns true if we're at least two levels deep in the nesting stack,
     * false otherwise. Used to check that if we haven't declared a method call
     * inside another.
     *
     * @return true if we're at least 2 levels deep, false otherwise.
     */
    private boolean inNestedScope() {
        return bracketStack.size() > ONE_LEVEL_DEEP_NESTING;
    }

    /**
     * The main pass, verifying the validity of the code. Done after the
     * globals pass so that the methods and globals are populated and ready
     * for the subroutines themselves.
     *
     * @throws ParserException if we've encountered an error parsing any line,
     *                         or when we finish the program but we still have an unterminated '{'.
     */
    private void verifyPass() throws ParserException {
        currentScope = new VariableScope();
        subroutineVerifier = new VariableVerifier(parentScope, currentScope);
        for (String line : allLines) {
            if (BAD_COMMENT_LINE.matcher(line).find()) {
                throw new ParserException(INVALID_LINE_ERROR_MESSAGE);
            }
            if (!isEmptyOrComment(line)) {
                line = line.trim();
                parseLine(line);
            }
            prevLine = line;
        }
        // When we finished parsing we're still inside a method, so throw an unterminated error
        if (!inGlobalScope()) {
            throw new BracketsBalanceException();
        }
    }

    /**
     * Parse and validate a single code line.
     *
     * @param line The line to parse and validate
     * @throws ParserException Thrown if the line is invalid
     */
    private void parseLine(String line) throws ParserException {
        if (line.endsWith(BLOCK_BODY_LINE_END)) {
            // Don't reparse global scope ";" lines
            if (!inGlobalScope())
                parseBlockBodyLine(line);
        } else if (line.endsWith(SCOPE_START_LINE_END)) {
            parseScopeStartLine(line);
        } else if (line.equals(SCOPE_END_LINE_END)) {
            parseScopeExitLine(line);
        } else {
            throw new ParserException(INVALID_LINE_ERROR_MESSAGE);
        }
    }

    /**
     * Parse a line that ends with a ";", aka not a line that starts
     * or ends a block. The possible lines are variable assignment/declaration,
     * method call, or return.
     *
     * @param line The line to parse and validate
     * @throws ParserException If the line is invalid
     */
    private void parseBlockBodyLine(String line) throws ParserException {
        if (MethodCall.isMethodCallStart(line)) {
            MethodCall.handleMethodCall(line, subroutineVerifier);
        } else if (Return.isReturn(line)) {
            // Not really handled here, see scope end.
        } else {
            // Not method call or return - must be a variable line
            VariableParser.parseVariablesLine(line, subroutineVerifier, currentScope);
        }
    }

    /**
     * Parse a line that starts a new scope - a while, if or a method
     * declaration
     *
     * @param line The line to parse
     * @throws ParserException Thrown if the line is invalid
     */
    private void parseScopeStartLine(String line) throws ParserException {
        enterScope();
        if (IfBlock.isIfStart(line)) {
            IfBlock.handleConditions(line, subroutineVerifier);
        } else if (WhileBlock.isWhileBlockStart(line)) {
            WhileBlock.handleConditions(line, subroutineVerifier);
        } else if (MethodSignature.isMethodSignatureStart(line)) {
            // Ensure that we're not already in a method
            ArrayList<Variable> parameters = MethodSignature.isMethodSignatureLegal(line);
            if (inNestedScope()) {
                throw new NestedMethodException();
            }
            currentScope.mergeWithScope(VariableScope.fromMethodParameters(parameters));
        } else {
            throw new ParserException(INVALID_LINE_ERROR_MESSAGE);
        }
    }

    /**
     * Parse a scope close line, aka '}'. Just exits the scope, validating that the
     * brackets are indeed balanced, and that if we've exited a method we had a return
     * as the previous line
     *
     * @param line The line to parse
     * @throws BracketsBalanceException Thrown In case we have a '}' without a matching '{'.
     * @throws NoReturnInMethodEnd      Thrown if we don't have a return in the end of the method.
     */
    private void parseScopeExitLine(String line) throws BracketsBalanceException, NoReturnInMethodEnd {
        exitScope();
        // If we've just exited to the global scope, it means we've exited a method.
        // so we check that the last line we encountered was a return.
        if (inGlobalScope() && !Return.isReturn(prevLine)) {
            throw new NoReturnInMethodEnd();
        }
    }

    /**
     * Called when entering a scope - pushes the bracket to the stack,
     * merging the parent scope with the current one, creating a new scope
     * and updating the verifier with the fresh scope information.
     */
    private void enterScope() {
        onNewScopeBracket();
        // Merge the parent scope with the current scope, making it the new parent scope,
        // and create a new current scope. Save the parent scope to restore at the scope
        // end.
        parentScope = currentScope.mergeWithScope(parentScope);
        scopeStack.push(parentScope);
        currentScope = new VariableScope();
        subroutineVerifier.updateScopes(parentScope, currentScope);
    }

    /**
     * Exit the scope - popping the brackets stack, popping the last parent
     * scope from the stack making it the new parent, and updating the
     * verifier with the fresh scope info.
     * When exiting from a method, the parent scope is the global scope - to
     * throw away modifications to the scope done in the method body.
     *
     * @throws BracketsBalanceException Thrown In case we have a '}' without a matching '{'.
     */
    private void exitScope() throws BracketsBalanceException {
        if (!verifyScopeCloseBracket())
            throw new BracketsBalanceException();
        // Clear what was added to the scope we're just exiting, and restore the parent scope
        currentScope = new VariableScope();
        parentScope = scopeStack.pop();
        // Discard modifications done by the subroutine to the global scope by
        // restoring the global
        if (inGlobalScope()) {
            parentScope = globalScope.clone();
        }
        subroutineVerifier.updateScopes(parentScope, currentScope);
    }

    /**
     * A simple helper for checking if a line has only spaces or it's a comment,
     * indicating we should skip it when parsing
     *
     * @param line The line to check
     * @return true if the line has only spaces or it's a comment
     */
    private boolean isEmptyOrComment(String line) {
        Matcher emptyLineMatcher = EMPTY_LINE.matcher(line);
        Matcher commentLineMatcher = COMMENT_LINE.matcher(line);
        return emptyLineMatcher.matches() || commentLineMatcher.matches();
    }
}
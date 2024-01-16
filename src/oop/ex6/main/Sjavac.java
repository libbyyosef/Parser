package oop.ex6.main;

import java.io.File;
import java.io.IOException;

public class Sjavac {
    private static final int SUCCESS_CODE = 0;

    public static void main(String[] args) {
        try {
            Parser parser = new Parser(args[0]);
            parser.parse();
            System.out.println(SUCCESS_CODE);
        } catch (ParserException parserException) {
            System.err.println(parserException.getMessage());
            System.out.println(parserException.getExceptionCode());
        }
    }
}
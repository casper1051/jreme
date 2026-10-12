package jreme;

public class Types {
    public static int expression = 0;
    public static int string = 1;

    public static int bool = 2;

    public static int integer = 3;

    public static int doub = 4;

    public static int multiline = 5;

    public static int symbol = 6;

    public static int function = 7;

    public static int error = 8;

    public static String toString(int type) {
        return switch(type) {
            case 0 -> "EXPRESSION";
            case 1 -> "STRING";
            case 2 -> "BOOL";
            case 3 -> "INT";
            case 4 -> "DOUBLE";
            case 5 -> "MULTILINE";
            case 6 -> "SYMBOL";
            case 7 -> "FUNCTION";
            case 8 -> "ERROR";
            default -> "UNKNOWN";
        };
    }
}

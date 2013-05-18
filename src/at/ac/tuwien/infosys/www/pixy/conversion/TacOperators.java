package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacOperators {

    public static final int PLUS = 0;
    public static final int IS_SMALLER = 1;
    public static final int IS_EQUAL = 2;
    public static final int IS_NOT_EQUAL = 3;
    public static final int MINUS = 4;
    public static final int MULT = 5;
    public static final int DIV = 6;
    public static final int MODULO = 7;
    public static final int SL = 8;
    public static final int SR = 9;
    public static final int IS_IDENTICAL = 10;
    public static final int IS_NOT_IDENTICAL = 11;
    public static final int IS_SMALLER_OR_EQUAL = 12;
    public static final int IS_GREATER = 13;
    public static final int IS_GREATER_OR_EQUAL = 14;
    public static final int NOT = 15;
    public static final int BITWISE_NOT = 16;
    public static final int CONCAT = 17;
    public static final int BITWISE_OR = 18;
    public static final int BITWISE_AND = 19;
    public static final int BITWISE_XOR = 20;
    public static final int INT_CAST = 21;
    public static final int DOUBLE_CAST = 22;
    public static final int STRING_CAST = 23;
    public static final int ARRAY_CAST = 24;
    public static final int OBJECT_CAST = 25;
    public static final int BOOL_CAST = 26;
    public static final int UNSET_CAST = 27;
    public static final int BOOLEAN_AND = 28;

    private static String[] opToName;

    private static Map<String, Integer> nameToOp;

    static {
        nameToOp = new HashMap<>();
        opToName = new String[29];

        add("+", PLUS);
        add("<", IS_SMALLER);
        add("==", IS_EQUAL);
        add("!=", IS_NOT_EQUAL);
        add("-", MINUS);
        add("*", MULT);
        add("/", DIV);
        add("%", MODULO);
        add("<<", SL);
        add(">>", SR);
        add("===", IS_IDENTICAL);

        add("!==", IS_NOT_IDENTICAL);
        add("<=", IS_SMALLER_OR_EQUAL);
        add(">", IS_GREATER);
        add(">=", IS_GREATER_OR_EQUAL);
        add("!", NOT);
        add("~", BITWISE_NOT);
        add(".", CONCAT);
        add("|", BITWISE_OR);
        add("&", BITWISE_AND);
        add("^", BITWISE_XOR);

        add("(int)", INT_CAST);
        add("(double)", DOUBLE_CAST);
        add("(string)", STRING_CAST);
        add("(array)", ARRAY_CAST);
        add("(object)", OBJECT_CAST);
        add("(bool)", BOOL_CAST);
        add("(unset)", UNSET_CAST);
        add("&&", BOOLEAN_AND);
    }

    private static void add(String name, int num) {
        opToName[num] = name;
        nameToOp.put(name, num);
    }

    private TacOperators() {
    }

    public static String opToName(int op) {
        return opToName[op];
    }

    public static boolean isOp(String s) {
        return nameToOp.containsKey(s);
    }
}
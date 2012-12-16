package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;


public class Literal 
extends TacPlace {

    public static final Literal TRUE = new Literal("_true");
    public static final Literal FALSE = new Literal("_false");
    public static final Literal NULL = new Literal("_null");
    // top element of Literals lattice
    public static final Literal TOP = new Literal("_top");

    private String literal;

    // pattern for string to number conversion (see PHP manual, "Floating point numbers")
    private static Pattern strtod = Pattern.compile("\\s*[+-]?((\\d+(\\.\\d*)?)|\\d*\\.\\d+)([eE][+-]?\\d+)?");

    // EFF: caching for getXyValue methods;
    
// *********************************************************************************
// CONSTRUCTORS ********************************************************************    
// *********************************************************************************

    public Literal(String literal) {
        this(literal, true);
    }
    
    // "trim": trim single/double quotes from the ends?
    public Literal(String literal, boolean trim) {

        boolean in_double_quotes = false;
        boolean in_single_quotes = false;
        
        if (literal.length() > 1) {
            if ((literal.charAt(0) == '"' && literal.charAt(literal.length()-1) == '"')) {
                in_double_quotes = true;
            } else if (literal.charAt(0) == '\'' && literal.charAt(literal.length()-1) == '\'') {
                in_single_quotes = true;
            }
        }
        
        if (trim && (in_double_quotes || in_single_quotes)) {
            // strip double / single quotes
            literal = literal.substring(1, literal.length() - 1);
        }

        // handle backslash escapes if inside single quotes; 
        // currently handled:
        // \\ -> \
        // \' -> '
        if (in_single_quotes && literal.contains("\\")) {
            
            StringBuilder buf = new StringBuilder();
            Integer backSlash = literal.indexOf('\\');
            buf.append(literal.substring(0, backSlash));
            
            while (backSlash != null) {
                
                char escapedCharOld = literal.charAt(backSlash+1);
                String escapedResult;
                switch (escapedCharOld) {
                case '\\':
                    escapedResult = "\\";
                    break;
                case '\'':
                    escapedResult = "'";
                    break;
                default:
                    escapedResult = "\\" + escapedCharOld;
                }
                buf.append(escapedResult);
                
                int nextBackSlash = literal.indexOf('\\', backSlash+2);
                if (nextBackSlash == -1) {
                    buf.append(literal.substring(backSlash+2));
                    backSlash = null;
                } else {
                    buf.append(literal.substring(backSlash+2, nextBackSlash));
                    backSlash = nextBackSlash;
                }
            }
            
            literal = buf.toString();
        }

        // handle backslash escapes if inside double quotes; 
        // currently handled:
        // \\ -> \
        // \$ -> $
        // \" -> "
        else if (in_double_quotes && literal.contains("\\")) {
            
            StringBuilder buf = new StringBuilder();
            Integer backSlash = literal.indexOf('\\');
            buf.append(literal.substring(0, backSlash));
            
            while (backSlash != null) {
                
                char escapedCharOld = literal.charAt(backSlash+1);
                String escapedResult;
                switch (escapedCharOld) {
                case '\\':
                    escapedResult = "\\";
                    break;
                case '$':
                    escapedResult = "$";
                    break;
                case '"':
                    escapedResult = "\"";
                    break;
                default:
                    escapedResult = "\\" + escapedCharOld;
                }
                buf.append(escapedResult);
                
                int nextBackSlash = literal.indexOf('\\', backSlash+2);
                if (nextBackSlash == -1) {
                    buf.append(literal.substring(backSlash+2));
                    backSlash = null;
                } else {
                    buf.append(literal.substring(backSlash+2, nextBackSlash));
                    backSlash = nextBackSlash;
                }
            }
            
            literal = buf.toString();
        }
        
        this.literal = literal;
    }
    
// *********************************************************************************
// GET *****************************************************************************    
// *********************************************************************************

// toString ************************************************************************
    
    public String toString() {
        return this.literal;
    }
    
// getBoolValueLiteral *************************************************************
    
    // returns Literal.TRUE, Literal.FALSE or Literal.TOP (if the boolean value 
    // can't be determined)
    public Literal getBoolValueLiteral() {
        
        if (this == Literal.TOP) {
            return Literal.TOP;
        }
        if (this == Literal.TRUE) {
            return Literal.TRUE;
        }
        if (this == Literal.FALSE      ||
            this == Literal.NULL       ||
            this.literal.length() == 0 ||
            this.literal.equals("0")) {

            return Literal.FALSE;
        }
        
        // if I am completely numeric and my float value is 0, I could 
        // evaluate to both true and false,
        // depending on my internal PHP type (string or float), which is
        // not tracked (would require an additional data-flow analysis...);
        // examples:
        // 0.0   <-> false
        // "0.0" <-> true
        if (this.isCompletelyNumeric() && this.getFloatValue() == 0) {
            return Literal.TOP;
        }

        return Literal.TRUE;
    }
    
// isCompletelyNumeric *************************************************************
    
    // returns true if the contained literal is completely numeric 
    // (e.g. contains no trailing characters) or is a special literal
    // that can be converted to a numeric value (NULL, TRUE, FALSE)
    boolean isCompletelyNumeric() {
        if (this == Literal.NULL || this == Literal.TRUE || this == Literal.FALSE) {
            return true;
        }
        try {
            Float.parseFloat(this.literal);
            return true;
        } catch (NumberFormatException e) {
            // not completely numeric
            return false;
        }
    }
    
// getFloatValue *******************************************************************

    // assumes that this Literal is not Literal.TOP, so check that before the call
    public float getFloatValue() {

        if (this == Literal.TRUE) {
            return 1;
        }
        if (this == Literal.FALSE || this == Literal.NULL) {
            return 0;
        }
        if (this == Literal.TOP) {
            throw new RuntimeException("SNH");
        }

        Matcher matcher = Literal.strtod.matcher(this.literal);
        
        if (matcher.find()) {
            String prefix = this.literal.substring(0, matcher.end());
            return Float.parseFloat(prefix);
        } else {
            // System.out.println("no match for " + this.literal);
            return 0;
        }
    }

//  getIntValue ********************************************************************

    // analogous to getFloatValue() 
    int getIntValue() {
        return (int) this.getFloatValue();
    }
    
// getFloatValueLiteral ************************************************************
    
    public Literal getFloatValueLiteral() {

        if (this == Literal.TOP) {
            return Literal.TOP;
        }
        // these checks will be performed again by getFloatValue(),
        // but skipping them here would result in "1.0" instead of "1"
        // for true (and analogous for false) since the conversion number->string
        // is not mimiced perfectly
        if (this == Literal.TRUE) {
            return new Literal("1");
        }
        if (this == Literal.FALSE || this == Literal.NULL) {
            return new Literal("0");
        }
        
        return new Literal(Literal.numberToString(this.getFloatValue()));
    }
    
// getIntValueLiteral **************************************************************
    
    // analogous to getFloatValueLiteral()
    public Literal getIntValueLiteral() {
        
        if (this == Literal.TOP) {
            return Literal.TOP;
        }
        return new Literal(Literal.numberToString(this.getIntValue()));
    }
    
// getStringValueLiteral ***********************************************************
    
    public Literal getStringValueLiteral() {
        if (this == Literal.TOP) {
            return Literal.TOP;
        }
        return new Literal(this.getStringValue());
    }
    
// getStringValue ******************************************************************
    
    // assumes that this Literal is not Literal.TOP, so check that before the call
    public String getStringValue() {
        if (this == Literal.TRUE) {
            return "1";
        }
        if (this == Literal.FALSE || this == Literal.NULL) {
            return "";
        }
        return this.literal;
    }
    
// numberToString ******************************************************************
    
    // LATER: better simulation of PHP's conversion of number to string;
    // don't use Float.toString(); tedious because of missing specification
    public static String numberToString(float in) {
        return Float.toString(in);
    }
    
    static String numberToString(int in) {
        return Integer.toString(in);
    }
    
// isSmallerLiteral ****************************************************************
    
    // tests if left < right and returns the resulting literal (can also be
    // Literal.TOP in case of unspecified semantics)
    public static Literal isSmallerLiteral(Literal left, Literal right, CfgNode cfgNode) {
        // if both operands are completely numeric, comparing them
        // is straightforward
        if (left.isCompletelyNumeric() && right.isCompletelyNumeric()) {
            if (left.getFloatValue() < right.getFloatValue()) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        } else {
            // fuzzy explanations in the PHP manual and contradictions
            // to observed behavior
            /*
            throw new RuntimeException("Unspecified PHP semantics, line " + 
                    cfgNode.getOrigLineno());*/
            return Literal.TOP;
        }
    }

// isGreaterLiteral ****************************************************************
    
    // tests if left > right and returns the resulting literal;
    // analogous to isSmallerLiteral
    public static Literal isGreaterLiteral(Literal left, Literal right, CfgNode cfgNode) {
        if (left.isCompletelyNumeric() && right.isCompletelyNumeric()) {
            if (left.getFloatValue() > right.getFloatValue()) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        } else {
            /*
            System.out.println(left + " > " + right);
            throw new RuntimeException(
                "Unspecified PHP semantics, line " + cfgNode.getOrigLineno());*/
            return Literal.TOP;
            
        }
    }

// isEqualLiteral ******************************************************************
    
    // tests the two operands for loose equality and returns the resulting literal
    public static Literal isEqualLiteral(Literal left, Literal right) {
        
        // check this before
        if (left == Literal.TOP || right == Literal.TOP) {
            throw new RuntimeException("SNH");
        }
        
        // if at least one of the operands is boolean 
        if (left.isBool() || right.isBool()) {
            if (left.getBoolValueLiteral() == right.getBoolValueLiteral()) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        } 
        
        // if the left or the right operand is NULL:
        if (left == Literal.NULL) {
            return right.isEqualToNullLiteral();
        }
        if (right == Literal.NULL) {
            return left.isEqualToNullLiteral();
        }
        
        // both operands are strings/numbers;
        // watch out for weird cases:
        // '042' == '42.0' (true)
        // '042' == 42 (true)
        // '42' == '42xz' (false)
        // 42 == '42xz' (true)
        
        // easiest case: if they are completely equal
        if (left.toString().equals(right.toString())) {
            return Literal.TRUE;
        }
        boolean leftNumeric = left.isCompletelyNumeric();
        boolean rightNumeric = right.isCompletelyNumeric();
        
        // if both operands are completely numeric, we can compare their float values
        if (leftNumeric && rightNumeric) {
            if (left.getFloatValue() == right.getFloatValue()) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        }
        
        // if both operands are NOT completely numeric, we know that they
        // are strings and therefore have to be different
        if (!leftNumeric && !rightNumeric) {
            return Literal.FALSE;
        }
        
        // one of the operands is completely numeric, the other is not;
        // we don't know whether to completely numeric operand is a string or
        // not, so we don't know if they are equal (see example below)
        return Literal.TOP;
    }
    
// isIdenticalLiteral **************************************************************
    
    // tests the two operands for strict equality and returns the resulting literal
    public static Literal isIdenticalLiteral(Literal left, Literal right) {
        
        if (left == Literal.TOP || right == Literal.TOP) {
            throw new RuntimeException("SNH");
        }
        
        // if any of the two operands is boolean or null
        if (left == Literal.TRUE || left == Literal.FALSE ||
            right == Literal.TRUE || right == Literal.FALSE ||
            left == Literal.NULL || right == Literal.NULL) {
            if (left == right) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        }

        // in all other cases, we don't know the exact result since we aren't
        // performing type analysis
        return Literal.TOP;
        
    }
    
// invert **************************************************************************
    
    // inverts TRUE and FALSE, leaves TOP as it is
    public static Literal invert(Literal lit) {
        if (lit == Literal.FALSE) {
            return Literal.TRUE;
        } else if (lit == Literal.TRUE) {
            return Literal.FALSE;
        } else if (lit == Literal.TOP) {
            return lit;
        } else {
            throw new RuntimeException("SNH");
        }
    }
    
// isEqualToNullLiteral ************************************************************
    
    // returns the literal resulting from a loose comparison to NULL
    Literal isEqualToNullLiteral() {
        // NULL is equal to NULL, FALSE, and the number 0
        // but not equal to the string '0'
        // => if the literal is completely numeric and has a float value 0,
        // we can't say anything (could be a number as well as a string,
        // unknown since we don't perform type analysis)
        if (this == Literal.NULL || this == Literal.FALSE) {
            return Literal.TRUE;
        }
        if (this.isCompletelyNumeric() && this.getFloatValue() == 0) {
            return Literal.TOP;
        }
        // all other cases yield FALSE
        return Literal.FALSE;

    }

// isBool **************************************************************************
    
    // returns true if this is Literal.TRUE or Literal.FALSE, false otherwise
    boolean isBool() {
        if (this == Literal.TRUE || this == Literal.FALSE) {
            return true;
        } else {
            return false;
        }
    }
    
// *********************************************************************************
// SET *****************************************************************************    
// *********************************************************************************
     
    void setLiteral(String literal) {
        this.literal = literal;
    }
    
// *********************************************************************************
// OTHER ***************************************************************************    
// *********************************************************************************
  
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Literal)) {
            return false;
        }
        Literal comp = (Literal) obj;
        return (this.literal.equals(comp.toString()));
    }

    // CAUTION: hashCode has to be set to 0 and recomputed if the element is changed
    public int hashCode() {
        return this.literal.hashCode();
        /*
        if (this.hashCode == 0) {
            this.hashCode = this.literal.hashCode();
        }
        return this.hashCode;
        */
    }

}

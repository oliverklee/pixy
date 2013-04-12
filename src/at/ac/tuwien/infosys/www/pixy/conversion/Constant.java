package at.ac.tuwien.infosys.www.pixy.conversion;

public class Constant
    extends TacPlace {

    private String label;

    static public final Constant TRUE = new Constant("TRUE", Literal.TRUE);
    static public final Constant FALSE = new Constant("FALSE", Literal.FALSE);
    static final Constant NULL = new Constant("NULL", Literal.NULL);
    // special constants used in builtinFunctions.php;
    // are not modeled as constants any longer, but as superglobals
    //static final Constant UNTAINTED = new Constant("_UNTAINTED", Literal.TOP, Taint.UNTAINTED);
    //static final Constant TAINTED = new Constant("_TAINTED", Literal.TOP, Taint.TAINTED);

    // marker for the special constants above
    private boolean isSpecial;
    // literal for the special constants above
    private Literal specialLiteral;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // use getInstance() instead of constructor:
    // we want to be able to return the existing special instances TRUE, FALSE and
    // NULL
    private Constant(String label) {
        this.label = label;
        this.isSpecial = false;
    }

    // only used for the special constants above
    private Constant(String label, Literal specialLiteral) {
        this.label = label;
        this.isSpecial = true;
        this.specialLiteral = specialLiteral;
    }

    static Constant getInstance(String label) {
        if (label.equalsIgnoreCase("true")) {
            return Constant.TRUE;
        } else if (label.equalsIgnoreCase("false")) {
            return Constant.FALSE;
        } else if (label.equalsIgnoreCase("null")) {
            return Constant.NULL;
        } else {
            return new Constant(label);
        }
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return this.label;
    }

    public boolean isSpecial() {
        return this.isSpecial;
    }

    public Literal getSpecialLiteral() {
        return this.specialLiteral;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Constant)) {
            return false;
        }
        Constant comp = (Constant) obj;
        return (this.label.equals(comp.getLabel()));
    }

    // CAUTION: hashCode has to be set to 0 and recomputed if the element is changed
    public int hashCode() {
        return this.label.hashCode();
    }
}
package at.ac.tuwien.infosys.www.pixy.conversion;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractTacPlace {
    public boolean isVariable() {
        return (this instanceof Variable);
    }

    public boolean isConstant() {
        return (this instanceof Constant);
    }

    public boolean isLiteral() {
        return (this instanceof Literal);
    }

    public Variable getVariable() {
        return (Variable) this;
    }

    Constant getConstant() {
        return (Constant) this;
    }

    Literal getLiteral() {
        return (Literal) this;
    }

    public abstract String toString();

    public abstract boolean equals(Object obj);

    /**
     * Caution: hashCode has to be reset if any of the significant attributes are changed.
     *
     * Should be included in the corresponding methods.
     *
     * @return
     */
    public abstract int hashCode();
}
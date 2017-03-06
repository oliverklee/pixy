package at.ac.tuwien.infosys.www.pixy.conversion;

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
		return ((Variable) this);
	}

	Constant getConstant() {
		return ((Constant) this);
	}

	Literal getLiteral() {
		return ((Literal) this);
	}

	public abstract String toString();

	public abstract boolean equals(Object obj);

	public abstract int hashCode();

}

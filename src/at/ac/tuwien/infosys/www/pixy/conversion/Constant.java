package at.ac.tuwien.infosys.www.pixy.conversion;

public class Constant extends AbstractTacPlace {

	private String label;

	static public final Constant TRUE = new Constant("TRUE", Literal.TRUE);
	static public final Constant FALSE = new Constant("FALSE", Literal.FALSE);
	static final Constant NULL = new Constant("NULL", Literal.NULL);
	private boolean isSpecial;
	private Literal specialLiteral;

	private Constant(String label) {
		this.label = label;
		this.isSpecial = false;
	}

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

	public int hashCode() {
		return this.label.hashCode();
	}

}

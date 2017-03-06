package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Literal extends AbstractTacPlace {

	public static final Literal TRUE = new Literal("_true");
	public static final Literal FALSE = new Literal("_false");
	public static final Literal NULL = new Literal("_null");
	public static final Literal TOP = new Literal("_top");
	private String literal;
	private static Pattern strtod = Pattern.compile("\\s*[+-]?((\\d+(\\.\\d*)?)|\\d*\\.\\d+)([eE][+-]?\\d+)?");

	public Literal(String literal) {
		this(literal, true);
	}

	public Literal(String literal, boolean trim) {

		boolean in_double_quotes = false;
		boolean in_single_quotes = false;

		if (literal.length() > 1) {
			if ((literal.charAt(0) == '"' && literal.charAt(literal.length() - 1) == '"')) {
				in_double_quotes = true;
			} else if (literal.charAt(0) == '\'' && literal.charAt(literal.length() - 1) == '\'') {
				in_single_quotes = true;
			}
		}

		if (trim && (in_double_quotes || in_single_quotes)) {
			literal = literal.substring(1, literal.length() - 1);
		}

		if (in_single_quotes && literal.contains("\\")) {

			StringBuilder buf = new StringBuilder();
			Integer backSlash = literal.indexOf('\\');
			buf.append(literal.substring(0, backSlash));

			while (backSlash != null) {

				char escapedCharOld = literal.charAt(backSlash + 1);
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

				int nextBackSlash = literal.indexOf('\\', backSlash + 2);
				if (nextBackSlash == -1) {
					buf.append(literal.substring(backSlash + 2));
					backSlash = null;
				} else {
					buf.append(literal.substring(backSlash + 2, nextBackSlash));
					backSlash = nextBackSlash;
				}
			}

			literal = buf.toString();
		}

		else if (in_double_quotes && literal.contains("\\")) {

			StringBuilder buf = new StringBuilder();
			Integer backSlash = literal.indexOf('\\');
			buf.append(literal.substring(0, backSlash));

			while (backSlash != null) {

				char escapedCharOld = literal.charAt(backSlash + 1);
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

				int nextBackSlash = literal.indexOf('\\', backSlash + 2);
				if (nextBackSlash == -1) {
					buf.append(literal.substring(backSlash + 2));
					backSlash = null;
				} else {
					buf.append(literal.substring(backSlash + 2, nextBackSlash));
					backSlash = nextBackSlash;
				}
			}

			literal = buf.toString();
		}

		this.literal = literal;
	}

	public String toString() {
		return this.literal;
	}

	public Literal getBoolValueLiteral() {

		if (this == Literal.TOP) {
			return Literal.TOP;
		}
		if (this == Literal.TRUE) {
			return Literal.TRUE;
		}
		if (this == Literal.FALSE || this == Literal.NULL || this.literal.length() == 0 || this.literal.equals("0")) {

			return Literal.FALSE;
		}

		if (this.isCompletelyNumeric() && this.getFloatValue() == 0) {
			return Literal.TOP;
		}

		return Literal.TRUE;
	}

	boolean isCompletelyNumeric() {
		if (this == Literal.NULL || this == Literal.TRUE || this == Literal.FALSE) {
			return true;
		}
		try {
			Float.parseFloat(this.literal);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

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
			return 0;
		}
	}

	int getIntValue() {
		return (int) this.getFloatValue();
	}

	public Literal getFloatValueLiteral() {

		if (this == Literal.TOP) {
			return Literal.TOP;
		}

		if (this == Literal.TRUE) {
			return new Literal("1");
		}
		if (this == Literal.FALSE || this == Literal.NULL) {
			return new Literal("0");
		}

		return new Literal(Literal.numberToString(this.getFloatValue()));
	}

	public Literal getIntValueLiteral() {

		if (this == Literal.TOP) {
			return Literal.TOP;
		}
		return new Literal(Literal.numberToString(this.getIntValue()));
	}

	public Literal getStringValueLiteral() {
		if (this == Literal.TOP) {
			return Literal.TOP;
		}
		return new Literal(this.getStringValue());
	}

	public String getStringValue() {
		if (this == Literal.TRUE) {
			return "1";
		}
		if (this == Literal.FALSE || this == Literal.NULL) {
			return "";
		}
		return this.literal;
	}

	public static String numberToString(float in) {
		return Float.toString(in);
	}

	static String numberToString(int in) {
		return Integer.toString(in);
	}

	public static Literal isSmallerLiteral(Literal left, Literal right, AbstractCfgNode cfgNode) {
		if (left.isCompletelyNumeric() && right.isCompletelyNumeric()) {
			if (left.getFloatValue() < right.getFloatValue()) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		} else {
			return Literal.TOP;
		}
	}

	public static Literal isGreaterLiteral(Literal left, Literal right, AbstractCfgNode cfgNode) {
		if (left.isCompletelyNumeric() && right.isCompletelyNumeric()) {
			if (left.getFloatValue() > right.getFloatValue()) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		} else {
			return Literal.TOP;

		}
	}

	public static Literal isEqualLiteral(Literal left, Literal right) {

		if (left == Literal.TOP || right == Literal.TOP) {
			throw new RuntimeException("SNH");
		}

		if (left.isBool() || right.isBool()) {
			if (left.getBoolValueLiteral() == right.getBoolValueLiteral()) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		}

		if (left == Literal.NULL) {
			return right.isEqualToNullLiteral();
		}
		if (right == Literal.NULL) {
			return left.isEqualToNullLiteral();
		}

		if (left.toString().equals(right.toString())) {
			return Literal.TRUE;
		}
		boolean leftNumeric = left.isCompletelyNumeric();
		boolean rightNumeric = right.isCompletelyNumeric();

		if (leftNumeric && rightNumeric) {
			if (left.getFloatValue() == right.getFloatValue()) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		}

		if (!leftNumeric && !rightNumeric) {
			return Literal.FALSE;
		}

		return Literal.TOP;
	}

	public static Literal isIdenticalLiteral(Literal left, Literal right) {

		if (left == Literal.TOP || right == Literal.TOP) {
			throw new RuntimeException("SNH");
		}

		if (left == Literal.TRUE || left == Literal.FALSE || right == Literal.TRUE || right == Literal.FALSE
				|| left == Literal.NULL || right == Literal.NULL) {
			if (left == right) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		}

		return Literal.TOP;

	}

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

	Literal isEqualToNullLiteral() {

		if (this == Literal.NULL || this == Literal.FALSE) {
			return Literal.TRUE;
		}
		if (this.isCompletelyNumeric() && this.getFloatValue() == 0) {
			return Literal.TOP;
		}
		return Literal.FALSE;
	}

	boolean isBool() {
		if (this == Literal.TRUE || this == Literal.FALSE) {
			return true;
		} else {
			return false;
		}
	}

	void setLiteral(String literal) {
		this.literal = literal;
	}

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

	public int hashCode() {
		return this.literal.hashCode();

	}

}
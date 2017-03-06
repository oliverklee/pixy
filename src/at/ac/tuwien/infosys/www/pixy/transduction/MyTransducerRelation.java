package at.ac.tuwien.infosys.www.pixy.transduction;

public final class MyTransducerRelation {

	final Object in;
	final Object out;

	public MyTransducerRelation(Object in, Object out) {
		this.in = in;
		this.out = out;
	}

	public Object getIn() {
		return in;
	}

	public Object getOut() {
		return out;
	}

	public boolean equals(Object compX) {

		if (compX == this) {
			return true;
		}
		if (!(compX instanceof MyTransducerRelation)) {
			return false;
		}
		MyTransducerRelation comp = (MyTransducerRelation) compX;

		if (this.in != null) {
			if (!this.in.equals(comp.in)) {
				return false;
			}
		} else {
			if (comp.in != null) {
				return false;
			}
		}

		if (this.out != null) {
			if (!this.out.equals(comp.out)) {
				return false;
			}
		} else {
			if (comp.out != null) {
				return false;
			}
		}

		return true;
	}

	public int hashCode() {
		int hashCode = 17;
		if (this.in != null) {
			hashCode = 37 * hashCode + this.in.hashCode();
		}
		if (this.out != null) {
			hashCode = 37 * hashCode + this.out.hashCode();
		}
		return hashCode;
	}

	public String toString() {
		return (in == null ? "1" : in.toString()) + "/" + (out == null ? "1" : out.toString());
	}
}
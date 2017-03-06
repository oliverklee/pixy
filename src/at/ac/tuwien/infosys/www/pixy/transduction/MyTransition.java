package at.ac.tuwien.infosys.www.pixy.transduction;

public class MyTransition {

	private MyState start;
	private Object label;
	private MyState end;

	public MyTransition(MyState start, Object label, MyState end) {
		if (start == null || end == null) {
			throw new RuntimeException("SNH");
		}
		this.start = start;
		this.label = label;
		this.end = end;
	}

	public MyState getStart() {
		return start;
	}

	public Object getLabel() {
		return label;
	}

	public MyState getEnd() {
		return end;
	}

	public String toString() {
		if (label == null) {
			return "(" + start + " , 1 , " + end + ")";
		} else {
			return "(" + start + " , " + label + " , " + end + ")";
		}
	}

	public boolean equals(Object compX) {

		if (compX == this) {
			return true;
		}
		if (!(compX instanceof MyTransition)) {
			return false;
		}
		MyTransition comp = (MyTransition) compX;

		if (!this.start.equals(comp.start)) {
			return false;
		}
		if (!this.end.equals(comp.end)) {
			return false;
		}
		if (this.label != null) {
			if (!this.label.equals(comp.label)) {
				return false;
			}
		} else {
			if (comp.label != null) {
				return false;
			}
		}
		return true;

	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.start.hashCode();
		hashCode = 37 * hashCode + this.end.hashCode();
		if (this.label != null) {
			hashCode = 37 * hashCode + this.label.hashCode();
		}
		return hashCode;
	}
}
package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

public class CallStringContext extends AbstractContext {

	private int position;

	public CallStringContext(int position) {
		this.position = position;
	}

	public int getPosition() {
		return this.position;
	}

	public int hashCode() {
		return this.position;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CallStringContext)) {
			return false;
		}
		CallStringContext comp = (CallStringContext) obj;
		return this.position == comp.getPosition();
	}

	public String toString() {
		return String.valueOf(this.position);
	}

}

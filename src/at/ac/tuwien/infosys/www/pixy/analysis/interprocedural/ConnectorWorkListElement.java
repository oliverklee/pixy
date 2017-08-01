package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallString;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;

public final class ConnectorWorkListElement {

	private final TacFunction function;
	private final CallString callString;

	ConnectorWorkListElement(TacFunction function, CallString callString) {
		this.function = function;
		this.callString = callString;
	}

	TacFunction getFunction() {
		return this.function;
	}

	CallString getCallString() {
		return this.callString;
	}
}

package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class ReverseTarget {

	private Call callNode;
	private Set<? extends AbstractContext> contexts;

	public ReverseTarget(Call callNode, Set<? extends AbstractContext> contexts) {
		this.callNode = callNode;
		this.contexts = contexts;
	}

	public Call getCallNode() {
		return this.callNode;
	}

	public Set<? extends AbstractContext> getContexts() {
		return this.contexts;
	}
}

package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class CallGraphNode {

	private TacFunction function;
	private Map<Call, CallGraphNode> outEdges;
	private Map<Call, CallGraphNode> inEdges;

	CallGraphNode(TacFunction function) {
		this.function = function;
		this.outEdges = new HashMap<Call, CallGraphNode>();
		this.inEdges = new HashMap<Call, CallGraphNode>();
	}

	public TacFunction getFunction() {
		return this.function;
	}

	Collection<CallGraphNode> getSuccessors() {
		return this.outEdges.values();
	}

	Collection<CallGraphNode> getPredecessors() {
		return this.inEdges.values();
	}

	Set<Call> getCallsTo() {
		return this.inEdges.keySet();
	}

	public boolean equals(Object compX) {

		if (compX == this) {
			return true;
		}
		if (!(compX instanceof CallGraphNode)) {
			return false;
		}
		CallGraphNode comp = (CallGraphNode) compX;

		return this.function.equals(comp.function);
	}

	public int hashCode() {
		return this.function.hashCode();
	}

	public void addCallee(Call callNode, CallGraphNode calleeNode) {
		this.outEdges.put(callNode, calleeNode);
	}

	public void addCaller(Call callNode, CallGraphNode callerNode) {
		this.inEdges.put(callNode, callerNode);
	}

}

package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

public class CallGraph {

	private Map<TacFunction, CallGraphNode> nodes;
	private TacFunction mainFunction;

	CallGraph(TacFunction mainFunction) {
		this.nodes = new HashMap<TacFunction, CallGraphNode>();
		this.mainFunction = mainFunction;
		this.nodes.put(mainFunction, new CallGraphNode(mainFunction));
	}

	public void add(TacFunction caller, TacFunction callee, Call callNode) {

		CallGraphNode callerNode = this.nodes.get(caller);
		if (callerNode == null) {
			callerNode = new CallGraphNode(caller);
			this.nodes.put(caller, callerNode);
		}

		CallGraphNode calleeNode = this.nodes.get(callee);
		if (calleeNode == null) {
			calleeNode = new CallGraphNode(callee);
			this.nodes.put(callee, calleeNode);
		}

		callerNode.addCallee(callNode, calleeNode);
		calleeNode.addCaller(callNode, callerNode);
	}

	public Map<TacFunction, Integer> getPostOrder() {

		List<CallGraphNode> postorder = new LinkedList<CallGraphNode>();

		LinkedList<CallGraphNode> stack = new LinkedList<CallGraphNode>();
		Set<CallGraphNode> visited = new HashSet<CallGraphNode>();

		stack.add(this.nodes.get(this.mainFunction));

		while (!stack.isEmpty()) {

			CallGraphNode node = stack.getLast();
			visited.add(node);

			CallGraphNode nextNode = null;
			Iterator<CallGraphNode> calleeIter = node.getSuccessors().iterator();
			while (calleeIter.hasNext() && nextNode == null) {
				CallGraphNode callee = calleeIter.next();
				if (!visited.contains(callee)) {
					nextNode = callee;
				}
			}

			if (nextNode == null) {
				postorder.add(stack.removeLast());
			} else {
				stack.add(nextNode);
			}
		}

		Map<TacFunction, Integer> retMe = new HashMap<TacFunction, Integer>();
		int i = 0;
		for (CallGraphNode f : postorder) {
			retMe.put(f.getFunction(), i++);
		}
		return retMe;
	}

	public Collection<TacFunction> getFunctions() {
		return this.nodes.keySet();
	}

	public Collection<CallGraphNode> getCallers(TacFunction f) {
		return this.nodes.get(f).getPredecessors();
	}

	public Set<Call> getCallsTo(TacFunction f) {
		return this.nodes.get(f).getCallsTo();
	}

	public boolean reachable(TacFunction f) {
		return this.nodes.containsKey(f);
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		for (CallGraphNode n : this.nodes.values()) {
			b.append(n.getFunction().getName());
			b.append("\n");
			for (CallGraphNode callee : n.getSuccessors()) {
				b.append("- ");
				b.append(callee.getFunction().getName());
				b.append("\n");
			}
		}
		return b.toString();
	}
}

package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Node {

	private Variable label;
	private Map<Node, Edge> doubleEdges;

	public Node(Variable label) {
		this.label = label;
		this.doubleEdges = new HashMap<Node, Edge>();
	}

	public Variable getLabel() {
		return this.label;
	}

	public Set<Node> getDoubleTargets() {
		return new HashSet<Node>(this.doubleEdges.keySet());
	}

	public void addDoubleEdge(Edge edge, Node target) {
		this.doubleEdges.put(target, edge);
	}

}

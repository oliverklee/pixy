package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Graph {

	private Map<Variable, Node> label2nodes;
	private Set<Edge> singleEdges;
	private Set<Edge> doubleEdges;

	public Graph(Set<?> variables) {
		this.label2nodes = new HashMap<Variable, Node>();
		this.singleEdges = new HashSet<Edge>();
		this.doubleEdges = new HashSet<Edge>();
		for (Iterator<?> iter = variables.iterator(); iter.hasNext();) {
			Variable variable = (Variable) iter.next();
			this.createNode(variable);
		}
	}

	private Node createNode(Variable label) {
		Node newNode = new Node(label);
		this.label2nodes.put(label, newNode);
		return newNode;
	}

	public Node getNode(Variable label) {
		return (Node) this.label2nodes.get(label);
	}

	public void drawFirstScc(Set<Variable> varSet) {
		Set<Variable> fromVarSet = varSet;
		Set<Variable> toVarSet = new HashSet<Variable>(fromVarSet);

		for (Iterator<Variable> iter = fromVarSet.iterator(); iter.hasNext();) {
			Variable fromVar = (Variable) iter.next();
			toVarSet.remove(fromVar);
			this.drawFirstEdge(fromVar, toVarSet);
		}
	}

	private void drawFirstEdge(Variable fromVar, Set<Variable> toVarSet) {
		Node fromNode = (Node) this.label2nodes.get(fromVar);
		for (Iterator<Variable> iter = toVarSet.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			Node toNode = (Node) this.label2nodes.get(var);
			Edge edge = new Edge(fromNode, toNode);
			this.singleEdges.add(edge);
		}
	}

	public void drawSecondScc(Set<Variable> varSet) {
		Set<Variable> fromVarSet = varSet;
		Set<Variable> toVarSet = new HashSet<Variable>(fromVarSet);

		for (Iterator<Variable> iter = fromVarSet.iterator(); iter.hasNext();) {
			Variable fromVar = (Variable) iter.next();
			toVarSet.remove(fromVar);
			this.drawSecondEdge(fromVar, toVarSet);
		}
	}

	private void drawSecondEdge(Variable fromVar, Set<Variable> toVarSet) {
		Node fromNode = (Node) this.label2nodes.get(fromVar);
		for (Iterator<Variable> iter = toVarSet.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			Node toNode = (Node) this.label2nodes.get(var);
			Edge edge = new Edge(fromNode, toNode);
			if (this.singleEdges.contains(edge)) {
				this.singleEdges.remove(edge);
				this.doubleEdges.add(edge);
				toNode.addDoubleEdge(edge, fromNode);
				fromNode.addDoubleEdge(edge, toNode);
			} else {
				this.singleEdges.add(edge);
			}
		}
	}

	public Set<Edge> getSingleEdges() {
		return this.singleEdges;
	}

	public Set<Set<Variable>> getDoubleSccs() {

		Set<Set<Variable>> sccs = new HashSet<Set<Variable>>();
		Set<Node> nodesWorkSet = new HashSet<Node>(this.label2nodes.values());

		while (!nodesWorkSet.isEmpty()) {

			Node node = (Node) nodesWorkSet.iterator().next();

			Set<?> doubleTargets = node.getDoubleTargets();

			if (!doubleTargets.isEmpty()) {

				Set<Variable> scc = new HashSet<Variable>();
				for (Iterator<?> iter = doubleTargets.iterator(); iter.hasNext();) {
					Node sccNode = (Node) iter.next();
					scc.add(sccNode.getLabel());
				}
				scc.add(node.getLabel());
				sccs.add(scc);
				nodesWorkSet.removeAll(doubleTargets);
			}

			nodesWorkSet.remove(node);
		}

		return sccs;
	}
}

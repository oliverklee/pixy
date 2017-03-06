package at.ac.tuwien.infosys.www.pixy.conversion.includes;

import java.io.*;
import java.util.*;

public class IncludeGraph {

	private IncludeNode root;

	private Set<IncludeNode> nodes;

	private HashMap<IncludeNode, Set<IncludeNode>> adjSets;

	private HashMap<IncludeNode, Integer> inDegrees;

	public IncludeGraph(File rootFile) {
		this.root = new IncludeNode(rootFile);

		this.nodes = new HashSet<IncludeNode>();
		this.nodes.add(root);

		this.adjSets = new HashMap<IncludeNode, Set<IncludeNode>>();
		this.adjSets.put(root, new HashSet<IncludeNode>());

		this.inDegrees = new HashMap<IncludeNode, Integer>();
		this.inDegrees.put(root, new Integer(0));
	}

	private IncludeGraph(IncludeGraph cloneMe) {
		this.root = cloneMe.root;
		this.nodes = new HashSet<IncludeNode>(cloneMe.nodes);
		this.adjSets = new HashMap<IncludeNode, Set<IncludeNode>>(cloneMe.adjSets);
		this.inDegrees = new HashMap<IncludeNode, Integer>(cloneMe.inDegrees);
	}

	public String dump() {
		StringBuilder b = new StringBuilder();
		for (Map.Entry<IncludeNode, Set<IncludeNode>> entry : this.adjSets.entrySet()) {
			IncludeNode from = entry.getKey();
			Set<IncludeNode> tos = entry.getValue();
			b.append(from.getCanonicalPath());
			b.append("\n");
			for (IncludeNode to : tos) {
				b.append("- ");
				b.append(to.getCanonicalPath());
				b.append("\n");
			}
		}

		return b.toString();
	}

	public boolean addAcyclicEdge(File fromFile, File toFile) {

		IncludeNode from = new IncludeNode(fromFile);
		IncludeNode to = new IncludeNode(toFile);

		if (!this.nodes.contains(from)) {
			throw new RuntimeException("SNH: " + from);
		}

		if (this.edgeExists(from, to)) {
			return true;
		}

		if (!this.nodes.contains(to)) {
			this.addNode(to);
			this.addEdge(from, to);
			return true;
		}

		this.addNode(to);
		this.addEdge(from, to);

		if (isCyclic()) {
			this.removeEdge(from, to);
			this.clean(from);
			this.clean(to);
			return false;
		} else {
			return true;
		}
	}

	private boolean edgeExists(IncludeNode from, IncludeNode to) {
		Set<?> adjSet = (Set<?>) this.adjSets.get(from);
		if (adjSet == null) {
			return false;
		}
		return adjSet.contains(to);
	}

	private void addEdge(IncludeNode from, IncludeNode to) {
		Set<IncludeNode> adjSet = this.adjSets.get(from);
		adjSet.add(to);
		this.increaseInDegree(to);
	}

	private void removeEdge(IncludeNode from, IncludeNode to) {
		this.decreaseInDegree(to);
		Set<?> adjSet = (Set<?>) this.adjSets.get(from);
		adjSet.remove(to);
	}

	private void increaseInDegree(IncludeNode node) {
		Integer inDegree = (Integer) this.inDegrees.get(node);
		this.inDegrees.put(node, new Integer(inDegree.intValue() + 1));
	}

	private void decreaseInDegree(IncludeNode node) {
		Integer inDegree = (Integer) this.inDegrees.get(node);
		this.inDegrees.put(node, new Integer(inDegree.intValue() - 1));
	}

	private void addNode(IncludeNode node) {
		this.nodes.add(node);

		Set<?> adjSet = (Set<?>) this.adjSets.get(node);
		if (adjSet == null) {
			this.adjSets.put(node, new HashSet<IncludeNode>());
		}

		Integer inDegree = (Integer) this.inDegrees.get(node);
		if (inDegree == null) {
			this.inDegrees.put(node, new Integer(0));
		}
	}

	private boolean isCyclic() {

		IncludeGraph clone = new IncludeGraph(this);
		boolean goOn = true;
		while (goOn) {

			Set<?> inDegreeZeros = clone.getInDegreeZeros();

			if (inDegreeZeros.isEmpty()) {
				goOn = false;
			} else {
				for (Iterator<?> iter = inDegreeZeros.iterator(); iter.hasNext();) {
					IncludeNode node = (IncludeNode) iter.next();
					clone.removeZero(node);
				}
			}
		}
		if (clone.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private Set<IncludeNode> getInDegreeZeros() {
		Set<IncludeNode> retMe = new HashSet<IncludeNode>();

		for (Map.Entry<IncludeNode, Integer> entry : this.inDegrees.entrySet()) {
			int inDegree = entry.getValue().intValue();
			if (inDegree == 0) {
				retMe.add(entry.getKey());
			}
		}

		return retMe;
	}

	private void removeZero(IncludeNode node) {
		Set<?> adjSet = (Set<?>) this.adjSets.get(node);
		for (Iterator<?> iter = adjSet.iterator(); iter.hasNext();) {
			IncludeNode successor = (IncludeNode) iter.next();
			this.decreaseInDegree(successor);
		}
		this.adjSets.remove(node);
		this.inDegrees.remove(node);
		this.nodes.remove(node);
	}

	private boolean isEmpty() {
		return this.nodes.isEmpty();
	}

	private void clean(IncludeNode node) {
		boolean hasNoSucc = ((Set<?>) this.adjSets.get(node)).isEmpty();
		boolean hasNoPred = ((Integer) this.inDegrees.get(node)).intValue() == 0;
		if (hasNoSucc && hasNoPred && !node.equals(this.root)) {
			this.nodes.remove(node);
			this.adjSets.remove(node);
			this.inDegrees.remove(node);
		}
	}
}
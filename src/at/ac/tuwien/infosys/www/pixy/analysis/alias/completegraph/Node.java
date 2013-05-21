package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Node in the Graph.
 *
 * SCC stands for "strongly connected component".
 *
 * Note: This term is not really correct here, it should be "complete graph" instead.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Node {
    private Variable label;
    // Map Node -> Edge (i.e., target node -> edge)
    private Map<Node, Edge> doubleEdges;

    public Node(Variable label) {
        this.label = label;
        this.doubleEdges = new HashMap<>();
    }

    public Variable getLabel() {
        return this.label;
    }

    public Set<Node> getDoubleTargets() {
        return new HashSet<>(this.doubleEdges.keySet());
    }

    public void addDoubleEdge(Edge edge, Node target) {
        this.doubleEdges.put(target, edge);
    }
}
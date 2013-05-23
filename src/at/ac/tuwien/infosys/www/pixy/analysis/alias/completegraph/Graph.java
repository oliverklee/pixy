package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Auxiliary graph for computing least upper bounds of alias lattice elements.
 *
 * SCC stands for "strongly connected component".
 *
 * Note: This term is not really correct here, it should be "complete graph" instead.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Graph {
    // Variable (label) -> Node
    private Map<Variable, Node> label2nodes;

    // Sets of Edge's
    private Set<Edge> singleEdges;
    private Set<Edge> doubleEdges;

    // expects a set of variables for which to create nodes
    public Graph(Set<Variable> variables) {
        this.label2nodes = new HashMap<>();
        this.singleEdges = new HashSet<>();
        this.doubleEdges = new HashSet<>();
        for (Variable variable : variables) {
            this.createNode(variable);
        }
    }

    private Node createNode(Variable label) {
        Node newNode = new Node(label);
        this.label2nodes.put(label, newNode);
        return newNode;
    }

    public void drawFirstScc(Set<Variable> varSet) {
        Set<Variable> fromVarSet = varSet;
        Set<Variable> toVarSet = new HashSet<>(fromVarSet);

        for (Variable fromVar : fromVarSet) {
            toVarSet.remove(fromVar);
            this.drawFirstEdge(fromVar, toVarSet);
        }
    }

    private void drawFirstEdge(Variable fromVar, Set<Variable> toVarSet) {
        Node fromNode = this.label2nodes.get(fromVar);
        for (Variable variable : toVarSet) {
            Node toNode = this.label2nodes.get(variable);
            Edge edge = new Edge(fromNode, toNode);
            this.singleEdges.add(edge);
        }
    }

    public void drawSecondScc(Set<Variable> varSet) {
        Set<Variable> fromVarSet = varSet;
        Set<Variable> toVarSet = new HashSet<>(fromVarSet);

        for (Variable fromVar : fromVarSet) {
            toVarSet.remove(fromVar);
            this.drawSecondEdge(fromVar, toVarSet);
        }
    }

    private void drawSecondEdge(Variable fromVar, Set<Variable> toVarSet) {
        Node fromNode = this.label2nodes.get(fromVar);
        for (Variable variable : toVarSet) {
            Node toNode = this.label2nodes.get(variable);
            Edge edge = new Edge(fromNode, toNode);
            // check if such an edge already exists
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

    // returns the set of strongly connected components (connected by
    // double lines)
    // (i.e., returns a set of sets of Variables (not SccNodes: more convenient))
    public Set<Set<Variable>> getDoubleSccs() {

        // to be returned:
        Set<Set<Variable>> sccs = new HashSet<>();

        // we start with a workset containing all nodes from the graph
        Set<Node> nodesWorkSet = new HashSet<>(this.label2nodes.values());

        while (!nodesWorkSet.isEmpty()) {

            // pick an arbitrary node from the workset
            Node node = nodesWorkSet.iterator().next();

            // ask the node about its double targets
            Set<Node> doubleTargets = node.getDoubleTargets();

            // if there are such double targets
            if (!doubleTargets.isEmpty()) {

                // transform this set of SccNodes into a set of Variables
                Set<Variable> scc = new HashSet<>();
                for (Node sccNode : doubleTargets) {
                    scc.add(sccNode.getLabel());
                }
                // add the variable of the node itself to the scc set
                scc.add(node.getLabel());
                // add the scc set to the set of sccs
                sccs.add(scc);

                // no need to visit any of these nodes again, so
                // remove them from the workset
                nodesWorkSet.removeAll(doubleTargets);
            }

            nodesWorkSet.remove(node);
        }

        return sccs;
    }
}
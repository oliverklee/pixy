package at.ac.tuwien.infosys.www.pixy.analysis.alias.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// auxiliary graph for computing least upper bounds of alias lattice elements;
// SCC stands for "strongly connected component";
// NOTE: this term is not really correct here, it should be "complete graph"!
public class SccGraph {

    // Variable (label) -> SccNode
    private Map<Variable,SccNode> label2nodes;

    // Sets of SccEdge's
    private Set<SccEdge> singleEdges;
    private Set<SccEdge> doubleEdges;

    // expects a set of variables for which to create nodes
    public SccGraph(Set variables) {
        this.label2nodes = new HashMap<Variable,SccNode>();
        this.singleEdges = new HashSet<SccEdge>();
        this.doubleEdges = new HashSet<SccEdge>();
        for (Iterator iter = variables.iterator(); iter.hasNext();) {
            Variable variable = (Variable) iter.next();
            this.createNode(variable);
        }
    }

    private SccNode createNode(Variable label) {
        SccNode newNode = new SccNode(label);
        this.label2nodes.put(label, newNode);
        return newNode;
    }

    public SccNode getNode(Variable label) {
        return (SccNode) this.label2nodes.get(label);
    }

    public void drawFirstScc(Set<Variable> varSet) {
        Set<Variable> fromVarSet = varSet;
        Set<Variable> toVarSet = new HashSet<Variable>(fromVarSet);

        for (Iterator iter = fromVarSet.iterator(); iter.hasNext(); ) {
            Variable fromVar = (Variable) iter.next();
            toVarSet.remove(fromVar);
            this.drawFirstEdge(fromVar, toVarSet);
        }
    }

    private void drawFirstEdge(Variable fromVar, Set toVarSet) {
        SccNode fromNode = (SccNode) this.label2nodes.get(fromVar);
        for (Iterator iter = toVarSet.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            SccNode toNode = (SccNode) this.label2nodes.get(var);
            SccEdge edge = new SccEdge(fromNode, toNode);
            this.singleEdges.add(edge);
        }
    }

    public void drawSecondScc(Set<Variable> varSet) {
        Set<Variable> fromVarSet = varSet;
        Set<Variable> toVarSet = new HashSet<Variable>(fromVarSet);

        for (Iterator iter = fromVarSet.iterator(); iter.hasNext(); ) {
            Variable fromVar = (Variable) iter.next();
            toVarSet.remove(fromVar);
            this.drawSecondEdge(fromVar, toVarSet);
        }
    }

    private void drawSecondEdge(Variable fromVar, Set toVarSet) {
        SccNode fromNode = (SccNode) this.label2nodes.get(fromVar);
        for (Iterator iter = toVarSet.iterator(); iter.hasNext();) {
            Variable var = (Variable) iter.next();
            SccNode toNode = (SccNode) this.label2nodes.get(var);
            SccEdge edge = new SccEdge(fromNode, toNode);
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

    public Set<SccEdge> getSingleEdges() {
        return this.singleEdges;
    }

    // returns the set of strongly connected components (connected by
    // double lines)
    // (i.e., returns a set of sets of Variables (not SccNodes: more convenient))
    public Set<Set<Variable>> getDoubleSccs() {

        // to be returned:
        Set<Set<Variable>> sccs = new HashSet<Set<Variable>>();

        // we start with a workset containing all nodes from the graph
        Set<SccNode> nodesWorkSet = new HashSet<SccNode>(this.label2nodes.values());

        while (!nodesWorkSet.isEmpty()) {

            // pick an arbitrary node from the workset
            SccNode node = (SccNode) nodesWorkSet.iterator().next();

            // ask the node about its double targets
            Set doubleTargets = node.getDoubleTargets();

            // if there are such double targets
            if (!doubleTargets.isEmpty()) {

                // transform this set of SccNodes into a set of Variables
                Set<Variable> scc = new HashSet<Variable>();
                for (Iterator iter = doubleTargets.iterator(); iter.hasNext();) {
                    SccNode sccNode = (SccNode) iter.next();
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
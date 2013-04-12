package at.ac.tuwien.infosys.www.pixy.conversion.includes;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// an include graph consists of vertices corresponding to files
// and directed edges corresponding to include relationships;
// it has exactly one root (the entry file) and must be acyclic;
// LATER: faster implementation
public class IncludeGraph {

    private IncludeNode root;

    private Set<IncludeNode> nodes;

    // IncludeNode -> Set of IncludeNodes (successors)
    private HashMap<IncludeNode, Set<IncludeNode>> adjSets;

    // IncludeNode -> Integer
    private HashMap<IncludeNode, Integer> inDegrees;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

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

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

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

//  addAcyclicEdge *****************************************************************

    // if adding the indicated edge leaves the graph acyclic,
    // it is added and "true" is returned; otherwise, "false"
    // is returned
    public boolean addAcyclicEdge(File fromFile, File toFile) {

        IncludeNode from = new IncludeNode(fromFile);
        IncludeNode to = new IncludeNode(toFile);

        // System.out.println("addAcyclicEdge: " + from + " -> " + to);

        if (!this.nodes.contains(from)) {
            throw new RuntimeException("SNH: " + from);
        }

        // if this edge exists already: adding the edge can't make this
        // graph cyclic
        if (this.edgeExists(from, to)) {
            return true;
        }

        // speed-up: if the "to" node doesn't exist yet,
        // there can't appear a cycle through this addition
        if (!this.nodes.contains(to)) {
            this.addNode(to);
            this.addEdge(from, to);
            return true;
        }

        this.addNode(to);
        this.addEdge(from, to);

        if (isCyclic()) {
            // if this made the graph cyclic: undo
            this.removeEdge(from, to);
            this.clean(from);
            this.clean(to);
            return false;
        } else {
            return true;
        }
    }

//  edgeExists *********************************************************************

    // tests whether the indicated edge already exists in this graph
    private boolean edgeExists(IncludeNode from, IncludeNode to) {
        Set adjSet = (Set) this.adjSets.get(from);
        if (adjSet == null) {
            return false;
        }
        return adjSet.contains(to);
    }

//  addEdge ************************************************************************

    // simply adds the indicated edge without asking questions
    private void addEdge(IncludeNode from, IncludeNode to) {
        Set<IncludeNode> adjSet = this.adjSets.get(from);
        adjSet.add(to);
        this.increaseInDegree(to);
    }

//  removeEdge *********************************************************************

    private void removeEdge(IncludeNode from, IncludeNode to) {
        this.decreaseInDegree(to);
        Set adjSet = (Set) this.adjSets.get(from);
        adjSet.remove(to);
    }

//  increaseInDegree ***************************************************************

    private void increaseInDegree(IncludeNode node) {
        Integer inDegree = (Integer) this.inDegrees.get(node);
        this.inDegrees.put(node, new Integer(inDegree.intValue() + 1));
    }

//  decreaseInDegree ***************************************************************

    private void decreaseInDegree(IncludeNode node) {
        Integer inDegree = (Integer) this.inDegrees.get(node);
        this.inDegrees.put(node, new Integer(inDegree.intValue() - 1));
    }

//  addNode ************************************************************************

    private void addNode(IncludeNode node) {
        this.nodes.add(node);

        Set adjSet = (Set) this.adjSets.get(node);
        if (adjSet == null) {
            this.adjSets.put(node, new HashSet<IncludeNode>());
        }

        Integer inDegree = (Integer) this.inDegrees.get(node);
        if (inDegree == null) {
            this.inDegrees.put(node, new Integer(0));
        }
    }

//  isCyclic ***********************************************************************

    // tests whether this graph is actually cyclic
    private boolean isCyclic() {
        // - clone this graph
        // - continue = true
        // - while "continue"
        //   - find all nodes with inDegree == 0
        //   - if there are such nodes:
        //     - remove them together with their outgoing edges
        //   - else: continue = false
        // - if the cloned graph is empty: return false
        // - else: return true

        IncludeGraph clone = new IncludeGraph(this);
        boolean goOn = true;
        while (goOn) {

            // nodes with in-degree == 0
            Set inDegreeZeros = clone.getInDegreeZeros();

            if (inDegreeZeros.isEmpty()) {
                goOn = false;
            } else {
                // remove these nodes (together with their outgoing edges)
                for (Iterator iter = inDegreeZeros.iterator(); iter.hasNext(); ) {
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

//  getInDegreeZeros **************************************************************

    // returns a set of IncludeNode's with in-degree zero
    private Set<IncludeNode> getInDegreeZeros() {
        Set<IncludeNode> retMe = new HashSet<IncludeNode>();

        // EFF: this is a linear search
        for (Map.Entry<IncludeNode, Integer> entry : this.inDegrees.entrySet()) {
            int inDegree = entry.getValue().intValue();
            if (inDegree == 0) {
                retMe.add(entry.getKey());
            }
        }

        return retMe;
    }

//  removeZero *********************************************************************

    // removes the given node from the graph (and hence, decreases the in-degree
    // for all its successors); assumes that the given node has no incoming edges
    private void removeZero(IncludeNode node) {
        Set adjSet = (Set) this.adjSets.get(node);
        for (Iterator iter = adjSet.iterator(); iter.hasNext(); ) {
            IncludeNode successor = (IncludeNode) iter.next();
            this.decreaseInDegree(successor);
        }
        this.adjSets.remove(node);
        this.inDegrees.remove(node);
        this.nodes.remove(node);
    }

//  isEmpty ************************************************************************

    private boolean isEmpty() {
        return this.nodes.isEmpty();
    }

//  clean **************************************************************************

    // removes the given node if it has neither in- nor outgoing edges and
    // if it is not the root node
    private void clean(IncludeNode node) {

        boolean hasNoSucc = ((Set) this.adjSets.get(node)).isEmpty();
        boolean hasNoPred = ((Integer) this.inDegrees.get(node)).intValue() == 0;
        if (hasNoSucc && hasNoPred && !node.equals(this.root)) {
            this.nodes.remove(node);
            this.adjSets.remove(node);
            this.inDegrees.remove(node);
        }
    }
}
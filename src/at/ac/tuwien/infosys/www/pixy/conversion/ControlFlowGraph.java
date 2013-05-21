package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class ControlFlowGraph {
    private AbstractCfgNode head;
    private final AbstractCfgNode tail;
    private final int tailEdgeType;

// CONSTRUCTORS ********************************************************************

    ControlFlowGraph(AbstractCfgNode head, AbstractCfgNode tail, int tailEdgeType) {
        this.head = head;
        this.tail = tail;
        this.tailEdgeType = tailEdgeType;
    }

    ControlFlowGraph(AbstractCfgNode head, AbstractCfgNode tail) {
        this.head = head;
        this.tail = tail;
        this.tailEdgeType = CfgEdge.NORMAL_EDGE;
    }

    // dummy constructor, can be used for "getFunction"
    public ControlFlowGraph() {
        this.tail = null;
        this.tailEdgeType = 0;
    }

// GET *****************************************************************************

    public AbstractCfgNode getHead() {
        return this.head;
    }

    public AbstractCfgNode getTail() {
        return this.tail;
    }

    int getTailEdgeType() {
        return this.tailEdgeType;
    }

    // returns the function that contains the given CFG node;
    // throws an exception if it does not succeed
    public static TacFunction getFunction(AbstractCfgNode cfgNode) {
        return cfgNode.getEnclosingFunction();
    }

    // EFF: this function should be called only once, and the result cached;
    // this is what currently happens in ConnectorComputation
    public List<Call> getContainedCalls() {
        List<Call> retMe = new LinkedList<>();
        for (AbstractCfgNode cfgNode : this.dfPreOrder()) {
            if (cfgNode instanceof Call) {
                retMe.add((Call) cfgNode);
            }
        }
        return retMe;
    }

    // returns the head of this cfg by walking backwards;
    // only use this for linear cfgs (e.g., default param cfgs)
    public static AbstractCfgNode getHead(AbstractCfgNode cfgNode) {

        boolean goOn = true;
        while (goOn) {
            List<AbstractCfgNode> pre = cfgNode.getPredecessors();
            if (pre.size() == 0) {
                // found the head!
                goOn = false;
            } else if (pre.size() == 1) {
                // continue with predecessor
                cfgNode = pre.get(0);
            } else {
                // more than one predecessor...
                System.out.println(cfgNode.getLoc());
                throw new RuntimeException("SNH");
            }
        }
        return cfgNode;
    }

// SET *****************************************************************************

    void setHead(AbstractCfgNode head) {
        this.head = head;
    }

// OTHER ***************************************************************************

    public void assignReversePostOrder() {
        LinkedList<AbstractCfgNode> postorder = this.dfPostOrder();
        ListIterator<AbstractCfgNode> iter = postorder.listIterator(postorder.size());
        int i = 0;
        while (iter.hasPrevious()) {
            AbstractCfgNode cfgNode = iter.previous();
            cfgNode.setReversePostOrder(i);
            i++;
        }
    }

    // returns the number of nodes in this ControlFlowGraph
    public int size() {
        Set<AbstractCfgNode> visited = new HashSet<>();
        int s = size(this.head, visited);
        //visited = null;
        return s;
    }

    // helper function for size();
    // recursively calculates the number of successor nodes
    int size(AbstractCfgNode node, Set<AbstractCfgNode> visited) {

        if (!visited.contains(node)) {
            visited.add(node);
            int size = 1;
            CfgEdge[] outEdges = node.getOutEdges();
            for (CfgEdge outEdge : outEdges) {
                if (outEdge != null) {
                    size += size(outEdge.getDest(), visited);
                }
            }
            return size;
        } else {
            return 0;
        }
    }

// bfIterator **********************************************************************

    // breadth first iterator;
    // NOTE: when iterating over large CFGs, use should better use
    // dfPreOrderIterator; bfIterator tends to produce stack overflows
    public Iterator<AbstractCfgNode> bfIterator() {
        // list for the iterator
        LinkedList<AbstractCfgNode> list = new LinkedList<>();

        // queue for nodes that still have to be visited
        LinkedList<AbstractCfgNode> queue = new LinkedList<>();

        Set<AbstractCfgNode> visited = new HashSet<>();

        queue.add(this.head);
        visited.add(this.head);

        this.bfIteratorHelper(list, queue, visited);

        return list.iterator();
    }

// bfIteratorHelper ****************************************************************

    private void bfIteratorHelper(List<AbstractCfgNode> list, LinkedList<AbstractCfgNode> queue,
                                  Set<AbstractCfgNode> visited) {

        AbstractCfgNode cfgNode = queue.removeFirst();
        list.add(cfgNode);

        // handle successors
        for (int i = 0; i < 2; i++) {
            CfgEdge outEdge = cfgNode.getOutEdge(i);
            if (outEdge != null) {
                AbstractCfgNode succ = outEdge.getDest();
                // for all successors that have not been visited yet...
                if (!visited.contains(succ)) {
                    // add it to the queue
                    queue.add(succ);
                    // mark it as visited
                    visited.add(succ);
                }
            }
        }

        // if the queue is non-empty: recurse
        if (queue.size() > 0) {
            bfIteratorHelper(list, queue, visited);
        }
    }

//  depth first iterators **********************************************************

    // depth first iterator (preorder)
    public LinkedList<AbstractCfgNode> dfPreOrder() {
        LinkedList<AbstractCfgNode> preorder = new LinkedList<>();
        LinkedList<AbstractCfgNode> postorder = new LinkedList<>();
        this.dfIterator(preorder, postorder);
        return preorder;
    }

    // depth first iterator (postorder)
    public LinkedList<AbstractCfgNode> dfPostOrder() {
        LinkedList<AbstractCfgNode> preorder = new LinkedList<>();
        LinkedList<AbstractCfgNode> postorder = new LinkedList<>();
        this.dfIterator(preorder, postorder);
        return postorder;
    }

    // uses the given lists as containers for preorder and postorder
    private void dfIterator(LinkedList<AbstractCfgNode> preorder, LinkedList<AbstractCfgNode> postorder) {

        // auxiliary stack and visited set
        LinkedList<AbstractCfgNode> stack = new LinkedList<>();
        Set<AbstractCfgNode> visited = new HashSet<>();

        // visit head:
        // mark it as visited, add it to stack and preorder
        AbstractCfgNode current = this.head;
        visited.add(current);
        stack.add(current);
        preorder.add(current);

        // how it works:
        // while there is something on the stack:
        // - mark the top stack element as visited
        // - add it to the preorder list
        // - try to get an unvisited successor of this element
        // - if there is such a successor: push it on the stack and continue
        // - else: pop the stack and add the popped element to the postorder list
        while (!stack.isEmpty()) {

            // inspect the top stack element
            current = stack.getLast();

            // we will try to get an unvisited successor element
            AbstractCfgNode next = null;
            for (int i = 0; (i < 2) && (next == null); i++) {
                CfgEdge outEdge = current.getOutEdge(i);
                if (outEdge != null) {
                    next = outEdge.getDest();
                    if (visited.contains(next)) {
                        // try another one
                        next = null;
                    } else {
                        // found it!
                    }
                }
            }

            if (next == null) {
                // pop from stack and add it to the postorder list
                postorder.add(stack.removeLast());
            } else {
                // visit next
                visited.add(next);
                stack.add(next);
                preorder.add(next);
            }
        }
    }
}
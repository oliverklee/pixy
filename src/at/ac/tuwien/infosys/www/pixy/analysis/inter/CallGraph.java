package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

import java.util.*;

/**
 * This is a fine-grained call graph (i.e., it does not only contain edges between functions, but also edges between
 * call nodes and functions).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallGraph {
    private Map<TacFunction, CallGraphNode> nodes;
    private TacFunction mainFunction;

    CallGraph(TacFunction mainFunction) {
        this.nodes = new HashMap<>();
        this.mainFunction = mainFunction;
        this.nodes.put(mainFunction, new CallGraphNode(mainFunction));
    }

    public void add(TacFunction caller, TacFunction callee, CfgNodeCall callNode) {

        // add caller node (if necessary)
        CallGraphNode callerNode = this.nodes.get(caller);
        if (callerNode == null) {
            callerNode = new CallGraphNode(caller);
            this.nodes.put(caller, callerNode);
        }

        // add callee node (if necessary)
        CallGraphNode calleeNode = this.nodes.get(callee);
        if (calleeNode == null) {
            calleeNode = new CallGraphNode(callee);
            this.nodes.put(callee, calleeNode);
        }

        callerNode.addCallee(callNode, calleeNode);
        calleeNode.addCaller(callNode, callerNode);
    }

    // computes the postorder on the call graph
    public Map<TacFunction, Integer> getPostOrder() {

        List<CallGraphNode> postorder = new LinkedList<>();

        // auxiliary stack and visited set
        LinkedList<CallGraphNode> stack = new LinkedList<>();
        Set<CallGraphNode> visited = new HashSet<>();

        stack.add(this.nodes.get(this.mainFunction));

        while (!stack.isEmpty()) {

            // mark the top stack element as visited
            CallGraphNode node = stack.getLast();
            visited.add(node);

            // we will try to get an unvisited successor element
            CallGraphNode nextNode = null;
            Iterator<CallGraphNode> calleeIter = node.getSuccessors().iterator();
            while (calleeIter.hasNext() && nextNode == null) {
                CallGraphNode callee = calleeIter.next();
                if (!visited.contains(callee)) {
                    nextNode = callee;
                }
            }

            if (nextNode == null) {
                // pop from stack and add it to the postorder list
                postorder.add(stack.removeLast());
            } else {
                // push to stack
                stack.add(nextNode);
            }
        }

        Map<TacFunction, Integer> retMe = new HashMap<>();
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

    public Set<CfgNodeCall> getCallsTo(TacFunction f) {
        return this.nodes.get(f).getCallsTo();
    }

    // is the given function reachable from the main function?
    // (i.e., is it part of the call graph?)
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
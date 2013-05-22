package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgEntry;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgExit;

import java.util.*;

/**
 * Computes a reverse post-order for the whole, interprocedural cfg.
 *
 * Currently only works for call-string analysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class InterproceduralWorklistOrder {
    // this is what we want to compute: a mapping of interprocedural
    // worklist elements to some number (order)
    private Map<InterproceduralWorklistElement, Integer> order;

//  ********************************************************************************

    public InterproceduralWorklistOrder(TacConverter tac, ConnectorComputation cc) {
        this.order = new HashMap<>();

        TacFunction mainFunction = tac.getMainFunction();
        AbstractCfgNode startNode = mainFunction.getControlFlowGraph().getHead();

        Map<TacFunction, EncodedCallStrings> function2ECS = cc.getFunction2ECS();
        EncodedCallStrings mainEncodedCallStrings = function2ECS.get(mainFunction);
        if (mainEncodedCallStrings.size() != 1) {
            throw new RuntimeException("SNH");
        }

        InterproceduralWorklistElement start = new InterproceduralWorklistElement(startNode, new CallStringContext(0));
        LinkedList<InterproceduralWorklistElement> postorder = this.getPostorder(start, cc);

        // get *reverse* postorder
        ListIterator<InterproceduralWorklistElement> iter = postorder.listIterator(postorder.size());
        int i = 0;
        while (iter.hasPrevious()) {
            InterproceduralWorklistElement iwle = iter.previous();
            this.order.put(iwle, i);
            i++;
        }
    }

//  ********************************************************************************

    // non-recursive postorder
    private LinkedList<InterproceduralWorklistElement> getPostorder(
        InterproceduralWorklistElement start, ConnectorComputation cc) {

        // this is what we want to compute
        LinkedList<InterproceduralWorklistElement> postorder = new LinkedList<>();

        // auxiliary stack and visited set
        LinkedList<InterproceduralWorklistElement> stack = new LinkedList<>();
        Set<InterproceduralWorklistElement> visited = new HashSet<>();

        // begin with start element
        stack.add(start);

        // how it works:
        // while there is something on the stack:
        // - mark the top stack element as visited
        // - try to get an unvisited successor of this element
        // - if there is such a successor: push it on the stack and continue
        // - else: pop the stack and add the popped element to the postorder list
        while (!stack.isEmpty()) {

            // mark the top stack element as visited
            InterproceduralWorklistElement element = stack.getLast();
            visited.add(element);

            // interior of this element
            AbstractCfgNode cfgNode = element.getCfgNode();
            CallStringContext context = (CallStringContext) element.getContext();

            // we will try to get an unvisited successor element
            InterproceduralWorklistElement nextElement = null;

            if (cfgNode instanceof Call) {

                // in case of a call node, we have to distinguish between
                // unknown calls (no callee available) and known calls

                Call callNode = (Call) cfgNode;
                TacFunction callee = callNode.getCallee();
                if (callee == null) {
                    // for unknown calls:
                    // simply move on to the callret node; context stays the same

                    AbstractCfgNode retNode = callNode.getSuccessor(0);
                    nextElement = new InterproceduralWorklistElement(retNode, context);

                    if (visited.contains(nextElement)) {
                        nextElement = null;
                    }
                } else {
                    // for normal calls:
                    // enter function under corresponding context

                    CfgEntry entryNode = (CfgEntry) callee.getControlFlowGraph().getHead();
                    AbstractContext propagationContext = cc.getTargetContext(callNode, context.getPosition());
                    if (propagationContext == null) {
                        throw new RuntimeException("SNH: " + callNode.getLoc());
                    }
                    nextElement = new InterproceduralWorklistElement(entryNode, propagationContext);

                    if (visited.contains(nextElement)) {
                        nextElement = null;
                    }
                }
            } else if (cfgNode instanceof CfgExit) {
                CfgExit exitNode = (CfgExit) cfgNode;
                TacFunction exitedFunction = exitNode.getEnclosingFunction();

                // only proceed if this is not the exit node of the main function
                if (!exitedFunction.isMain()) {

                    // an exit node can have several "reverse targets";
                    // a reverse target consists of one call node and one or more contexts

                    Iterator<ReverseTarget> revTargetsIter = cc.getReverseTargets(exitedFunction, context.getPosition()).iterator();
                    while ((nextElement == null) && revTargetsIter.hasNext()) {

                        ReverseTarget revTarget = revTargetsIter.next();
                        Call revCall = revTarget.getCallNode();
                        AbstractCfgNode revRet = revCall.getSuccessor(0);
                        Iterator<? extends AbstractContext> reverseContextsIter = revTarget.getContexts().iterator();

                        while ((nextElement == null) && reverseContextsIter.hasNext()) {

                            AbstractContext reverseContext = reverseContextsIter.next();
                            nextElement = new InterproceduralWorklistElement(revRet, reverseContext);

                            if (visited.contains(nextElement)) {
                                // try the next one
                                nextElement = null;
                            } else {
                                // found it!
                            }
                        }
                    }
                }
            } else {

                // handle successors
                for (int i = 0; (i < 2) && (nextElement == null); i++) {
                    CfgEdge outEdge = cfgNode.getOutEdge(i);
                    if (outEdge != null) {
                        AbstractCfgNode succNode = outEdge.getDest();
                        nextElement = new InterproceduralWorklistElement(succNode, context);
                        if (visited.contains(nextElement)) {
                            // try next one
                            nextElement = null;
                        } else {
                            // found it!
                        }
                    }
                }
            }

            if (nextElement == null) {
                // pop from stack and add it to the postorder list
                postorder.add(stack.removeLast());
            } else {
                // push to stack
                stack.add(nextElement);
            }
        }

        return postorder;
    }

//  ********************************************************************************

    public Integer getReversePostOrder(InterproceduralWorklistElement element) {
        return this.order.get(element);
    }
}
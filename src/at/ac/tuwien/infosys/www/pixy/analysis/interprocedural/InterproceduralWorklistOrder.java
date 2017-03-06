package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgEntry;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgExit;

public class InterproceduralWorklistOrder {

	private Map<InterproceduralWorklistElement, Integer> order;

	public InterproceduralWorklistOrder(TacConverter tac, ConnectorComputation cc) {

		this.order = new HashMap<InterproceduralWorklistElement, Integer>();

		TacFunction mainFunction = tac.getMainFunction();
		AbstractCfgNode startNode = mainFunction.getCfg().getHead();

		Map<TacFunction, EncodedCallStrings> function2ECS = cc.getFunction2ECS();
		EncodedCallStrings mainECS = function2ECS.get(mainFunction);
		if (mainECS.size() != 1) {
			throw new RuntimeException("SNH");
		}

		InterproceduralWorklistElement start = new InterproceduralWorklistElement(startNode, new CallStringContext(0));
		LinkedList<InterproceduralWorklistElement> postorder = this.getPostorder(start, cc);

		ListIterator<InterproceduralWorklistElement> iter = postorder.listIterator(postorder.size());
		int i = 0;
		while (iter.hasPrevious()) {
			InterproceduralWorklistElement iwle = (InterproceduralWorklistElement) iter.previous();
			this.order.put(iwle, i);
			i++;
		}
	}

	private LinkedList<InterproceduralWorklistElement> getPostorder(InterproceduralWorklistElement start,
			ConnectorComputation cc) {

		LinkedList<InterproceduralWorklistElement> postorder = new LinkedList<InterproceduralWorklistElement>();

		LinkedList<InterproceduralWorklistElement> stack = new LinkedList<InterproceduralWorklistElement>();
		Set<InterproceduralWorklistElement> visited = new HashSet<InterproceduralWorklistElement>();

		stack.add(start);
		while (!stack.isEmpty()) {

			InterproceduralWorklistElement element = stack.getLast();
			visited.add(element);

			AbstractCfgNode cfgNode = element.getCfgNode();
			CallStringContext context = (CallStringContext) element.getContext();

			InterproceduralWorklistElement nextElement = null;

			if (cfgNode instanceof Call) {

				Call callNode = (Call) cfgNode;
				TacFunction callee = callNode.getCallee();
				if (callee == null) {
					AbstractCfgNode retNode = callNode.getSuccessor(0);
					nextElement = new InterproceduralWorklistElement(retNode, context);

					if (visited.contains(nextElement)) {
						nextElement = null;
					}
				} else {
					CfgEntry entryNode = (CfgEntry) callee.getCfg().getHead();
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

				if (!exitedFunction.isMain()) {
					Iterator<ReverseTarget> revTargetsIter = cc.getReverseTargets(exitedFunction, context.getPosition())
							.iterator();
					while ((nextElement == null) && revTargetsIter.hasNext()) {

						ReverseTarget revTarget = revTargetsIter.next();
						Call revCall = revTarget.getCallNode();
						AbstractCfgNode revRet = revCall.getSuccessor(0);
						Iterator<? extends AbstractContext> reverseContextsIter = revTarget.getContexts().iterator();

						while ((nextElement == null) && reverseContextsIter.hasNext()) {

							AbstractContext reverseContext = reverseContextsIter.next();
							nextElement = new InterproceduralWorklistElement(revRet, reverseContext);

							if (visited.contains(nextElement)) {
								nextElement = null;
							} else {
							}
						}
					}
				}

			} else {
				for (int i = 0; (i < 2) && (nextElement == null); i++) {
					CfgEdge outEdge = cfgNode.getOutEdge(i);
					if (outEdge != null) {
						AbstractCfgNode succNode = outEdge.getDest();
						nextElement = new InterproceduralWorklistElement(succNode, context);
						if (visited.contains(nextElement)) {
							nextElement = null;
						} else {
						}
					}
				}
			}

			if (nextElement == null) {
				postorder.add(stack.removeLast());
			} else {
				stack.add(nextElement);
			}
		}
		return postorder;
	}

	public Integer getReversePostOrder(InterproceduralWorklistElement element) {
		return this.order.get(element);
	}
}

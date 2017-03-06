package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

import java.io.*;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.VulnerabilityAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLabel;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgEntry;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgExit;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Empty;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.EmptyTest;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Eval;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Global;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Hotspot;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Static;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset;
import at.ac.tuwien.infosys.www.pixy.sanitation.AbstractSanitationAnalysis;
import at.ac.tuwien.infosys.www.pixy.sanitation.FSAAutomaton;

public class DependencyGraph {

	private Map<AbstractNode, AbstractNode> nodes;

	private NormalNode root;

	private Map<AbstractNode, List<AbstractNode>> edges;

	private InterproceduralAnalysisInformation analysisInfo;

	private SymbolTable mainSymTab;

	private DependencyAnalysis depAnalysis;

	private int n;

	private boolean leavesReduced = false;

	private DependencyGraph() {
	}

	public DependencyGraph(NormalNode root) {
		this.nodes = new LinkedHashMap<AbstractNode, AbstractNode>();
		this.addNode(root);
		this.root = root;
		this.edges = new LinkedHashMap<AbstractNode, List<AbstractNode>>();
		this.analysisInfo = null;
		this.mainSymTab = null;
		this.depAnalysis = null;
	}

	public static DependencyGraph create(AbstractTacPlace place, AbstractCfgNode start,
			InterproceduralAnalysisInformation analysisInfo, SymbolTable mainSymTab, DependencyAnalysis depAnalysis) {

		DependencyGraph depGraph = new DependencyGraph();
		depGraph.nodes = new LinkedHashMap<AbstractNode, AbstractNode>();
		depGraph.edges = new LinkedHashMap<AbstractNode, List<AbstractNode>>();
		depGraph.analysisInfo = analysisInfo;
		depGraph.mainSymTab = mainSymTab;
		depGraph.depAnalysis = depAnalysis;
		List<AbstractTacPlace> indices = new LinkedList<AbstractTacPlace>();
		try {
			Set<AbstractContext> allC = analysisInfo.getAnalysisNode(start).getContexts();
			depGraph.root = (NormalNode) depGraph.makeDepGraph(place, start, ControlFlowGraph.getFunction(start),
					indices, allC);
		} catch (NotReachableException ex) {

			debug("not reachable!!!");
		}

		return depGraph;

	}

	public DependencyGraph(DependencyGraph orig) {

		this.nodes = new LinkedHashMap<AbstractNode, AbstractNode>(orig.nodes);
		this.root = orig.root;

		this.edges = new LinkedHashMap<AbstractNode, List<AbstractNode>>();
		for (Map.Entry<AbstractNode, List<AbstractNode>> origEntry : orig.edges.entrySet()) {
			AbstractNode origFrom = origEntry.getKey();
			List<AbstractNode> origTos = origEntry.getValue();
			List<AbstractNode> myTos = new LinkedList<AbstractNode>(origTos);
			this.edges.put(origFrom, myTos);
		}
		this.analysisInfo = orig.analysisInfo;
		this.mainSymTab = orig.mainSymTab;
		this.depAnalysis = orig.depAnalysis;
	}

	private static void debug(String s) {
		int a = 1;
		if (a == 2) {
			System.out.println(s);
		}
	}

	private AbstractNode makeDepGraph(AbstractTacPlace place, AbstractCfgNode current, TacFunction function,
			List<AbstractTacPlace> indices, Set<AbstractContext> contexts) throws NotReachableException {

		debug("  visiting: " + current.getClass() + ", " + current.getOriginalLineNumber() + ", " + place);
		debug(current.toString());
		debug("in function : " + function.getName());
		debug("under contexts: " + contexts);

		AbstractNode dgn = new NormalNode(place, current);

		if (this.nodes.containsKey(dgn)) {
			debug("loop!");
			return this.nodes.get(dgn);
		}

		addNode(dgn);

		if (place instanceof Literal) {
			debug("literal!");
			return dgn;
		}

		DependencySet depSet = this.getDepSet(current, place, contexts);

		debug("start going to nodes...");
		for (DependencyLabel dep : depSet.getDepSet()) {
			if (dep == DependencyLabel.UNINIT) {

				debug("uninit!");
				UninitializedNode uninitNode = new UninitializedNode();
				addNode(uninitNode);
				addEdge(dgn, uninitNode);
			} else {
				debug("getting used places for " + dep.getCfgNode().getOriginalLineNumber());

				AbstractCfgNode targetNode = dep.getCfgNode();

				AbstractNode connectWith = this.checkOp(targetNode);
				if (connectWith == null) {
					connectWith = dgn;
				} else if (this.nodes.containsKey(connectWith)) {
					connectWith = this.nodes.get(connectWith);
					addEdge(dgn, connectWith);
					continue;
				} else {
					addNode(connectWith);
					addEdge(dgn, connectWith);
				}
				List<AbstractTacPlace> newIndices = new LinkedList<AbstractTacPlace>();

				ContextSwitch cswitch = this.switchContexts(function, contexts, current, targetNode);
				TacFunction targetFunction = cswitch.targetFunction;
				Set<AbstractContext> targetContexts = cswitch.targetContexts;

				for (AbstractTacPlace used : this.getUsedPlaces(targetNode, place, indices, newIndices)) {

					addEdge(connectWith, makeDepGraph(used, targetNode, targetFunction, newIndices, targetContexts));
				}
			}
		}
		debug("...end going to nodes");

		return dgn;
	}

	private DependencySet getDepSet(AbstractCfgNode cfgNode, AbstractTacPlace place, Set<AbstractContext> contexts)
			throws NotReachableException {

		DependencySet depSet = null;
		AbstractCfgNode enclosingX = cfgNode.getSpecial();
		if (enclosingX instanceof BasicBlock) {

			Map<?, ?> bbPhi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();

			if (bbPhi.isEmpty()) {
				throw new NotReachableException();
			}

			DependencyLatticeElement latticeElement = this.newFold(bbPhi, contexts);

			DependencyLatticeElement propagated = this.depAnalysis.applyInsideBasicBlock((BasicBlock) enclosingX,
					cfgNode, latticeElement);
			depSet = propagated.getDep(place);

		} else if (enclosingX instanceof CfgEntry) {
			if (place.isConstant()) {

				Map<?, ?> phi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();
				if (phi.isEmpty()) {
					throw new NotReachableException();
				}

				depSet = this.newFold(phi, place, contexts);

			} else {
				AbstractCfgNode defaultHead = ControlFlowGraph.getHead(cfgNode);
				Map<?, ?> bbPhi = this.analysisInfo.getAnalysisNode(enclosingX).getPhi();
				DependencyLatticeElement latticeElement = this.newFold(bbPhi, contexts);
				DependencyLatticeElement propagated = this.depAnalysis.applyInsideDefaultCfg(defaultHead, cfgNode,
						latticeElement);
				depSet = propagated.getDep(place);
			}

		} else {

			Map<?, ?> phi = this.analysisInfo.getAnalysisNode(cfgNode).getPhi();
			if (phi.isEmpty()) {
				throw new NotReachableException();
			}

			try {
				depSet = this.newFold(phi, place, contexts);
			} catch (NullPointerException e) {
				System.out.println(cfgNode.getLoc());
				System.out.println(place);
				throw e;
			}
		}

		return depSet;
	}

	private ContextSwitch switchContexts(TacFunction function, Set<AbstractContext> contexts, AbstractCfgNode current,
			AbstractCfgNode targetNode) {

		ContextSwitch retMe = new ContextSwitch();
		TacFunction targetFunction = function;
		Set<AbstractContext> targetContexts = contexts;

		if (current instanceof CallReturn) {
			CallReturn callRet = (CallReturn) current;
			Call callNode = callRet.getCallNode();
			targetFunction = callNode.getCallee();
			debug("jumping from caller to end of callee: " + function.getName() + " -> " + targetFunction.getName());
			targetContexts = new HashSet<AbstractContext>();
			for (AbstractContext c : contexts) {
				targetContexts.add(this.depAnalysis.getPropagationContext(callNode, c));
			}
			debug("target contexts: " + targetContexts);
		} else if (targetNode instanceof CallPreparation) {
			debug("jumping from start of callee to caller!");
			CallPreparation prep = (CallPreparation) targetNode;
			Call callNode = (Call) prep.getSuccessor(0);
			targetFunction = prep.getCaller();
			debug("caller: " + targetFunction.getName());
			targetContexts = new HashSet<AbstractContext>();
			for (AbstractContext c : contexts) {
				List<ReverseTarget> revs = this.depAnalysis.getReverseTargets(function, c);
				for (ReverseTarget rev : revs) {
					if (!rev.getCallNode().equals(callNode)) {
						continue;
					}
					targetContexts.addAll(rev.getContexts());
				}
			}
			if (targetContexts.isEmpty()) {
				throw new RuntimeException("SNH");
			}
			debug("target contexts: " + targetContexts);
		} else if (targetNode instanceof Define) {
			targetFunction = ControlFlowGraph.getFunction(targetNode);
			targetContexts = this.analysisInfo.getAnalysisNode(targetNode.getSpecial()).getContexts();
			if (targetContexts.isEmpty()) {
				throw new RuntimeException("SNH");
			}
		} else if (!ControlFlowGraph.getFunction(targetNode).equals(function)) {
			debug("Unexpected function change: " + function.getName() + " -> "
					+ ControlFlowGraph.getFunction(targetNode).getName());
			targetFunction = ControlFlowGraph.getFunction(targetNode);
			targetContexts = this.analysisInfo.getAnalysisNode(targetNode.getSpecial()).getContexts();
			if (targetContexts.isEmpty()) {
				throw new RuntimeException("SNH");
			}
		}

		retMe.targetFunction = targetFunction;
		retMe.targetContexts = targetContexts;
		return retMe;

	}

	private AbstractNode checkOp(AbstractCfgNode targetNode) {

		if (targetNode instanceof AssignBinary) {
			AssignBinary inspectMe = (AssignBinary) targetNode;
			return new BuiltinFunctionNode(targetNode, TacOperators.opToName(inspectMe.getOperator()), true);
		} else if (targetNode instanceof AssignUnary) {
			AssignUnary inspectMe = (AssignUnary) targetNode;
			return new BuiltinFunctionNode(targetNode, TacOperators.opToName(inspectMe.getOperator()), true);
		} else if (targetNode instanceof CallReturn) {

			CallReturn inspectMe = (CallReturn) targetNode;
			CallPreparation prepNode = inspectMe.getCallPrepNode();

			if (prepNode.getCallee() == null) {
				throw new RuntimeException("SNH");
			} else {
				return null;
			}

		} else if (targetNode instanceof CallBuiltinFunction) {
			CallBuiltinFunction cfgNode = (CallBuiltinFunction) targetNode;
			String functionName = cfgNode.getFunctionName();
			return new BuiltinFunctionNode(targetNode, functionName, true);

		} else if (targetNode instanceof CallUnknownFunction) {
			CallUnknownFunction callNode = (CallUnknownFunction) targetNode;
			String functionName = callNode.getFunctionName();
			boolean builtin = false;
			return new BuiltinFunctionNode(targetNode, functionName, builtin);
		}

		return null;

	}

	public AbstractNode addNode(AbstractNode node) {
		if (this.nodes.containsKey(node)) {
			throw new RuntimeException("SNH");
		}
		this.nodes.put(node, node);
		return node;
	}

	public boolean containsNode(AbstractNode node) {
		return this.nodes.containsKey(node);
	}

	public void addEdge(AbstractNode from, AbstractNode to) {
		if (!this.nodes.containsKey(from) || !this.nodes.containsKey(to)) {
			throw new RuntimeException("SNH");
		}
		List<AbstractNode> toList = this.edges.get(from);
		if (toList == null) {
			toList = new LinkedList<AbstractNode>();
			this.edges.put(from, toList);
		}
		toList.add(to);

	}

	private List<AbstractTacPlace> getUsedPlaces(AbstractCfgNode cfgNodeX, AbstractTacPlace victim,
			List<AbstractTacPlace> oldIndices, List<AbstractTacPlace> newIndices) {

		List<AbstractTacPlace> retMe = new LinkedList<AbstractTacPlace>();
		if (cfgNodeX instanceof AssignArray) {
			retMe.add(new Literal(""));

		} else if (cfgNodeX instanceof AssignBinary) {
			AssignBinary cfgNode = (AssignBinary) cfgNodeX;
			retMe.add(cfgNode.getLeftOperand());
			retMe.add(cfgNode.getRightOperand());

		} else if (cfgNodeX instanceof AssignReference) {
			AssignReference cfgNode = (AssignReference) cfgNodeX;
			retMe.add(cfgNode.getRight());

		} else if (cfgNodeX instanceof AssignSimple) {

			AssignSimple cfgNode = (AssignSimple) cfgNodeX;
			Variable left = (Variable) cfgNode.getLeft();
			AbstractTacPlace right = cfgNode.getRight();

			if (!oldIndices.isEmpty() && right.isVariable()) {
				retMe.add(getCorresponding(left, victim.getVariable(), right.getVariable(), oldIndices, newIndices));

			} else if (victim.isVariable() && victim.getVariable().isArrayElement()
					&& victim.getVariable().isArrayElementOf(left)) {

				if (!right.isVariable()) {
					retMe.add(right);

				} else {
					retMe.add(
							getCorresponding(left, victim.getVariable(), right.getVariable(), oldIndices, newIndices));
				}

			} else {
				retMe.add(right);
			}

		} else if (cfgNodeX instanceof AssignUnary) {
			AssignUnary cfgNode = (AssignUnary) cfgNodeX;
			retMe.add(cfgNode.getRight());
		} else if (cfgNodeX instanceof BasicBlock) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Call) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof CallPreparation) {

			CallPreparation cfgNode = (CallPreparation) cfgNodeX;
			List<TacActualParameter> actualParams = cfgNode.getParamList();
			List<TacFormalParameter> formalParams = cfgNode.getCallee().getParams();
			int index = -1;
			int i = 0;

			for (TacFormalParameter formalParam : formalParams) {
				TacActualParameter actualParam = actualParams.get(i);

				if (formalParam.getVariable().equals(victim)) {

					if (oldIndices.isEmpty()) {
						retMe.add(actualParam.getPlace());
					} else {
						if (!actualParam.getPlace().isVariable()) {
							retMe.add(actualParam.getPlace());
						} else {
							retMe.add(getCorresponding(formalParam.getVariable(), victim.getVariable(),
									actualParam.getPlace().getVariable(), oldIndices, newIndices));
						}
					}

					index = i;
					break;
				}

				if (victim.isVariable() && victim.getVariable().isArrayElement()
						&& victim.getVariable().isArrayElementOf(formalParam.getVariable())) {

					if (!actualParam.getPlace().isVariable()) {
						retMe.add(actualParam.getPlace());
					} else {
						retMe.add(getCorresponding(formalParam.getVariable(), victim.getVariable(),
								actualParam.getPlace().getVariable(), oldIndices, newIndices));
					}

					index = i;
					break;
				}

				i++;
			}

			if (index == -1) {
				System.out.println("victim: " + victim);
				throw new RuntimeException("SNH");
			}

		} else if (cfgNodeX instanceof CallReturn) {

			return this.getUsedPlacesForCall((CallReturn) cfgNodeX, victim, oldIndices, newIndices);

		} else if (cfgNodeX instanceof CallBuiltinFunction) {
			return this.getUsedPlacesForBuiltin((CallBuiltinFunction) cfgNodeX);

		} else if (cfgNodeX instanceof CallUnknownFunction) {
			CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
			for (TacActualParameter param : cfgNode.getParamList()) {
				retMe.add(param.getPlace());
			}
		} else if (cfgNodeX instanceof Define) {
			Define cfgNode = (Define) cfgNodeX;
			retMe.add(cfgNode.getSetTo());
		} else if (cfgNodeX instanceof Echo) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Empty) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof CfgExit) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof EmptyTest) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Eval) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof CfgExit) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Global) {
			Global cfgNode = (Global) cfgNodeX;
			Variable realGlobal = mainSymTab.getVariable(cfgNode.getOperand().getName());
			if (realGlobal == null) {
				throw new RuntimeException("SNH");
			}
			retMe.add(realGlobal);
		} else if (cfgNodeX instanceof Hotspot) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof If) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Include) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof IncludeEnd) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof IncludeStart) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Isset) {
		} else if (cfgNodeX instanceof Static) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Tester) {
			throw new RuntimeException("SNH");
		} else if (cfgNodeX instanceof Unset) {
			retMe.add(new Literal(""));
		} else {
			throw new RuntimeException("not yet: " + cfgNodeX.getClass());
		}
		return retMe;
	}

	private Variable getCorresponding(Variable left, Variable victim, Variable right, List<AbstractTacPlace> oldIndices,
			List<AbstractTacPlace> newIndices) {

		if (!victim.isArrayElementOf(left)) {
			victim = left;
		}
		List<AbstractTacPlace> leftIndices = left.getIndices();
		List<AbstractTacPlace> victimIndices = victim.getIndices();
		for (Iterator<AbstractTacPlace> oldIter = oldIndices.iterator(); oldIter.hasNext();) {
			AbstractTacPlace oldIndex = (AbstractTacPlace) oldIter.next();
			victimIndices.add(oldIndex);
		}
		ListIterator<AbstractTacPlace> victimIter = victimIndices.listIterator();
		for (int i = 0; i < leftIndices.size(); i++) {
			victimIter.next();
		}
		Variable retMe = right;
		while (victimIter.hasNext()) {
			AbstractTacPlace victimIndex = (AbstractTacPlace) victimIter.next();
			Variable newTarget = retMe.getElement(victimIndex);
			if (newTarget == null) {
				victimIter.previous();
				break;
			} else {
				retMe = retMe.getElement(victimIndex);
			}
		}
		while (victimIter.hasNext()) {
			AbstractTacPlace victimIndex = (AbstractTacPlace) victimIter.next();
			newIndices.add(victimIndex);
		}

		return retMe;

	}

	private List<AbstractTacPlace> getUsedPlacesForCall(CallReturn retNode, AbstractTacPlace victim,
			List<AbstractTacPlace> oldIndices, List<AbstractTacPlace> newIndices) {

		List<AbstractTacPlace> retMe = new LinkedList<AbstractTacPlace>();

		CallPreparation prepNode = retNode.getCallPrepNode();
		if (prepNode.getCallee() == null) {
			throw new RuntimeException("SNH");
		}

		Variable retVar = retNode.getRetVar();

		if (!oldIndices.isEmpty()) {

			retMe.add(getCorresponding(victim.getVariable(), victim.getVariable(), retVar, oldIndices, newIndices));
		} else {
			retMe.add(retVar);
		}

		return retMe;
	}

	private List<AbstractTacPlace> getUsedPlacesForBuiltin(CallBuiltinFunction cfgNode) {

		List<AbstractTacPlace> retMe = new LinkedList<AbstractTacPlace>();
		String functionName = cfgNode.getFunctionName();

		if (functionName.equals("mysql_query")) {

		} else {
			for (TacActualParameter param : cfgNode.getParamList()) {
				retMe.add(param.getPlace());
			}
		}
		return retMe;
	}

	private DependencySet newFold(Map<?, ?> phi, AbstractTacPlace place, Set<AbstractContext> contexts) {

		DependencySet depSet = null;

		for (AbstractContext context : contexts) {
			DependencyLatticeElement element = (DependencyLatticeElement) phi.get(context);
			if (element == null) {
				continue;
			}
			if (depSet == null) {
				depSet = element.getDep(place);
			} else {
				depSet = DependencySet.lub(depSet, element.getDep(place));
			}
		}

		return depSet;
	}

	private DependencyLatticeElement newFold(Map<?, ?> phi, Set<AbstractContext> contexts) {

		DependencyLatticeElement retMe = null;

		for (AbstractContext context : contexts) {
			DependencyLatticeElement element = (DependencyLatticeElement) phi.get(context);
			if (retMe == null) {
				retMe = new DependencyLatticeElement(element);
			} else {
				retMe = (DependencyLatticeElement) this.depAnalysis.getLattice().lub(element, retMe);
			}
		}

		return retMe;
	}

	public boolean isTree() {
		int edgeCount = 0;
		for (Map.Entry<AbstractNode, List<AbstractNode>> entry : this.edges.entrySet()) {
			edgeCount += entry.getValue().size();
		}

		System.out.println("nodes size: " + this.nodes.size());
		System.out.println("edges size: " + edgeCount);

		if (this.nodes.size() == edgeCount + 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasCycles() {

		HashMap<AbstractNode, Integer> colorMap = new HashMap<AbstractNode, Integer>();

		for (AbstractNode node : this.nodes.keySet()) {
			colorMap.put(node, 0);
		}

		for (AbstractNode node : this.nodes.keySet()) {
			if (hasCyclesHelper(node, colorMap)) {
				return true;
			}
		}
		return false;

	}

	private boolean hasCyclesHelper(AbstractNode node, HashMap<AbstractNode, Integer> colorMap) {

		colorMap.put(node, 1);

		List<?> successors = this.edges.get(node);
		if (successors != null) {
			for (AbstractNode succ : this.edges.get(node)) {
				int color = colorMap.get(succ);
				if (color == 1) {
					return true;
				} else if (color == 0) {
					if (hasCyclesHelper(succ, colorMap)) {
						return true;
					}
				}
			}
		}

		colorMap.put(node, 2);

		return false;
	}

	public Map<String, Integer> getOpMap() {
		Map<String, Integer> retMe = new HashMap<String, Integer>();
		for (AbstractNode node : this.nodes.keySet()) {
			if (!(node instanceof BuiltinFunctionNode)) {
				continue;
			}
			BuiltinFunctionNode opNode = (BuiltinFunctionNode) node;
			String opName = opNode.getName();
			Integer opCount = retMe.get(opName);
			if (opCount == null) {
				opCount = new Integer(1);
			} else {
				opCount = new Integer(opCount.intValue() + 1);
			}
			retMe.put(opName, opCount);
		}

		return retMe;
	}

	public boolean leafsAreStrings() {
		for (AbstractNode node : this.getLeafNodes()) {
			if (!(node instanceof NormalNode)) {
				return false;
			}
			if (node instanceof CompleteGraphNode) {
				throw new RuntimeException("SNH");
			}
			if (!((NormalNode) node).isString()) {
				return false;
			}
		}
		return true;
	}

	public List<AbstractNode> getNodes() {
		List<AbstractNode> retMe = new LinkedList<AbstractNode>(this.nodes.keySet());
		return retMe;
	}

	public Set<AbstractNode> getLeafNodes() {
		Set<AbstractNode> leafCandidates = new HashSet<AbstractNode>(this.nodes.keySet());
		Set<AbstractNode> nonLeafs = this.edges.keySet();
		leafCandidates.removeAll(nonLeafs);
		return leafCandidates;
	}

	public Set<UninitializedNode> getUninitNodes() {
		Set<UninitializedNode> uninitNodes = new HashSet<UninitializedNode>();
		for (AbstractNode node : this.nodes.keySet()) {
			if (node instanceof UninitializedNode) {
				uninitNodes.add((UninitializedNode) node);
			}
		}
		return uninitNodes;
	}

	public NormalNode getRoot() {
		return this.root;
	}

	public String makeDotUnique(String graphName) {
		try {
			Writer outWriter = new StringWriter();
			this.writeDotUnique(graphName, new HashSet<AbstractNode>(), true, outWriter);
			String ret = outWriter.toString();
			outWriter.close();
			return ret;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return "";
		}
	}

	public void dumpDotUnique(String graphName, String path) {
		try {
			(new File(path)).mkdir();
			Writer outWriter = new FileWriter(path + "/" + graphName + ".dot");
			this.writeDotUnique(graphName, new HashSet<AbstractNode>(), true, outWriter);
			outWriter.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void dumpDot(String graphName, String path, VulnerabilityAnalysisInformation dci) {
		this.dumpDot(graphName, path, new HashSet<AbstractNode>(), dci);
	}

	public void dumpDot(String graphName, String path, Set<? extends AbstractNode> fillUs,
			VulnerabilityAnalysisInformation dci) {
		try {
			(new File(path)).mkdir();
			Writer outWriter = new FileWriter(path + "/" + graphName + ".dot");
			this.writeDot(graphName, fillUs, outWriter, dci);
			outWriter.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeDot(String graphName, Set<? extends AbstractNode> fillUs, Writer outWriter,
			VulnerabilityAnalysisInformation dci) throws IOException {

		if (MyOptions.option_V) {
			writeDotVerbose(graphName, fillUs, outWriter, dci);
		} else {
			writeDotNormal(graphName, fillUs, outWriter);
		}
	}

	public void writeDotVerbose(String graphName, Set<? extends AbstractNode> fillUs, Writer outWriter,
			VulnerabilityAnalysisInformation dci) throws IOException {

		outWriter.write("digraph cfg {\n  label=\"");
		outWriter.write(Dumper.escapeDot(graphName, 0));
		outWriter.write("\";\n");
		outWriter.write("  labelloc=t;\n");

		int idCounter = 0;
		HashMap<AbstractNode, Integer> node2Int = new HashMap<AbstractNode, Integer>();
		for (AbstractNode tgn : this.nodes.keySet()) {
			node2Int.put(tgn, ++idCounter);

			String styleString = "";
			if (fillUs.contains(tgn)) {
				styleString = ",style=filled";
			}

			boolean isModelled = true;
			String shapeString = "shape=box";
			if (tgn == this.root) {
				shapeString = "shape=doubleoctagon";
			} else if (tgn instanceof BuiltinFunctionNode) {
				shapeString = "shape=ellipse";
				if (dci != null) {
					isModelled = dci.isModelled(((BuiltinFunctionNode) tgn).getName());
				}
			}

			String name = tgn.dotNameVerbose(isModelled);
			outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" + name + "\"" + styleString + "];\n");

		}

		for (Map.Entry<AbstractNode, List<AbstractNode>> entry : this.edges.entrySet()) {
			AbstractNode from = entry.getKey();
			List<AbstractNode> toList = entry.getValue();
			int i = 1;
			for (AbstractNode to : toList) {
				if (from instanceof BuiltinFunctionNode) {
					if (leavesReduced) {
						outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
					} else {
						outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) + "[label=\"Param #"
								+ i++ + "\"]");
					}
					outWriter.write(";\n");
				} else {
					outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
					outWriter.write(";\n");
				}
			}
		}

		outWriter.write("}\n");
	}

	public void writeDotNormal(String graphName, Set<? extends AbstractNode> fillUs, Writer outWriter)
			throws IOException {

		outWriter.write("digraph cfg {\n  label=\"");
		outWriter.write(Dumper.escapeDot(graphName, 0));
		outWriter.write("\";\n");
		outWriter.write("  labelloc=t;\n");

		int idCounter = 0;
		HashMap<AbstractNode, Integer> node2Int = new HashMap<AbstractNode, Integer>();
		for (AbstractNode tgn : this.nodes.keySet()) {
			node2Int.put(tgn, ++idCounter);

			String styleString = "";
			if (fillUs.contains(tgn)) {
				styleString = ",style=filled";
			}

			if (tgn instanceof BuiltinFunctionNode) {
				styleString = ",style=filled,color=lightblue";
			}

			String shapeString = "shape=ellipse";
			if (tgn == this.root) {
				shapeString = "shape=box";
			}

			String name = tgn.dotName();
			outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" + name + "\"" + styleString + "];\n");

		}

		for (Map.Entry<AbstractNode, List<AbstractNode>> entry : this.edges.entrySet()) {
			AbstractNode from = entry.getKey();
			List<AbstractNode> toList = entry.getValue();
			int i = 1;
			for (AbstractNode to : toList) {
				if (from instanceof BuiltinFunctionNode) {
					if (leavesReduced) {
						outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
					} else {
						outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) + "[label=\"Param #"
								+ i++ + "\"]");
					}
					outWriter.write(";\n");
				} else {
					outWriter.write("  n" + node2Int.get(from) + " -> n" + node2Int.get(to));
					outWriter.write(";\n");
				}
			}
		}

		outWriter.write("}\n");
	}

	public void writeDotUnique(String graphName, Set<AbstractNode> fillUs, boolean shortName, Writer outWriter)
			throws IOException {

		outWriter.write("digraph cfg {\n  label=\"");
		outWriter.write(Dumper.escapeDot(graphName, 0));
		outWriter.write("\";\n");
		outWriter.write("  labelloc=t;\n");

		int idCounter = 0;
		HashMap<AbstractNode, Integer> node2Int = new HashMap<AbstractNode, Integer>();

		for (AbstractNode tgn : this.bfIterator()) {

			node2Int.put(tgn, ++idCounter);

			String styleString = "";
			if (fillUs.contains(tgn)) {
				styleString = ",style=filled";
			}

			if (tgn instanceof BuiltinFunctionNode) {
				styleString = ",style=filled,color=lightblue";
			}

			String shapeString = "shape=ellipse";
			if (tgn == this.root) {
				shapeString = "shape=box";
			}

			String name;
			if (shortName) {
				name = tgn.dotNameShort();
			} else {
				name = tgn.dotName();
			}
			outWriter.write("  n" + idCounter + " [" + shapeString + ", label=\"" + name + "\"" + styleString + "];\n");

		}

		List<String> lines = new LinkedList<String>();
		for (Map.Entry<AbstractNode, List<AbstractNode>> entry : this.edges.entrySet()) {
			AbstractNode from = entry.getKey();
			List<AbstractNode> toList = entry.getValue();
			int i = 1;
			for (AbstractNode to : toList) {
				if (from instanceof BuiltinFunctionNode) {
					lines.add("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) + "[label=\"" + i++ + "\"];");
				} else {
					lines.add("  n" + node2Int.get(from) + " -> n" + node2Int.get(to) + ";");
				}
			}
		}

		Collections.sort(lines);
		for (String line : lines) {
			outWriter.write(line);
			outWriter.write("\n");
		}

		outWriter.write("}\n");
	}

	public void eliminateCycles() {

		if (!this.hasCycles()) {
			return;
		}
		List<List<AbstractNode>> sccs = this.getSccs();
		for (List<AbstractNode> scc : sccs) {
			if (scc.size() < 2) {
				continue;
			}
			Set<AbstractNode> sccPredecessors = new HashSet<AbstractNode>();
			for (AbstractNode sccMember : scc) {
				Set<AbstractNode> predecessors = this.getPredecessors(sccMember);
				predecessors.removeAll(scc);
				sccPredecessors.addAll(predecessors);
			}
			Set<AbstractNode> sccSuccessors = new HashSet<AbstractNode>();
			for (AbstractNode sccMember : scc) {
				List<AbstractNode> successors = this.getSuccessors(sccMember);
				successors.removeAll(scc);
				sccSuccessors.addAll(successors);
			}
			for (AbstractNode sccMember : scc) {
				this.nodes.remove(sccMember);
				this.edges.remove(sccMember);
			}
			CompleteGraphNode sccNode = new CompleteGraphNode();
			this.addNode(sccNode);

			for (AbstractNode pre : sccPredecessors) {
				List<AbstractNode> out = this.edges.get(pre);
				for (Iterator<AbstractNode> iter = out.iterator(); iter.hasNext();) {
					AbstractNode outNode = (AbstractNode) iter.next();
					if (!this.nodes.containsKey(outNode)) {
						iter.remove();
					}
				}
				out.add(sccNode);
			}
			this.edges.put(sccNode, new LinkedList<AbstractNode>(sccSuccessors));
		}

	}

	public List<List<AbstractNode>> getSccs() {
		n = 1;
		List<List<AbstractNode>> sccs = new LinkedList<List<AbstractNode>>();
		List<AbstractNode> stack = new LinkedList<AbstractNode>();
		Map<AbstractNode, Integer> dfsnum = new HashMap<AbstractNode, Integer>();
		Map<AbstractNode, Integer> low = new HashMap<AbstractNode, Integer>();
		Set<AbstractNode> old = new HashSet<AbstractNode>();
		sccVisit(this.root, stack, dfsnum, low, old, sccs);
		return sccs;
	}

	private void sccVisit(AbstractNode v, List<AbstractNode> stack, Map<AbstractNode, Integer> dfsnum,
			Map<AbstractNode, Integer> low, Set<AbstractNode> old, List<List<AbstractNode>> sccs) {

		old.add(v);
		dfsnum.put(v, n);
		low.put(v, n);
		n++;
		stack.add(v);

		for (AbstractNode w : this.getSuccessors(v)) {
			if (!old.contains(w)) {
				sccVisit(w, stack, dfsnum, low, old, sccs);
				int low_v = low.get(v);
				int low_w = low.get(w);
				low.put(v, Math.min(low_v, low_w));
			} else {
				int dfsnum_v = dfsnum.get(v);
				int dfsnum_w = dfsnum.get(w);
				if (dfsnum_w < dfsnum_v && stack.contains(w)) {
					int low_v = low.get(v);
					low.put(v, Math.min(dfsnum_w, low_v));
				}
			}
		}

		if (low.get(v).equals(dfsnum.get(v))) {
			List<AbstractNode> scc = new LinkedList<AbstractNode>();
			AbstractNode x;
			do {
				x = stack.remove(stack.size() - 1);
				scc.add(x);
			} while (!x.equals(v));
			sccs.add(scc);
		}
	}

	public List<AbstractNode> getSuccessors(AbstractNode node) {
		List<AbstractNode> retMe = this.edges.get(node);
		if (retMe == null) {
			retMe = new LinkedList<AbstractNode>();
		}
		return retMe;
	}

	public Set<AbstractNode> getPredecessors(AbstractNode node) {
		Set<AbstractNode> retMe = new HashSet<AbstractNode>();
		for (Map.Entry<AbstractNode, List<AbstractNode>> entry : this.edges.entrySet()) {
			AbstractNode from = entry.getKey();
			List<AbstractNode> toList = entry.getValue();
			if (toList.contains(node)) {
				retMe.add(from);
			}
		}

		return retMe;
	}

	public List<AbstractNode> bfIterator() {

		LinkedList<AbstractNode> list = new LinkedList<AbstractNode>();

		LinkedList<AbstractNode> queue = new LinkedList<AbstractNode>();

		Set<AbstractNode> visited = new HashSet<AbstractNode>();

		queue.add(this.root);
		visited.add(this.root);

		Comparator<AbstractNode> comp = new NodeComparator<AbstractNode>();
		this.bfIteratorHelper(list, queue, visited, comp);

		return list;
	}

	private void bfIteratorHelper(List<AbstractNode> list, LinkedList<AbstractNode> queue, Set<AbstractNode> visited,
			Comparator<AbstractNode> comp) {

		AbstractNode node = (AbstractNode) queue.removeFirst();
		list.add(node);

		List<AbstractNode> successors = this.getSuccessors(node);
		if (!(node instanceof BuiltinFunctionNode)) {
			Collections.sort(successors, comp);
		}
		for (AbstractNode succ : successors) {
			if (!visited.contains(succ)) {
				queue.add(succ);
				visited.add(succ);
			}
		}
		if (queue.size() > 0) {
			bfIteratorHelper(list, queue, visited, comp);
		}
	}

	public boolean isRoot(AbstractNode node) {
		return node.equals(this.root);
	}

	@SuppressWarnings("rawtypes")
	public void reduceWithLeaves(Collection<? extends AbstractNode> leaves) {

		this.leavesReduced = true;

		Set<AbstractNode> reachable = new HashSet<AbstractNode>();
		Set<NormalNode> retVars = new HashSet<NormalNode>();
		for (AbstractNode leaf : leaves) {
			reduceWithLeavesHelper(leaf, reachable, retVars);
		}

		for (Iterator<?> iter = this.nodes.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractNode node = (AbstractNode) entry.getKey();
			if (!reachable.contains(node)) {
				iter.remove();
			}
		}

		for (Iterator<Map.Entry<AbstractNode, List<AbstractNode>>> iter = this.edges.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<AbstractNode, List<AbstractNode>> entry = iter.next();
			AbstractNode node = entry.getKey();
			List<AbstractNode> successors = entry.getValue();
			if (!reachable.contains(node)) {
				iter.remove();
				continue;
			}
			for (Iterator<AbstractNode> succIter = successors.iterator(); succIter.hasNext();) {
				AbstractNode succ = (AbstractNode) succIter.next();
				if (!reachable.contains(succ)) {
					succIter.remove();
				}
			}
		}

		for (NormalNode retVarNode : retVars) {
			Set<AbstractNode> tempNodes = this.getPredecessors(retVarNode);
			if (tempNodes.size() != 1) {
				throw new RuntimeException("SNH");
			}
			AbstractNode tempNode = tempNodes.iterator().next();

			if (tempNode == this.root) {
				continue;
			}

			Set<AbstractNode> preds = this.getPredecessors(tempNode);

			this.nodes.remove(tempNode);
			this.edges.remove(tempNode);
			for (Iterator<Map.Entry<AbstractNode, List<AbstractNode>>> iter = this.edges.entrySet().iterator(); iter
					.hasNext();) {
				Map.Entry<AbstractNode, List<AbstractNode>> entry = iter.next();
				List<AbstractNode> successors = entry.getValue();
				for (Iterator<AbstractNode> succIter = successors.iterator(); succIter.hasNext();) {
					AbstractNode succ = (AbstractNode) succIter.next();
					if (succ.equals(tempNode)) {
						succIter.remove();
					}
				}
			}

			for (AbstractNode pred : preds) {
				this.addEdge(pred, retVarNode);
			}
		}
	}

	private void reduceWithLeavesHelper(AbstractNode node, Set<AbstractNode> reachable, Set<NormalNode> retVars) {
		if (reachable.contains(node)) {
			return;
		}
		reachable.add(node);

		if (node instanceof NormalNode) {
			NormalNode normalNode = (NormalNode) node;
			AbstractTacPlace place = normalNode.getPlace();
			if (place.isVariable() && place.getVariable().isReturnVariable()) {
				retVars.add(normalNode);
			}
		}

		for (AbstractNode pre : this.getPredecessors(node)) {
			reduceWithLeavesHelper(pre, reachable, retVars);
		}
	}

	public Set<AbstractNode> removeUninitNodes() {

		Set<AbstractNode> retme = new HashSet<AbstractNode>();

		Set<UninitializedNode> uninitNodes = getUninitNodes();
		for (UninitializedNode uninitNode : uninitNodes) {

			Set<AbstractNode> preds = this.getPredecessors(uninitNode);
			if (preds.size() != 1) {
				throw new RuntimeException("SNH");
			}

			AbstractNode pre = preds.iterator().next();

			retme.add(pre);

			this.nodes.remove(uninitNode);
			List<AbstractNode> outEdges = this.edges.get(pre);
			if (outEdges == null) {
				this.edges.remove(pre);
			} else {
				outEdges.remove(uninitNode);
				if (outEdges.isEmpty()) {
					this.edges.remove(pre);
				}
			}
		}

		return retme;
	}

	public void removeTemporaries() {
		Set<NormalNode> temporaries = this.getTemporaries();
		for (NormalNode temp : temporaries) {

			Set<AbstractNode> preds = this.getPredecessors(temp);
			List<AbstractNode> succs = this.edges.get(temp);

			if (preds == null || succs == null || preds.size() != 1 || succs.size() != 1) {
				continue;
			}

			AbstractNode pre = preds.iterator().next();
			AbstractNode succ = succs.iterator().next();

			List<AbstractNode> outEdges = this.edges.get(pre);
			int outIndex = outEdges.indexOf(temp);
			outEdges.remove(outIndex);
			outEdges.add(outIndex, succ);

			this.nodes.remove(temp);
			this.edges.remove(temp);
		}
	}

	private Set<NormalNode> getTemporaries() {
		Set<NormalNode> retme = new HashSet<NormalNode>();
		for (AbstractNode node : this.nodes.keySet()) {
			if (node instanceof NormalNode) {
				NormalNode nn = (NormalNode) node;
				if (nn.getPlace().isVariable()) {
					if (nn.getPlace().getVariable().isTemp()) {
						retme.add(nn);
					}
				}
			}
		}
		return retme;
	}

	public int reduceToIneffectiveSanit(Map<AbstractNode, FSAAutomaton> deco,
			AbstractSanitationAnalysis sanitAnalysis) {

		List<AbstractNode> border = new LinkedList<AbstractNode>();
		Set<AbstractNode> visited = new HashSet<AbstractNode>();
		this.getCustomSanitBorder(this.root, visited, border);

		List<AbstractNode> ineffectiveBorder = new LinkedList<AbstractNode>();
		for (AbstractNode customSanit : border) {
			if (sanitAnalysis.isIneffective(customSanit, deco)) {
				ineffectiveBorder.add(customSanit);
			}
		}

		this.reduceToInnerNodes(ineffectiveBorder);
		return ineffectiveBorder.size();
	}

	private void getCustomSanitBorder(AbstractNode node, Set<AbstractNode> visited, List<AbstractNode> border) {

		if (visited.contains(node)) {
			return;
		}
		visited.add(node);

		if (AbstractSanitationAnalysis.isCustomSanit(node)) {
			border.add(node);
			return;
		}

		for (AbstractNode succ : this.getSuccessors(node)) {
			getCustomSanitBorder(succ, visited, border);
		}

	}

	@SuppressWarnings("rawtypes")
	public void reduceToInnerNodes(Collection<? extends AbstractNode> nodes) {

		Set<AbstractNode> reachable = new HashSet<AbstractNode>();
		for (AbstractNode sanitNode : nodes) {
			reduceToInnerHelper(sanitNode, reachable);
		}

		for (Iterator<?> iter = this.nodes.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractNode node = (AbstractNode) entry.getKey();
			if (!reachable.contains(node)) {
				iter.remove();
			}
		}
		for (Iterator<Map.Entry<AbstractNode, List<AbstractNode>>> iter = this.edges.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<AbstractNode, List<AbstractNode>> entry = iter.next();
			AbstractNode node = entry.getKey();
			List<AbstractNode> successors = entry.getValue();
			if (!reachable.contains(node)) {
				iter.remove();
				continue;
			}
			for (Iterator<AbstractNode> succIter = successors.iterator(); succIter.hasNext();) {
				AbstractNode succ = (AbstractNode) succIter.next();
				if (!reachable.contains(succ)) {
					succIter.remove();
				}
			}
		}
	}

	private void reduceToInnerHelper(AbstractNode node, Set<AbstractNode> reachable) {

		for (AbstractNode pre : this.getPredecessors(node)) {
			reduceToInnerHelperUp(pre, reachable);
		}

		reduceToSanitInnerDown(node, reachable);

	}

	private void reduceToInnerHelperUp(AbstractNode node, Set<AbstractNode> reachable) {

		if (reachable.contains(node)) {
			return;
		}

		reachable.add(node);

		for (AbstractNode pre : this.getPredecessors(node)) {
			reduceToInnerHelperUp(pre, reachable);
		}

	}

	private void reduceToSanitInnerDown(AbstractNode node, Set<AbstractNode> reachable) {

		if (reachable.contains(node)) {
			return;
		}

		reachable.add(node);

		for (AbstractNode succ : this.getSuccessors(node)) {
			reduceToSanitInnerDown(succ, reachable);
		}
	}

	public int countPaths() {
		return (new DependencyGraph(this).countPathsDestructive());
	}

	private int countPathsDestructive() {
		this.eliminateCycles();
		Map<AbstractNode, Integer> node2p = new HashMap<AbstractNode, Integer>();
		pathCounterHelper(this.root, node2p, new HashSet<AbstractNode>());
		return node2p.get(root);
	}

	private void pathCounterHelper(AbstractNode node, Map<AbstractNode, Integer> node2p, Set<AbstractNode> visited) {

		visited.add(node);

		List<AbstractNode> successors = this.getSuccessors(node);
		if (successors != null && !successors.isEmpty()) {
			for (AbstractNode succ : successors) {
				if (!visited.contains(succ) && node2p.get(succ) == null) {
					pathCounterHelper(succ, node2p, visited);
				}
			}
			int p = 0;
			for (AbstractNode succ : successors) {
				p += node2p.get(succ);
			}
			node2p.put(node, p);
		} else {
			node2p.put(node, 1);
		}

	}

	@SuppressWarnings("serial")
	private class NotReachableException extends Exception {

	}

	private class NodeComparator<T> implements Comparator<T> {

		public int compare(T o1, T o2) {
			if (!(o1 instanceof AbstractNode) || !(o2 instanceof AbstractNode)) {
				throw new RuntimeException("SNH");
			}
			AbstractNode n1 = (AbstractNode) o1;
			AbstractNode n2 = (AbstractNode) o2;
			return n1.comparableName().compareTo(n2.dotName());
		}
	}

	private class ContextSwitch {
		TacFunction targetFunction;
		Set<AbstractContext> targetContexts;
	}

}

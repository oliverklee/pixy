package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.*;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.CompositeTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.DependencyGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignArray;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignReference;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallUnknown;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Define;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.FunctionEntry;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Isset;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Tester;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Global;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

public class DependencyAnalysis extends AbstractInterproceduralAnalysis {

	private TacConverter tac;
	private List<AbstractTacPlace> places;
	private ConstantsTable constantsTable;
	private SymbolTable superSymbolTable;
	private Variable memberPlace;
	private GenericRepository<AbstractLatticeElement> repos;

	private AliasAnalysis aliasAnalysis;
	private LiteralAnalysis literalAnalysis;
	private GlobalsModificationAnalysis modAnalysis;

	private boolean finishedDetection;

	public DependencyAnalysis(TacConverter tac, AliasAnalysis aliasAnalysis, LiteralAnalysis literalAnalysis,
			AbstractAnalysisType analysisType, InterproceduralWorklist workList,
			GlobalsModificationAnalysis modAnalysis) {

		this.tac = tac;
		this.places = tac.getPlacesList();
		this.constantsTable = tac.getConstantsTable();
		this.superSymbolTable = tac.getSuperSymbolTable();
		this.memberPlace = tac.getMemberPlace();
		this.repos = new GenericRepository<AbstractLatticeElement>();

		this.aliasAnalysis = aliasAnalysis;
		this.literalAnalysis = literalAnalysis;
		this.modAnalysis = modAnalysis;

		this.finishedDetection = false;

		this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(), analysisType, workList);

	}

	protected void initLattice() {

		this.lattice = new DependencyLattice(this.places, this.constantsTable, this.functions, this.superSymbolTable,
				this.memberPlace);

		this.startValue = new DependencyLatticeElement();

		this.initialValue = this.lattice.getBottom();

	}

	public DependencyLatticeElement applyInsideBasicBlock(BasicBlock basicBlock, AbstractCfgNode untilHere,
			DependencyLatticeElement invalue) {

		DependencyLatticeElement outValue = new DependencyLatticeElement((DependencyLatticeElement) invalue);
		List<AbstractCfgNode> containedNodes = basicBlock.getContainedNodes();
		CompositeTransferFunction ctf = (CompositeTransferFunction) this.getTransferFunction(basicBlock);

		Iterator<AbstractCfgNode> nodesIter = containedNodes.iterator();
		Iterator<AbstractTransferFunction> tfIter = ctf.iterator();

		while (nodesIter.hasNext() && tfIter.hasNext()) {
			AbstractCfgNode node = (AbstractCfgNode) nodesIter.next();
			AbstractTransferFunction tf = (AbstractTransferFunction) tfIter.next();
			if (node == untilHere) {
				break;
			}
			outValue = (DependencyLatticeElement) tf.transfer(outValue);
		}

		return outValue;
	}

	public DependencyLatticeElement applyInsideDefaultCfg(AbstractCfgNode defaultNode, AbstractCfgNode untilHere,
			DependencyLatticeElement invalue) {

		DependencyLatticeElement out = new DependencyLatticeElement((DependencyLatticeElement) invalue);

		while (defaultNode != untilHere) {
			AbstractTransferFunction tf = this.getTransferFunction(defaultNode);
			out = (DependencyLatticeElement) tf.transfer(out);
			defaultNode = defaultNode.getSuccessor(0);
		}

		return out;

	}

	protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();
		Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
		Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

		return new AssignSimple(left, cfgNode.getRight(), mustAliases, mayAliases, cfgNode);
	}

	protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();
		Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
		Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

		return new AssignUnary(left, cfgNode.getRight(), cfgNode.getOperator(), mustAliases, mayAliases, cfgNode);
	}

	protected AbstractTransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();
		Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
		Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

		return new AssignBinary(left, cfgNode.getLeftOperand(), cfgNode.getRightOperand(), cfgNode.getOperator(),
				mustAliases, mayAliases, cfgNode);
	}

	protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference) cfgNodeX;
		return new AssignReference(cfgNode.getLeft(), cfgNode.getRight(), cfgNode);
	}

	protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
		return new Unset(cfgNode.getOperand(), cfgNode);
	}

	protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray) cfgNodeX;
		return new AssignArray(cfgNode.getLeft(), cfgNode);
	}

	protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation) cfgNodeX;
		TacFunction calledFunction = cfgNode.getCallee();
		TacFunction callingFunction = traversedFunction;

		if (calledFunction == null) {

			System.out.println(cfgNodeX.getFileName() + ", " + cfgNodeX.getOriginalLineNumber());
			System.out.println(cfgNode.getFunctionNamePlace());
			throw new RuntimeException("SNH");

		}
		List<TacActualParameter> actualParams = cfgNode.getParamList();
		List<TacFormalParameter> formalParams = calledFunction.getParams();

		AbstractTransferFunction tf = null;

		if (actualParams.size() > formalParams.size()) {
			throw new RuntimeException("More actual than formal params for function "
					+ cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOriginalLineNumber());

		} else {
			tf = new CallPreparation(actualParams, formalParams, callingFunction, calledFunction, this, cfgNode);
		}

		return tf;

	}

	protected AbstractTransferFunction entry(TacFunction traversedFunction) {
		return new FunctionEntry(traversedFunction);
	}

	protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		CallReturn cfgNodeRet = (CallReturn) cfgNodeX;
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNodePrep = cfgNodeRet.getCallPrepNode();
		TacFunction callingFunction = traversedFunction;
		TacFunction calledFunction = cfgNodePrep.getCallee();

		AbstractTransferFunction tf;
		if (calledFunction == null) {

			throw new RuntimeException("SNH");

		} else {

			Set<AbstractTacPlace> modSet = null;
			if (this.modAnalysis != null) {
				modSet = this.modAnalysis.getMod(calledFunction);
			}

			tf = new at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallReturn(
					this.interproceduralAnalysisInformation.getAnalysisNode(cfgNodePrep), callingFunction,
					calledFunction, cfgNodePrep, cfgNodeRet, this.aliasAnalysis, modSet);
		}

		return tf;
	}

	protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
		return new at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction.CallBuiltinFunction(cfgNode);
	}

	protected AbstractTransferFunction callUnknown(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;
		return new CallUnknown(cfgNode);
	}

	protected AbstractTransferFunction global(AbstractCfgNode cfgNodeX) {

		Global cfgNode = (Global) cfgNodeX;

		Variable globalOp = cfgNode.getOperand();

		TacFunction mainFunc = this.mainFunction;
		SymbolTable mainSymTab = mainFunc.getSymbolTable();
		Variable realGlobal = mainSymTab.getVariable(globalOp.getName());

		if (realGlobal == null) {
			return TransferFunctionId.INSTANCE;

		} else {
			return new AssignReference(globalOp, realGlobal, cfgNode);
		}
	}

	protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
		return new Isset(cfgNode.getLeft(), cfgNode.getRight(), cfgNode);
	}

	protected AbstractTransferFunction define(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define) cfgNodeX;
		return new Define(this.constantsTable, this.literalAnalysis, cfgNode);
	}

	protected AbstractTransferFunction tester(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester) cfgNodeX;
		return new Tester(cfgNode);
	}

	protected AbstractTransferFunction echo(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected Boolean evalIf(If ifNode, AbstractLatticeElement inValue) {
		return this.literalAnalysis.evalIf(ifNode);
	}

	public List<DependencyGraph> getDepGraph(Sink sink) {

		List<DependencyGraph> retMe = new LinkedList<DependencyGraph>();

		TacFunction mainFunc = this.mainFunction;
		SymbolTable mainSymTab = mainFunc.getSymbolTable();

		List<SinkProblem> problems;
		problems = sink.getSinkProblems();

		if (problems.isEmpty()) {
			return retMe;
		}

		for (SinkProblem problem : problems) {

			DependencyGraph depGraph = DependencyGraph.create(problem.getPlace(), sink.getNode(),
					this.interproceduralAnalysisInformation, mainSymTab, this);

			if (depGraph == null) {
				continue;
			}

			retMe.add(depGraph);
		}

		return retMe;
	}

	public TacConverter getTac() {
		return this.tac;
	}

	@SuppressWarnings("unused")
	public List<DependencyGraph> getDepGraphs(List<Sink> sinks) {

		List<DependencyGraph> retMe = new LinkedList<DependencyGraph>();

		Collections.sort(sinks);

		boolean statistics = false;

		int treeCount = 0;
		int dagCount = 0;
		int cycleCount = 0;

		int stringLeafs = 0;
		int nonStringLeafs = 0;

		TacFunction mainFunc = this.mainFunction;
		SymbolTable mainSymTab = mainFunc.getSymbolTable();

		Map<String, Integer> opMap = new HashMap<String, Integer>();

		int i = 0;
		for (Sink sink : sinks) {
			i++;

			List<SinkProblem> problems;
			problems = sink.getSinkProblems();

			if (problems.isEmpty()) {
				continue;
			}

			for (SinkProblem problem : problems) {

				DependencyGraph depGraph = DependencyGraph.create(problem.getPlace(), sink.getNode(),
						this.interproceduralAnalysisInformation, mainSymTab, this);

				if (depGraph == null) {
					continue;
				}

				retMe.add(depGraph);

				if (statistics) {

					System.out.println("Dep Graph Shape Info");
					System.out.println("--------------------");
					if (depGraph.isTree()) {
						System.out.println("is a tree");
						treeCount++;
						if (depGraph.leafsAreStrings()) {
							System.out.println("leafs are strings");
							stringLeafs++;
						} else {
							System.out.println("leafs are not strings");
							nonStringLeafs++;
						}
					} else if (depGraph.hasCycles()) {
						System.out.println("has cycles");
						cycleCount++;
					} else {
						System.out.println("is a dag");
						dagCount++;
						if (depGraph.leafsAreStrings()) {
							System.out.println("leafs are strings");
							stringLeafs++;
						} else {
							System.out.println("leafs are not strings");
							nonStringLeafs++;
						}
					}
					System.out.println();

					System.out.println("Dep Graph Operation Counters");
					System.out.println("------------------------------");
					Map<String, Integer> singleOpMap = depGraph.getOpMap();
					for (Map.Entry<String, Integer> entry : singleOpMap.entrySet()) {
						String opName = entry.getKey();
						Integer opCount = entry.getValue();
						System.out.println(opName + ": " + opCount);
						Integer totalCount = opMap.get(opName);
						if (totalCount == null) {
							totalCount = new Integer(opCount);
						} else {
							totalCount = new Integer(totalCount.intValue() + opCount);
						}
						opMap.put(opName, totalCount);
					}
					System.out.println();

				}

			}
		}

		this.finishedDetection = true;
		int a = 1;
		if (a == 2) {
			System.out.println("cleaning dependency analysis...");
			System.out.println();
			this.clean();
		} else {
			System.out.println("skipping clean-up");
			System.out.println();
		}

		if (statistics) {

			System.out.println("Total Shape Statistics");
			System.out.println("------------------------");
			System.out.println("trees:        " + treeCount);
			System.out.println("dags:         " + dagCount);
			System.out.println("cycle graphs: " + cycleCount);

			System.out.println("with string leafs:     " + stringLeafs);
			System.out.println("with non-string leafs: " + nonStringLeafs);

			System.out.println();
			System.out.println("Total Op Statistics");
			System.out.println("---------------------");
			for (Map.Entry<String, Integer> entry : opMap.entrySet()) {
				String opName = entry.getKey();
				Integer opCount = entry.getValue();
				System.out.println(opName + ": " + opCount);
			}
			System.out.println();

		}

		return retMe;

	}

	public void detectVulns() {

		if (true)
			throw new RuntimeException("dummy method");
	}

	public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
		return this.repos.recycle(recycleMe);
	}

	public TacFunction getMainFunction() {
		return this.tac.getMainFunction();
	}

	public void clean() {
		if (this.finishedDetection) {
			this.interproceduralAnalysisInformation.foldRecycledAndClean(this);
		} else {
			throw new RuntimeException("Refusing to clean extaint analysis: no detection yet");
		}
	}

	public void checkReachability() {

		if (!(this.analysisType instanceof CallStringAnalysis)) {
			System.out.println("Warning: Can't check for unreachable code");
			return;
		}

		ConnectorComputation cc = ((CallStringAnalysis) this.analysisType).getConnectorComputation();
		CallGraph callGraph = cc.getCallGraph();
		for (TacFunction f : callGraph.getFunctions()) {
			AbstractCfgNode head = f.getCfg().getHead();
			int numContexts = cc.getNumContexts(f);
			LinkedList<AbstractCfgNode> stack = new LinkedList<AbstractCfgNode>();
			Set<AbstractCfgNode> visited = new HashSet<AbstractCfgNode>();

			AbstractCfgNode current = head;
			visited.add(current);
			if (!this.isReachable(head, numContexts)) {
				this.warnUnreachable(current);
			} else {
				stack.add(current);
			}
			while (!stack.isEmpty()) {

				current = stack.getLast();

				AbstractCfgNode next = null;
				for (int i = 0; (i < 2) && (next == null); i++) {
					CfgEdge outEdge = current.getOutEdge(i);
					if (outEdge != null) {
						next = outEdge.getDest();
						if (visited.contains(next)) {
							next = null;
						} else {
						}
					}
				}

				if (next == null) {
					stack.removeLast();
				} else {
					visited.add(next);

					if (!this.isReachable(next, numContexts)) {
						this.warnUnreachable(next);
					} else {
						stack.add(next);
					}
				}
			}
		}
	}

	private boolean isReachable(AbstractCfgNode cfgNode, int numContexts) {
		Map<AbstractContext, AbstractLatticeElement> phi = this.interproceduralAnalysisInformation
				.getAnalysisNode(cfgNode).getPhi();
		if (phi.size() == 0) {
			return false;
		}

		for (AbstractLatticeElement elem : phi.values()) {
			if (elem == null) {
				throw new RuntimeException("SNH");
			}
		}
		return true;
	}

	private void warnUnreachable(AbstractCfgNode cfgNode) {
		System.out.println("Warning: Unreachable code");
		System.out.println("- " + cfgNode.getLoc());
		if (cfgNode instanceof CallReturn) {
			CallReturn callRet = (CallReturn) cfgNode;
			System.out.println("- return from: " + callRet.getCallNode().getFunctionNamePlace());
		} else {
			System.out.println("- " + cfgNode);
		}
	}

}

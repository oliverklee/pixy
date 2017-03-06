package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.type.Type;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeAnalysis;
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
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Goto;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Hotspot;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Static;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Throw;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.phpParser.PhpSymbols;

import java.io.*;

public class TacConverter {

	private File file;
	private ParseTree phpParseTree;
	private int tempId = 0;
	private int maxTempId = 0;
	private int id;
	private LinkedList<AbstractCfgNode> breakTargetStack;
	private LinkedList<AbstractCfgNode> continueTargetStack;
	private LinkedList<TacFunction> functionStack;
	private LinkedList<TacClass> classStack;
	private LinkedList<TacInterface> interfaceStack;
	private LinkedList<TacNamespace> nameSpaceStack;
	private SymbolTable specialSymbolTable;
	private SymbolTable superSymbolTable;
	private SymbolTable mainSymbolTable;
	private ConstantsTable constantsTable;
	private final AbstractTacPlace lineCPlace;
	private final AbstractTacPlace functionCPlace;
	private final AbstractTacPlace methodCPlace;
	private final AbstractTacPlace namespaceCPlace;
	private final AbstractTacPlace classCPlace;
	private final AbstractTacPlace voidPlace;
	private final AbstractTacPlace emptyOffsetPlace;
	private final Variable objectPlace;
	private final Variable memberPlace;
	private Map<String, TacFunction> userFunctions;
	private Map<String, Map<String, TacFunction>> userMethods;
	private Map<String, TacClass> userClasses;
	private Map<String, TacNamespace> userNameSpaces;
	private Map<String, TacInterface> userInterfaces;
	private TacFunction mainFunction;
	private Map<TacFunction, List<CallPreparation>> functionCalls;
	private Map<TacFunction, List<CallPreparation>> methodCalls;
	private boolean specialNodes;
	private Map<Integer, Hotspot> hotspots;
	private List<Include> includeNodes;

	public TacConverter(ParseTree phpParseTree, boolean specialNodes, int id, File file, ProgramConverter pcv) {

		this.id = id;
		this.file = file;

		this.phpParseTree = phpParseTree;
		this.breakTargetStack = new LinkedList<AbstractCfgNode>();
		this.continueTargetStack = new LinkedList<AbstractCfgNode>();
		this.functionStack = new LinkedList<TacFunction>();
		this.classStack = new LinkedList<TacClass>();
		this.interfaceStack = new LinkedList<TacInterface>();
		this.nameSpaceStack = new LinkedList<TacNamespace>();
		this.functionCalls = new HashMap<TacFunction, List<CallPreparation>>();
		this.methodCalls = new HashMap<TacFunction, List<CallPreparation>>();
		this.voidPlace = new Literal("_void");
		this.specialSymbolTable = new SymbolTable("_special");
		this.emptyOffsetPlace = new Variable("_emptyOffset", this.specialSymbolTable);
		this.specialSymbolTable.add((Variable) this.emptyOffsetPlace);
		this.objectPlace = new Variable("_object", this.specialSymbolTable);
		this.specialSymbolTable.add((Variable) this.objectPlace);
		this.memberPlace = new Variable(InternalStrings.memberName, this.specialSymbolTable);
		this.memberPlace.setIsMember(true);
		this.specialSymbolTable.add(this.memberPlace);
		this.mainSymbolTable = null;
		this.userFunctions = new HashMap<String, TacFunction>();
		this.mainFunction = null;
		this.superSymbolTable = pcv.getSuperSymbolTable();
		this.addSuperGlobal("$_TAINTED");
		this.addSuperGlobal("$_UNTAINTED");
		this.constantsTable = new ConstantsTable();

		Constant lineConstant = Constant.getInstance("__LINE__");
		Constant functionConstant = Constant.getInstance("__FUNCTION__");
		Constant classConstant = Constant.getInstance("__CLASS__");
		Constant namespaceConstant = Constant.getInstance("__NAMESPACE__");
		Constant methodConstant = Constant.getInstance("__METHOD__");
		Constant dirConstant = Constant.getInstance("__DIR__");
		this.constantsTable.add(Constant.TRUE);
		this.constantsTable.add(Constant.FALSE);
		this.constantsTable.add(Constant.NULL);
		this.constantsTable.add(lineConstant);
		this.constantsTable.add(functionConstant);
		this.constantsTable.add(classConstant);

		this.constantsTable.add(namespaceConstant);
		this.constantsTable.add(methodConstant);
		this.constantsTable.add(dirConstant);
		this.lineCPlace = lineConstant;
		this.functionCPlace = functionConstant;
		this.classCPlace = classConstant;
		this.methodCPlace = methodConstant;
		this.namespaceCPlace = namespaceConstant;
		this.specialNodes = specialNodes;
		this.hotspots = new HashMap<Integer, Hotspot>();
		this.includeNodes = new LinkedList<Include>();
		this.userClasses = new HashMap<String, TacClass>();
		this.userNameSpaces = new HashMap<String, TacNamespace>();
		this.userInterfaces = new HashMap<String, TacInterface>();
		this.userMethods = new HashMap<String, Map<String, TacFunction>>();

	}

	public void assignFunctions() {

		for (TacFunction function : this.userFunctions.values()) {
			if (function == null) {
				throw new RuntimeException("SNH");
			}

			ControlFlowGraph ControlFlowGraph = function.getCfg();
			this.assignFunctionsHelper(ControlFlowGraph, function);

			for (TacFormalParameter param : function.getParams()) {
				if (param.hasDefault()) {
					ControlFlowGraph defaultCfg = param.getDefaultCfg();
					this.assignFunctionsHelper(defaultCfg, function);
				}
			}
		}

		for (TacFunction function : this.getMethods()) {

			if (function == null) {
				throw new RuntimeException("SNH");
			}

			ControlFlowGraph ControlFlowGraph = function.getCfg();
			this.assignFunctionsHelper(ControlFlowGraph, function);

			for (TacFormalParameter param : function.getParams()) {
				if (param.hasDefault()) {
					ControlFlowGraph defaultCfg = param.getDefaultCfg();
					this.assignFunctionsHelper(defaultCfg, function);
				}
			}
		}
	}

	private void assignFunctionsHelper(ControlFlowGraph ControlFlowGraph, TacFunction function) {

		for (Iterator<AbstractCfgNode> iter = ControlFlowGraph.dfPreOrder().iterator(); iter.hasNext();) {

			AbstractCfgNode node = (AbstractCfgNode) iter.next();
			node.setEnclosingFunction(function);

			if (node instanceof BasicBlock) {
				BasicBlock bb = (BasicBlock) node;
				for (AbstractCfgNode contained : bb.getContainedNodes()) {
					contained.setEnclosingFunction(function);
				}
			}
		}
	}

	public void createBasicBlocks() {

		Set<AbstractCfgNode> visited = new HashSet<AbstractCfgNode>();

		for (Iterator<TacFunction> iter = this.userFunctions.values().iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();
			ControlFlowGraph ControlFlowGraph = function.getCfg();
			AbstractCfgNode head = ControlFlowGraph.getHead();
			visited.add(head);
			this.createBasicBlocksHelper(head.getSuccessor(0), visited);
		}
		for (TacFunction function : this.getMethods()) {
			ControlFlowGraph ControlFlowGraph = function.getCfg();
			AbstractCfgNode head = ControlFlowGraph.getHead();
			visited.add(head);
			this.createBasicBlocksHelper(head.getSuccessor(0), visited);
		}
	}

	private void createBasicBlocksHelper(AbstractCfgNode cfgNode, Set<AbstractCfgNode> visited) {

		if (visited.contains(cfgNode)) {
			return;
		}
		visited.add(cfgNode);

		if (this.allowedInBasicBlock(cfgNode)) {

			int contained = 1;

			List<CfgEdge> inEdges = cfgNode.getInEdges();

			AbstractCfgNode startNode = cfgNode;

			BasicBlock basicBlock = new BasicBlock(startNode);

			AbstractCfgNode succ = startNode.getSuccessor(0);
			AbstractCfgNode beforeSucc = startNode;
			while (succ != null && this.allowedInBasicBlock(succ) && !visited.contains(succ)) {
				if (succ.getPredecessors().size() > 1) {

					break;
				}
				visited.add(succ);
				basicBlock.addNode(succ);
				contained++;
				beforeSucc = succ;
				succ = succ.getSuccessor(0);
			}

			if (contained > 1) {

				startNode.clearInEdges();
				basicBlock.informEnclosedNodes();

				for (Iterator<CfgEdge> iter = inEdges.iterator(); iter.hasNext();) {
					CfgEdge inEdge = (CfgEdge) iter.next();
					inEdge.setDest(basicBlock);
					basicBlock.addInEdge(inEdge);
				}
				beforeSucc.clearOutEdges();
				if (succ != null) {
					succ.removeInEdge(beforeSucc);
					CfgEdge blockToSucc = new CfgEdge(basicBlock, succ, CfgEdge.NORMAL_EDGE);
					basicBlock.setOutEdge(0, blockToSucc);
					succ.addInEdge(blockToSucc);
				} else {
				}
			}

			if (succ != null) {
				this.createBasicBlocksHelper(succ, visited);
			}

		} else {
			if (cfgNode != null) {
				List<AbstractCfgNode> successors = cfgNode.getSuccessors();
				for (Iterator<AbstractCfgNode> iter = successors.iterator(); iter.hasNext();) {
					AbstractCfgNode successor = (AbstractCfgNode) iter.next();
					this.createBasicBlocksHelper(successor, visited);
				}
			}
		}
	}

	private boolean allowedInBasicBlock(AbstractCfgNode cfgNode) {

		if (cfgNode instanceof CallBuiltinFunction) {

			CallBuiltinFunction cfgNodeBuiltin = (CallBuiltinFunction) cfgNode;
			return !MyOptions.isSink(cfgNodeBuiltin.getFunctionName());
		} else if (cfgNode instanceof AssignSimple || cfgNode instanceof AssignUnary || cfgNode instanceof AssignBinary
				|| cfgNode instanceof Define || cfgNode instanceof EmptyTest || cfgNode instanceof Isset
				|| cfgNode instanceof Static) {
			return true;
		}
		return false;
	}

	public void include(TacConverter includedTac, Include includeNode, TacFunction includingFunction) {

		Map<String, TacFunction> includedUserFunctions = includedTac.getUserFunctions();
		TacFunction includedMainFunc = includedUserFunctions.get(InternalStrings.mainFunctionName);
		this.inlineMainCfg(includedMainFunc, includeNode);
		this.addFunctionCalls(this.mainFunction, includedTac.getFunctionCalls(includedMainFunc));
		this.addMethodCalls(this.mainFunction, includedTac.getMethodCalls(includedMainFunc));
		SymbolTable includedMainSymTab = includedMainFunc.getSymbolTable();
		SymbolTable includingSymTab = includingFunction.getSymbolTable();
		includingSymTab.addAll(includedMainSymTab);
		includedMainSymTab = null;
		for (TacFunction includedFunc : includedUserFunctions.values()) {
			if (includedFunc.isMain()) {
				continue;
			}
			String includedFuncName = includedFunc.getName();
			TacFunction existingFunction = this.userFunctions.get(includedFuncName);
			if (existingFunction != null) {
				if (!existingFunction.getFileName().equals(includedFunc.getFileName())) {
					System.out.println("\nWarning: Duplicate function definition due to include: " + includedFuncName);

					System.out.println("- tried: " + includedFunc.getLoc());
					System.out.println("- using: " + existingFunction.getLoc());
				}
				continue;
			}

			this.userFunctions.put(includedFuncName, includedFunc);

			this.addFunctionCalls(includedFunc, includedTac.getFunctionCalls(includedFunc));
			this.addMethodCalls(includedFunc, includedTac.getMethodCalls(includedFunc));
		}
		Map<String, Map<String, TacFunction>> includedUserMethods = includedTac.getUserMethods();

		for (Map.Entry<String, Map<String, TacFunction>> entry1 : includedUserMethods.entrySet()) {

			String includedMethodName = entry1.getKey();
			Map<String, TacFunction> class2Method = entry1.getValue();

			for (Map.Entry<String, TacFunction> entry2 : class2Method.entrySet()) {

				String className = entry2.getKey();
				TacFunction includedMethod = entry2.getValue();

				TacFunction existingMethod = this.addMethod(includedMethodName, className, includedMethod);

				if (existingMethod != null) {

					if (!existingMethod.getFileName().equals(includedMethod.getFileName())) {
						System.out.println(
								"\nWarning: Duplicate method definition due to include: " + includedMethodName);
						System.out.println("- found: " + includedMethod.getLoc());
						System.out.println("- using: " + existingMethod.getLoc());
					}

					continue;
				}

				this.addFunctionCalls(includedMethod, includedTac.getFunctionCalls(includedMethod));
				this.addMethodCalls(includedMethod, includedTac.getMethodCalls(includedMethod));

			}
		}

		for (Map.Entry<String, TacClass> entry : includedTac.userClasses.entrySet()) {
			String includedClassName = entry.getKey();
			TacClass includedClass = entry.getValue();
			TacClass existingClass = this.userClasses.get(includedClassName);
			if (existingClass == null) {
				this.userClasses.put(includedClassName, includedClass);
			} else {
				if (!existingClass.getFileName().equals(includedClass.getFileName())) {
					System.out.println("\nWarning: Duplicate class definition due to include: " + includedClassName);
					System.out.println("- found: " + includedClass.getLoc());
					System.out.println("- using: " + existingClass.getLoc());
				}
			}
		}

		this.constantsTable.addAll(includedTac.getConstantsTable());

		List<Include> includedIncludeNodes = includedTac.getIncludeNodes();
		for (Iterator<Include> iter = includedIncludeNodes.iterator(); iter.hasNext();) {
			Include includedIncludeNode = (Include) iter.next();
			if (includedIncludeNode.getIncludeFunction().isMain()) {
				includedIncludeNode.setIncludeFunction(includingFunction);
			}
		}

		if (this.specialNodes) {
			this.hotspots.putAll(includedTac.hotspots);
		}
	}

	private void inlineMainCfg(TacFunction includedMainFunc, Include includeNode) {

		ControlFlowGraph includedMainCfg = includedMainFunc.getCfg();

		CfgEntry includedEntry = (CfgEntry) includedMainCfg.getHead();
		CfgExit includedExit = (CfgExit) includedMainCfg.getTail();

		AbstractCfgNode afterEntry = includedEntry.getSuccessor(0);

		if (afterEntry instanceof CfgExit) {
			this.removeCfgNode(includeNode);
		} else {

			IncludeStart includeStart = new IncludeStart(includeNode.getFile(), includeNode.getParseNode());
			IncludeEnd includeEnd = new IncludeEnd(includeStart);

			List<CfgEdge> beforeExitList = includedExit.getInEdges();

			List<CfgEdge> includeInEdges = includeNode.getInEdges();
			CfgEdge[] includeOutEdges = includeNode.getOutEdges();

			AbstractCfgNode afterInclude;
			try {
				afterInclude = includeOutEdges[0].getDest();
			} catch (NullPointerException e) {
				System.out.println(includeNode.getLoc());
				throw e;
			}

			afterInclude.removeInEdge(includeNode);
			afterEntry.removeInEdge(includedEntry);

			for (Iterator<CfgEdge> iterator = includeInEdges.iterator(); iterator.hasNext();) {
				CfgEdge inEdge = (CfgEdge) iterator.next();
				inEdge.setDest(includeStart);
				includeStart.addInEdge(inEdge);
			}

			connect(includeStart, afterEntry);

			for (Iterator<CfgEdge> iterator = beforeExitList.iterator(); iterator.hasNext();) {
				CfgEdge inEdge = (CfgEdge) iterator.next();
				inEdge.setDest(includeEnd);
				includeEnd.addInEdge(inEdge);
			}

			connect(includeEnd, afterInclude);
		}

	}

	private void resetId(int logId) {
		if (this.tempId > this.maxTempId) {
			this.maxTempId = tempId;
		}
		this.tempId = logId;
	}

	public void convert() {
		this.start(this.phpParseTree.getRoot());
		if (this.tempId > this.maxTempId) {
			this.maxTempId = tempId;
		}
	}

	public void assignReversePostOrder() {
		this.mainFunction.assignReversePostOrder();
	}

	private Variable newTemp(TacFunction function) {
		String varName = "_t" + this.tempId++ + "_" + this.id;
		SymbolTable symbolTable = function.getSymbolTable();

		Variable variable = symbolTable.getVariable(varName);

		if (variable == null) {
			variable = new Variable(varName, symbolTable, true);
			symbolTable.add(variable);
		}

		return variable;
	}

	private Variable newTemp() {

		if (this.functionStack != null) {
			return this.newTemp((TacFunction) this.functionStack.getLast());
		} else
			return null;
	}

	public List<TacFunction> getAllFunctions() {
		List<TacFunction> retMe = new LinkedList<TacFunction>();
		retMe.addAll(this.userFunctions.values());
		retMe.addAll(this.getMethods());
		return retMe;
	}

	private Collection<TacFunction> getMethods() {
		List<TacFunction> retMe = new LinkedList<TacFunction>();
		for (Map<String, TacFunction> class2Method : this.userMethods.values()) {
			retMe.addAll(class2Method.values());
		}
		return retMe;
	}

	public int getSize() {
		int size = 0;
		for (Iterator<TacFunction> iter = this.userFunctions.values().iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();
			size += function.getCfg().size();
		}
		for (TacFunction function : this.getMethods()) {
			size += function.getCfg().size();
		}
		return size;
	}

	public File getFile() {
		return this.file;
	}

	public Map<String, TacFunction> getUserFunctions() {
		return this.userFunctions;
	}

	public Map<String, Map<String, TacFunction>> getUserMethods() {
		return this.userMethods;
	}

	public boolean hasEmptyMain() {
		if (this.mainFunction.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public SymbolTable getSuperSymbolTable() {
		return this.superSymbolTable;
	}

	SymbolTable getSpecialSymbolTable() {
		return this.specialSymbolTable;
	}

	public ConstantsTable getConstantsTable() {
		return this.constantsTable;
	}

	public int getMaxTempId() {
		return this.maxTempId;
	}

	public List<AbstractTacPlace> getPlacesList() {
		List<AbstractTacPlace> placesList = new LinkedList<AbstractTacPlace>();
		placesList.addAll(this.constantsTable.getConstants().values());
		placesList.addAll(this.getVariablesList());
		placesList.addAll(this.superSymbolTable.getVariables().values());
		placesList.addAll(this.specialSymbolTable.getVariables().values());
		return placesList;
	}

	public int getNumberOfVariables() {

		int varNum = 0;
		List<Variable> varList = this.getVariablesList();
		for (Iterator<Variable> iter = varList.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isTemp()) {
				continue;
			}
			varNum++;
		}

		return varNum;
	}

	public void stats() {

		int globalVarsReal = 0;
		int globalVarsTemp = 0;
		for (Variable globalVar : this.mainSymbolTable.getVariablesColl()) {
			if (globalVar.isTemp()) {
				globalVarsTemp++;
			} else {
				globalVarsReal++;
			}
		}

		if (MyOptions.optionV) {
			System.out.println("global variables (real): " + globalVarsReal);
			System.out.println("global variables (temp): " + globalVarsTemp);
		}

		int tempsTotal = globalVarsTemp;
		int gShadowsTotal = 0;
		int fShadowsTotal = 0;
		int normalLocalsTotal = 0;

		for (TacFunction userFunction : this.userFunctions.values()) {
			if (userFunction.isMain()) {
				continue;
			}
			Collection<Variable> vars = userFunction.getSymbolTable().getVariablesColl();

			int temps = 0;
			for (Variable var : vars) {
				if (var.isTemp()) {
					temps++;
				}
			}
			int gShadows = userFunction.getSymbolTable().getGlobals2GShadows().size();
			int fShadows = userFunction.getSymbolTable().getFormals2FShadows().size();
			int normalLocals = vars.size() - temps - gShadows - fShadows;

			tempsTotal += temps;
			gShadowsTotal += gShadows;
			fShadowsTotal += fShadows;
			normalLocalsTotal += normalLocals;
			int a = 1;
			if (a == 2) {
				System.out.println("_______________");
				System.out.println(userFunction.getName() + ": " + vars.size() + " variables");
				System.out.println("Normal Locals: " + normalLocals);
				System.out.println("Temps: " + temps);
				System.out.println("G-Shadows: " + gShadows);
				System.out.println("F-Shadows: " + fShadows);
			}
		}

		if (MyOptions.optionV) {
			System.out.println();
			System.out.println("Functions:     " + (this.userFunctions.size() - 1));
			System.out.println("Normal Locals: " + normalLocalsTotal);
			System.out.println("Temps:         " + tempsTotal);
			System.out.println("G-Shadows:     " + gShadowsTotal);
			System.out.println("F-Shadows:     " + fShadowsTotal);
			System.out.println();
			System.out.println("Constants Table size: " + this.constantsTable.size());
			System.out.println("SuperSymTab size:     " + this.superSymbolTable.size());
			System.out.println("Special SymTab size:  " + this.specialSymbolTable.size());
			System.out.println();
			System.out.println("Classes: " + this.userClasses.size());
		}

	}

	public Variable getMemberPlace() {
		return this.memberPlace;
	}

	public TacFunction getMainFunction() {
		return this.mainFunction;
	}

	public Hotspot getHotspot(int hotspotId) {
		return (Hotspot) this.hotspots.get(new Integer(hotspotId));
	}

	void addHotspot(Hotspot node) {
		this.hotspots.put(node.getHotspotId(), node);
	}

	public void addIncludeNode(Include node) {
		this.includeNodes.add(node);
	}

	List<Variable> getVariablesList() {
		List<Variable> variablesList = new LinkedList<Variable>();
		for (Iterator<TacFunction> iter = this.userFunctions.values().iterator(); iter.hasNext();) {
			TacFunction function = (TacFunction) iter.next();
			variablesList.addAll(function.getSymbolTable().getVariables().values());
		}
		for (TacFunction function : this.getMethods()) {
			variablesList.addAll(function.getSymbolTable().getVariables().values());
		}
		return variablesList;
	}

	public Variable getVariable(TacFunction fm, String varName) {
		Variable var = fm.getVariable(varName);
		if (var == null) {
			var = this.superSymbolTable.getVariable(varName);
		}
		return var;

	}

	public Variable getFuncVariable(String functionName, String varName) {
		TacFunction function = this.userFunctions.get(functionName);
		Variable retMe = function.getVariable(varName);

		return retMe;
	}

	public Variable getMethodVariable(String functionName, String varName) {
		Map<String, TacFunction> class2Method = this.userMethods.get(functionName);
		if (class2Method == null || class2Method.size() != 1) {
			throw new RuntimeException("Method " + functionName + " either does not exist or has duplicates");
		}
		TacFunction method = class2Method.values().iterator().next();
		Variable retMe = method.getVariable(varName);
		if (retMe == null) {
			throw new RuntimeException("Variable " + varName + " in function " + functionName + " does not exist");
		}
		return retMe;
	}

	public Constant getConstant(String constName) {
		Constant retMe = this.constantsTable.getConstant(constName);
		if (retMe == null) {
			throw new RuntimeException("Constant " + constName + " does not exist");
		}
		return retMe;
	}

	public Constant getConstantGraceful(String constName) {
		Constant retMe = this.constantsTable.getConstant(constName);
		return retMe;
	}

	public Variable getSuperGlobal(String varName) {
		return this.superSymbolTable.getVariable(varName);
	}

	public List<Include> getIncludeNodes() {
		return this.includeNodes;
	}

	public Map<String, TacClass> getUserClasses() {
		return this.userClasses;
	}

	private void optimize(ControlFlowGraph ControlFlowGraph) {

		AbstractCfgNode startHere;

		for (startHere = ControlFlowGraph.getHead(); startHere instanceof Empty;) {
			startHere = removeCfgNode(startHere);
		}
		ControlFlowGraph.setHead(startHere);

		Iterator<AbstractCfgNode> iter = ControlFlowGraph.dfPreOrder().iterator();
		while (iter.hasNext()) {
			AbstractCfgNode current = (AbstractCfgNode) iter.next();
			if (current instanceof Empty) {
				this.removeCfgNode(current);
			}
		}
	}

	private AbstractCfgNode removeCfgNode(AbstractCfgNode cfgNode) {

		CfgEdge outEdge = cfgNode.getOutEdge(0);
		List<CfgEdge> inEdges = cfgNode.getInEdges();
		if (outEdge != null) {
			AbstractCfgNode succ = outEdge.getDest();

			succ.removeInEdge(cfgNode);

			for (Iterator<CfgEdge> iter = inEdges.iterator(); iter.hasNext();) {
				CfgEdge inEdge = (CfgEdge) iter.next();
				inEdge.setDest(succ);
				succ.addInEdge(inEdge);
			}
			return succ;
		} else {

			return null;
		}
	}

	private void transformGlobals(ControlFlowGraph ControlFlowGraph) {

		Variable globalsArray = this.superSymbolTable.getVariable("$GLOBALS");

		for (Iterator<AbstractCfgNode> iter = ControlFlowGraph.dfPreOrder().iterator(); iter.hasNext();) {

			AbstractCfgNode cfgNode = (AbstractCfgNode) iter.next();

			int varCount = -1;
			for (Iterator<Variable> varIter = cfgNode.getVariables().iterator(); varIter.hasNext();) {
				Variable var = (Variable) varIter.next();
				varCount++;

				if (var == null) {
					continue;
				}

				if (!var.isArrayElement()) {
					continue;
				}

				if (!(var.getTopEnclosingArray().equals(globalsArray))) {
					continue;
				}

				if (var.hasNonLiteralIndices()) {
					continue;
				}

				StringBuilder varNameBuffer = new StringBuilder();
				varNameBuffer.append("$");
				Iterator<AbstractTacPlace> indicesIter = var.getIndices().iterator();
				AbstractTacPlace firstIndex = (AbstractTacPlace) indicesIter.next();
				varNameBuffer.append(firstIndex.getLiteral().toString());
				while (indicesIter.hasNext()) {
					AbstractTacPlace index = (AbstractTacPlace) indicesIter.next();
					varNameBuffer.append("[");
					varNameBuffer.append(index.getLiteral().toString());
					varNameBuffer.append("]");
				}
				String varName = varNameBuffer.toString();

				Variable transformedVar = (Variable) this.makePlace(varName, this.mainSymbolTable);
				cfgNode.replaceVariable(varCount, transformedVar);
			}
		}

	}

	void replaceGlobals() {

		for (Iterator<TacFunction> funcIter = this.userFunctions.values().iterator(); funcIter.hasNext();) {

			TacFunction userFunction = (TacFunction) funcIter.next();
			if (userFunction.isMain()) {
				continue;
			}

			Map<String, Variable> declaredAsGlobal = new HashMap<String, Variable>();
			for (Iterator<AbstractCfgNode> cfgIter = userFunction.getCfg().dfPreOrder().iterator(); cfgIter
					.hasNext();) {
				AbstractCfgNode cfgNodeX = (AbstractCfgNode) cfgIter.next();
				if (!(cfgNodeX instanceof Global)) {
					continue;
				}
				Global cfgNode = (Global) cfgNodeX;
				String varName = cfgNode.getOperand().getName();
				Variable correspondingGlobal = this.mainSymbolTable.getVariable(varName);

				if (correspondingGlobal == null) {
					if (this.superSymbolTable.getVariable(varName) != null) {

					} else {

					}
				} else {
					declaredAsGlobal.put(varName, correspondingGlobal);
				}
			}
			for (Iterator<AbstractCfgNode> cfgIter = userFunction.getCfg().dfPreOrder().iterator(); cfgIter
					.hasNext();) {
				AbstractCfgNode cfgNode = (AbstractCfgNode) cfgIter.next();
				if (cfgNode instanceof Global) {
					continue;
				}
				int varCount = -1;
				for (Iterator<Variable> varIter = cfgNode.getVariables().iterator(); varIter.hasNext();) {
					Variable var = (Variable) varIter.next();
					varCount++;
					if (var == null) {
						continue;
					}
					Variable theGlobal = declaredAsGlobal.get(var.getName());
					if (theGlobal != null) {
						cfgNode.replaceVariable(varCount, theGlobal);
					}
				}
			}
		}
	}

	private Variable makePlace(String varName, SymbolTable symbolTable) {

		Variable variable = symbolTable.getVariable(varName);

		if (variable == null) {
			variable = this.superSymbolTable.getVariable(varName);
		}

		if (variable == null) {
			variable = new Variable(varName, symbolTable);
			symbolTable.add(variable);
		}

		if (symbolTable == this.superSymbolTable) {
			variable.setIsSuperGlobal(true);
		}

		return variable;
	}

	private Variable makePlace(String varName) {
		return this.makePlace(varName, (SymbolTable) ((TacFunction) this.functionStack.getLast()).getSymbolTable());
	}

	private Variable makeReturnPlace(String functionName) {
		Variable returnPlace = this.makePlace(InternalStrings.returnPrefix + functionName, this.superSymbolTable);
		((Variable) returnPlace).setIsReturnVariable(true);
		return returnPlace;
	}

	private AbstractTacPlace makeConstantPlace(String label) {

		Constant constant = this.constantsTable.getConstant(label);

		if (constant == null) {
			constant = Constant.getInstance(label);
			this.constantsTable.add(constant);
		}
		return constant;
	}

	private void addSuperGlobal(String varName) {
		AbstractTacPlace sgPlace = this.makePlace(varName, this.superSymbolTable);
		Variable var = sgPlace.getVariable();
		var.setIsSuperGlobal(true);
	}

	private static void connect(AbstractCfgNode source, AbstractCfgNode dest, int edgeType) {
		if (edgeType != CfgEdge.NO_EDGE) {

			CfgEdge edge = new CfgEdge(source, dest, edgeType);
			if (edgeType == CfgEdge.TRUE_EDGE) {
				source.setOutEdge(1, edge);
			} else {
				source.setOutEdge(0, edge);
			}
			dest.addInEdge(edge);
		}
	}

	static void connect(AbstractCfgNode source, AbstractCfgNode dest) {
		connect(source, dest, CfgEdge.NORMAL_EDGE);
	}

	static void connect(ControlFlowGraph firstCfg, ControlFlowGraph secondCfg) {
		connect(firstCfg.getTail(), secondCfg.getHead(), firstCfg.getTailEdgeType());
	}

	static void connect(ControlFlowGraph firstCfg, AbstractCfgNode dest) {
		connect(firstCfg.getTail(), dest, firstCfg.getTailEdgeType());
	}

	private static void connect(AbstractCfgNode source, ControlFlowGraph secondCfg, int edgeType) {
		connect(source, secondCfg.getHead(), edgeType);
	}

	static void connect(AbstractCfgNode source, ControlFlowGraph secondCfg) {
		connect(source, secondCfg, CfgEdge.NORMAL_EDGE);
	}

	private TacFunction addMethod(String methodName, String className, TacFunction method) {

		Map<String, TacFunction> class2Method = this.userMethods.get(methodName);
		if (class2Method == null) {
			class2Method = new HashMap<String, TacFunction>();
			this.userMethods.put(methodName, class2Method);
		}
		TacFunction existingMethod = class2Method.get(className);
		if (existingMethod != null) {
			return existingMethod;
		} else {
			class2Method.put(className, method);
			return null;
		}
	}

	private TacFunction getMethod(String methodName, String className) {
		Map<String, TacFunction> class2Method = this.userMethods.get(methodName);
		if (class2Method == null) {
			return null;
		}
		return class2Method.get(className);
	}

	void booleanHelper(ParseNode node, TacAttributes myAtts, int type) {

		Variable myPlace = newTemp();

		TacAttributes atts0 = this.expr(node.getChild(0));
		TacAttributes atts2 = this.expr(node.getChild(2));

		AbstractCfgNode trueNode = new AssignSimple(myPlace, Constant.TRUE, node);
		AbstractCfgNode falseNode = new AssignSimple(myPlace, Constant.FALSE, node);

		AbstractCfgNode emptyNode = new Empty();
		connect(trueNode, emptyNode);
		connect(falseNode, emptyNode);

		AbstractCfgNode ifNode0 = new If(atts0.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL, node.getChild(0));

		AbstractCfgNode ifNode2 = new If(atts2.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL, node.getChild(2));

		connect(atts0.getCfg(), ifNode0);

		connect(atts2.getCfg(), ifNode2);

		if (type == PhpSymbols.T_LOGICAL_OR) {

			connect(ifNode0, trueNode, CfgEdge.TRUE_EDGE);

			connect(ifNode0, atts2.getCfg(), CfgEdge.FALSE_EDGE);

			connect(ifNode2, trueNode, CfgEdge.TRUE_EDGE);

			connect(ifNode2, falseNode, CfgEdge.FALSE_EDGE);

		} else {
			connect(ifNode0, atts2.getCfg(), CfgEdge.TRUE_EDGE);

			connect(ifNode0, falseNode, CfgEdge.FALSE_EDGE);

			connect(ifNode2, trueNode, CfgEdge.TRUE_EDGE);

			connect(ifNode2, falseNode, CfgEdge.FALSE_EDGE);

		}

		myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), emptyNode));
		myAtts.setPlace(myPlace);
	}

	void expOpExp(ParseNode node, int op, TacAttributes myAtts) {

		Variable myPlace = null;
		int logId = this.tempId;

		TacAttributes atts0 = this.expr(node.getChild(0));
		if (atts0.getPlace().isVariable() && ((Variable) atts0.getPlace()).isTemp()) {
			myPlace = (Variable) atts0.getPlace();
			logId = this.tempId;
		} else {
			this.resetId(logId);
		}
		TacAttributes atts2 = null;
		if (op == 29) {
			atts2 = this.class_name_reference(node.getChild(2));
		} else {
			atts2 = this.expr(node.getChild(2));
		}
		if (myPlace == null) {
			if (atts2.getPlace() != null) {
				if (atts2.getPlace().isVariable() && ((Variable) atts2.getPlace()).isTemp()) {
					myPlace = (Variable) atts2.getPlace();
				} else {
					this.resetId(logId);
					myPlace = this.newTemp();
				}
			} else {
				this.resetId(logId);
			}
		} else {
			this.resetId(logId);
		}

		AbstractCfgNode cfgNode = new AssignBinary(myPlace, atts0.getPlace(), atts2.getPlace(), op, node);
		connect(atts0.getCfg(), atts2.getCfg());
		connect(atts2.getCfg(), cfgNode);

		myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));
		myAtts.setPlace(myPlace);

	}

	void cvarOpExp(ParseNode node, int op, TacAttributes myAtts) {

		TacAttributes atts0 = this.variable(node.getChild(0));
		TacAttributes atts2 = this.expr(node.getChild(2));

		AbstractCfgNode cfgNode = new AssignBinary((Variable) atts0.getPlace(), atts0.getPlace(), atts2.getPlace(), op,
				node);
		connect(atts0.getCfg(), atts2.getCfg());
		connect(atts2.getCfg(), cfgNode);

		myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));
		myAtts.setPlace(atts0.getPlace());
	}

	void postIncDec(ParseNode node, int op, TacAttributes myAtts) {

		Variable tempPlace = newTemp();
		TacAttributes atts0 = this.variable(node.getChild(0));
		AbstractTacPlace addMePlace = new Literal("1");

		AbstractCfgNode rescueNode = new AssignSimple(tempPlace, atts0.getPlace(), node.getChild(0));

		AbstractCfgNode cfgNode = new AssignBinary((Variable) atts0.getPlace(), atts0.getPlace(), addMePlace, op, node);

		connect(atts0.getCfg(), rescueNode);
		connect(rescueNode, cfgNode);

		myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));
		myAtts.setPlace(tempPlace);
	}

	void Clone(ParseNode Parent, ParseNode node, TacAttributes myAtts) {

		TacAttributes atts0 = this.variable(Parent);
		TacAttributes atts1 = this.expr(node.getChild(1));

		connect(atts0.getCfg(), atts1.getCfg());

		myAtts.setCfg(atts0.getCfg());
		myAtts.setPlace(atts0.getPlace());
	}

	void preIncDec(ParseNode node, int op, TacAttributes myAtts) {

		TacAttributes atts1 = this.variable(node.getChild(1));
		AbstractTacPlace addMePlace = new Literal("1");

		AbstractCfgNode cfgNode = new AssignBinary((Variable) atts1.getPlace(), atts1.getPlace(), addMePlace, op, node);

		connect(atts1.getCfg(), cfgNode);

		myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
		myAtts.setPlace(atts1.getPlace());
	}

	void opExp(ParseNode node, int op, TacAttributes myAtts) {

		Variable tempPlace = newTemp();
		TacAttributes atts1 = this.expr(node.getChild(1));

		AbstractCfgNode cfgNode = new AssignUnary(tempPlace, atts1.getPlace(), op, node);
		connect(atts1.getCfg(), cfgNode);

		myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
		myAtts.setPlace(tempPlace);
	}

	void functionHelper(ParseNode node, int paramListNum, int statNum, TacAttributes myAtts) {

		String functionName = "";
		if (node.getChild(2).getSymbol() == PhpSymbols.T_STRING) {
			functionName = node.getChild(2).getLexeme().toLowerCase();
		}
		boolean isReference = (node.getChild(1).getChild(0).getSymbol() == PhpSymbols.T_EPSILON) ? false : true;

		TacFunction existingFunction = this.userFunctions.get(functionName);
		if (existingFunction != null) {

			System.out.println("\nWarning: Duplicate function definition: " + functionName);
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			return;
		}

		CfgEntry entryNode = new CfgEntry(node);
		CfgExit exitNode = new CfgExit(node);
		ControlFlowGraph ControlFlowGraph = new ControlFlowGraph(entryNode, exitNode, CfgEdge.NO_EDGE);

		TacFunction function = new TacFunction(functionName, ControlFlowGraph, this.makeReturnPlace(functionName),
				isReference, node, "");
		this.userFunctions.put(functionName, function);

		this.functionStack.add(function);

		TacAttributes attsParamList = this.parameter_list(node.getChild(paramListNum));

		for (TacFormalParameter formalParam : attsParamList.getFormalParamList()) {
			if (formalParam.hasDefault()) {
				ControlFlowGraph defaultCfg = formalParam.getDefaultCfg();
				for (Iterator<AbstractCfgNode> defaultIter = defaultCfg.dfPreOrder().iterator(); defaultIter
						.hasNext();) {
					AbstractCfgNode defaultNode = (AbstractCfgNode) defaultIter.next();
					defaultNode.setDefaultParamPrep(entryNode);
				}
			}
		}

		function.setParams(attsParamList.getFormalParamList());

		TacAttributes attsStat = this.inner_statement_list(node.getChild(statNum));

		connect(entryNode, attsStat.getCfg());
		connect(attsStat.getCfg(), exitNode);

		AbstractCfgNode emptyNode = new Empty();
		myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));

		this.functionStack.removeLast();

		this.optimize(ControlFlowGraph);
	}

	TacFunction methodHelper(ParseNode node, int paramListNum, int statNum, String functionName, boolean isInterface) {

		boolean isReference = (node.getChild(2).getChild(0).getSymbol() == PhpSymbols.T_EPSILON) ? false : true;

		CfgEntry entryNode = new CfgEntry(node);
		CfgExit exitNode = new CfgExit(node);
		ControlFlowGraph ControlFlowGraph = new ControlFlowGraph(entryNode, exitNode, CfgEdge.NO_EDGE);

		TacFunction function;
		if (isInterface) {
			function = new TacFunction(functionName, ControlFlowGraph, this.makeReturnPlace(functionName), isReference,
					node, this.interfaceStack.getLast().getName());
		} else {
			function = new TacFunction(functionName, ControlFlowGraph, this.makeReturnPlace(functionName), isReference,
					node, this.classStack.getLast().getName());
		}
		this.functionStack.add(function);

		TacAttributes attsParamList = this.parameter_list(node.getChild(paramListNum));

		for (TacFormalParameter formalParam : attsParamList.getFormalParamList()) {
			if (formalParam.hasDefault()) {
				ControlFlowGraph defaultCfg = formalParam.getDefaultCfg();
				for (Iterator<AbstractCfgNode> defaultIter = defaultCfg.dfPreOrder().iterator(); defaultIter
						.hasNext();) {
					AbstractCfgNode defaultNode = (AbstractCfgNode) defaultIter.next();
					defaultNode.setDefaultParamPrep(entryNode);
				}
			}
		}

		function.setParams(attsParamList.getFormalParamList());

		if (node.getChild(statNum).getChild(0).getSymbol() == PhpSymbols.T_SEMICOLON) {
			function.SetAbstract(true);
		} else {
			TacAttributes attsStat = this.inner_statement_list(node.getChild(statNum).getChild(1));
			connect(entryNode, attsStat.getCfg());
			connect(attsStat.getCfg(), exitNode);
		}
		this.functionStack.removeLast();

		this.optimize(ControlFlowGraph);

		return function;
	}

	TacFunction constructorHelper(ParseNode node, String className) {

		CfgEntry entryNode = new CfgEntry(node);
		CfgExit exitNode = new CfgExit(node);
		connect(entryNode, exitNode);
		ControlFlowGraph ControlFlowGraph = new ControlFlowGraph(entryNode, exitNode, CfgEdge.NO_EDGE);

		String functionName = className + InternalStrings.methodSuffix;
		TacFunction function = new TacFunction(functionName, ControlFlowGraph, this.makeReturnPlace(functionName),
				false, node, className);

		function.setParams(new LinkedList<TacFormalParameter>());

		return function;
	}

	public void generateShadows() {
		Iterator<TacFunction> shadowIter = this.userFunctions.values().iterator();
		while (shadowIter.hasNext()) {
			TacFunction userFunction = (TacFunction) shadowIter.next();
			if (userFunction.isMain()) {
				continue;
			}
			this.generateShadows(userFunction);
		}
	}

	private void generateShadows(TacFunction function) {

		SymbolTable symTab = function.getSymbolTable();

		for (Iterator<Variable> iter = this.mainSymbolTable.getVariablesColl().iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			if (var.isTemp()) {
				continue;
			}
			symTab.addGShadow(var);
		}

		for (Iterator<TacFormalParameter> iter = function.getParams().iterator(); iter.hasNext();) {
			TacFormalParameter param = (TacFormalParameter) iter.next();
			Variable var = param.getVariable();
			if (var.isArray() || var.isArrayElement()) {
				continue;
			}
			symTab.addFShadow(var);
		}

	}

	ControlFlowGraph functionCallHelper(String calledFuncName, boolean isMethod, TacFunction calledFunction,
			List<TacActualParameter> paramList, AbstractTacPlace tempPlace, boolean backpatch, ParseNode parseNode,
			String className, Variable object) {

		if (calledFuncName.equals("define")) {

			Iterator<TacActualParameter> paramIter = paramList.iterator();
			AbstractTacPlace setMe = paramIter.next().getPlace();
			AbstractTacPlace setTo = null;
			if (paramIter.hasNext()) {
				setTo = paramIter.next().getPlace();
			}
			AbstractTacPlace caseInsensitive;
			if (paramIter.hasNext()) {
				caseInsensitive = ((TacActualParameter) paramIter.next()).getPlace();
			} else {
				caseInsensitive = Constant.FALSE;
			}

			if (setMe.isLiteral()) {
				this.makeConstantPlace(setMe.toString());
			}

			Define defineNode = new Define(setMe, setTo, caseInsensitive, parseNode);
			return new ControlFlowGraph(defineNode, defineNode);
		}

		if (BuiltinFunctions.isBuiltinFunction(calledFuncName)) {

			CallBuiltinFunction builtinNode = new CallBuiltinFunction(calledFuncName, paramList, tempPlace, parseNode);
			return new ControlFlowGraph(builtinNode, builtinNode);
		}

		Literal funcNameLit = new Literal(calledFuncName);
		Variable returnVariable = (Variable) this.makeReturnPlace(calledFuncName);

		TacFunction enclosingFunction = (TacFunction) this.functionStack.getLast();

		CallPreparation prep = new CallPreparation(parseNode);
		Call call = new Call(funcNameLit, calledFunction, parseNode, enclosingFunction, returnVariable, tempPlace,
				paramList, object);
		CallReturn callRet = new CallReturn(parseNode);

		connect(prep, call);
		connect(call, callRet);

		if (backpatch) {
			if (isMethod) {
				this.addMethodCall(enclosingFunction, prep);
				call.setCalleeClassName(className);
			} else {
				this.addFunctionCall(enclosingFunction, prep);
			}
		}
		return new ControlFlowGraph(prep, callRet);
	}

	public void addFunctionCall(TacFunction enclosingFunction, CallPreparation prepNode) {
		List<CallPreparation> nodeList = this.functionCalls.get(enclosingFunction);
		if (nodeList == null) {
			nodeList = new LinkedList<CallPreparation>();
			this.functionCalls.put(enclosingFunction, nodeList);
		}
		nodeList.add(prepNode);
	}

	public void addFunctionCalls(TacFunction enclosingFunction, List<CallPreparation> prepNodes) {
		List<CallPreparation> nodeList = this.functionCalls.get(enclosingFunction);
		if (nodeList == null) {
			nodeList = new LinkedList<CallPreparation>();
			this.functionCalls.put(enclosingFunction, nodeList);
		}
		nodeList.addAll(prepNodes);
	}

	public List<CallPreparation> getFunctionCalls(TacFunction enclosingFunction) {
		List<CallPreparation> retMe = this.functionCalls.get(enclosingFunction);
		if (retMe == null) {
			return Collections.emptyList();
		} else {
			return retMe;
		}
	}

	public void addMethodCall(TacFunction enclosingFunction, CallPreparation prepNode) {
		List<CallPreparation> nodeList = this.methodCalls.get(enclosingFunction);
		if (nodeList == null) {
			nodeList = new LinkedList<CallPreparation>();
			this.methodCalls.put(enclosingFunction, nodeList);
		}
		nodeList.add(prepNode);
	}

	public void addMethodCalls(TacFunction enclosingFunction, List<CallPreparation> prepNodes) {
		List<CallPreparation> nodeList = this.methodCalls.get(enclosingFunction);
		if (nodeList == null) {
			nodeList = new LinkedList<CallPreparation>();
			this.methodCalls.put(enclosingFunction, nodeList);
		}
		nodeList.addAll(prepNodes);
	}

	public List<CallPreparation> getMethodCalls(TacFunction enclosingFunction) {
		List<CallPreparation> retMe = this.methodCalls.get(enclosingFunction);
		if (retMe == null) {
			return Collections.emptyList();
		} else {
			return retMe;
		}
	}

	public void addSuperGlobalElements() {

		String[] indices = { "PHP_SELF", "SERVER_NAME", "HTTP_HOST", "HTTP_REFERER", "HTTP_ACCEPT_LANGUAGE",
				"SERVER_SOFTWARE", "PHP_AUTH_USER", "PHP_AUTH_PW", "PHP_AUTH_TYPE", "SCRIPT_NAME", "SCRIPT_FILENAME",
				"REQUEST_URI", "QUERY_STRING", "SCRIPT_URI" };

		String superName = "$_SERVER";
		Variable superVar = this.superSymbolTable.getVariable(superName);
		Variable var = null;
		for (int i = 0; i < indices.length; i++) {
			var = this.superSymbolTable.getVariable(superName + "[" + indices[i] + "]");
			if (var == null) {
				this.makeArrayElementPlace(superVar, new Literal(indices[i]));
			}
		}

		Variable argv = this.superSymbolTable.getVariable(superName + "[argv]");
		if (argv == null) {
			argv = (Variable) this.makeArrayElementPlace(superVar, new Literal("argv"));
		}
		var = this.superSymbolTable.getVariable(superName + "[argv][0]");
		if (var == null) {
			this.makeArrayElementPlace(argv, new Literal("0"));
		}

		superName = "$HTTP_SERVER_VARS";
		superVar = this.superSymbolTable.getVariable(superName);
		for (int i = 0; i < indices.length; i++) {
			var = this.superSymbolTable.getVariable(superName + "[" + indices[i] + "]");
			if (var == null) {
				this.makeArrayElementPlace(superVar, new Literal(indices[i]));
			}
		}

		argv = this.superSymbolTable.getVariable(superName + "[argv]");
		if (argv == null) {
			argv = (Variable) this.makeArrayElementPlace(superVar, new Literal("argv"));
		}
		var = this.superSymbolTable.getVariable(superName + "[argv][0]");
		if (var == null) {
			this.makeArrayElementPlace(argv, new Literal("0"));
		}

	}

	Variable makeArrayElementPlace(AbstractTacPlace arrayPlace, AbstractTacPlace offsetPlace) {

		Variable arrayVar = arrayPlace.getVariable();

		if (arrayVar.isArray() == false) {
			arrayVar.setIsArray(true);
		}
		String offsetString = offsetPlace.toString();

		boolean superGlobal;
		SymbolTable symbolTable;
		if (arrayVar.isSuperGlobal()) {
			symbolTable = this.superSymbolTable;
			superGlobal = true;
		} else {
			symbolTable = (SymbolTable) ((TacFunction) this.functionStack.getLast()).getSymbolTable();
			superGlobal = false;
		}

		String arrayElementName = arrayVar.getName() + "[" + offsetString + "]";

		Variable arrayElementVar = symbolTable.getVariable(arrayElementName);
		if (arrayElementVar == null) {

			arrayElementVar = new Variable(arrayElementName, symbolTable);
			symbolTable.add(arrayElementVar);

			arrayElementVar.setArrayElementAttributes(arrayVar, offsetPlace);
			arrayElementVar.setIsSuperGlobal(superGlobal);

			arrayVar.addElement(arrayElementVar);
		}

		return arrayElementVar;
	}

	AbstractCfgNode arrayPairListHelper(AbstractTacPlace arrayPlace, AbstractTacPlace offsetPlace,
			AbstractTacPlace valuePlace, boolean reference, ParseNode node) {

		Variable arrayElementPlace = this.makeArrayElementPlace(arrayPlace, offsetPlace);

		if (reference) {
			return (new AssignReference(arrayElementPlace, (Variable) valuePlace, node));
		} else {
			return (new AssignSimple(arrayElementPlace, valuePlace, node));
		}

	}

	void encapsListHelper(ParseNode node, TacAttributes myAtts) {

		TacAttributes attsList = this.encaps_list(node.getChild(0));
		AbstractTacPlace stringPlace = new Literal(node.getChild(1).getLexeme(), false);
		if (attsList.getEncapsList() == null) {
		}

		EncapsList encapsList = attsList.getEncapsList();
		encapsList.add((Literal) stringPlace);
		myAtts.setEncapsList(encapsList);

	}

	AbstractTacPlace exprVarHelper(AbstractTacPlace exprPlace) {

		AbstractTacPlace myPlace = null;
		if (exprPlace.isLiteral()) {
			String literal = exprPlace.toString();

			if (Character.isDigit(literal.charAt(0))) {
				myPlace = this.makePlace("${" + literal + "}");
			} else {
				myPlace = this.makePlace("$" + literal);
			}

		} else if (exprPlace.isVariable()) {
			myPlace = this.makePlace("${" + exprPlace.getVariable().getName() + "}");
			myPlace.getVariable().setDependsOn(exprPlace);
		} else if (exprPlace.isConstant()) {
			myPlace = this.makePlace("${" + exprPlace.getConstant().getLabel() + "}");
			myPlace.getVariable().setDependsOn(exprPlace);
		} else {
			throw new RuntimeException("SNH");
		}

		return myPlace;
	}

	private void foreachHelper(ParseNode node, TacAttributes attsArray, TacAttributes myAtts) {

		Variable arrayPlace = this.newTemp();
		AbstractCfgNode backupNode = new AssignSimple(arrayPlace, attsArray.getPlace(), node);

		List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
		paramList.add(new TacActualParameter(arrayPlace, false));
		int logId = this.tempId;
		AbstractTacPlace tempPlace = this.newTemp();

		ControlFlowGraph resetCallCfg = this.functionCallHelper("reset", false, null, paramList, tempPlace, true, node,
				null, null);

		ControlFlowGraph eachCallCfg = this.functionCallHelper("each", false, null, paramList, tempPlace, true, node,
				null, null);

		connect(attsArray.getCfg(), backupNode);
		connect(backupNode, resetCallCfg.getHead());
		connect(resetCallCfg.getTail(), eachCallCfg.getHead());

		AbstractCfgNode ifNode = new If(tempPlace, Constant.TRUE, TacOperators.IS_EQUAL, node);

		AbstractCfgNode endNode = new Empty();

		TacAttributes attsOptional = this.foreach_optional_arg(node.getChild(5));
		if (attsOptional == null) {

			TacAttributes attsValue = this.foreach_variable(node.getChild(4));

			AbstractTacPlace tempPlaceValue = this.makeArrayElementPlace(tempPlace, new Literal("1"));

			AbstractCfgNode valueNode = new AssignSimple((Variable) attsValue.getPlace(), tempPlaceValue,
					node.getChild(4));

			connect(eachCallCfg.getTail(), attsValue.getCfg());
			connect(attsValue.getCfg(), valueNode);
			connect(valueNode, ifNode);

		} else {

			TacAttributes attsKey = this.foreach_variable(node.getChild(4));
			TacAttributes attsValue = attsOptional;

			AbstractTacPlace tempPlaceKey = this.makeArrayElementPlace(tempPlace, new Literal("0"));
			AbstractTacPlace tempPlaceValue = this.makeArrayElementPlace(tempPlace, new Literal("1"));

			AbstractCfgNode keyNode = new AssignSimple((Variable) attsKey.getPlace(), tempPlaceKey, node.getChild(4));

			AbstractCfgNode valueNode = new AssignSimple((Variable) attsValue.getPlace(), tempPlaceValue,
					node.getChild(5));

			connect(eachCallCfg.getTail(), attsKey.getCfg());
			connect(attsKey.getCfg(), attsValue.getCfg());
			connect(attsValue.getCfg(), keyNode);
			connect(keyNode, valueNode);
			connect(valueNode, ifNode);
		}

		this.resetId(logId);

		this.continueTargetStack.add(eachCallCfg.getHead());
		this.breakTargetStack.add(endNode);

		TacAttributes attsStatement = this.foreach_statement(node.getChild(7));
		connect(ifNode, attsStatement.getCfg().getHead(), CfgEdge.TRUE_EDGE);
		connect(attsStatement.getCfg(), eachCallCfg.getHead());
		connect(ifNode, endNode, CfgEdge.FALSE_EDGE);

		this.continueTargetStack.removeLast();
		this.breakTargetStack.removeLast();

		myAtts.setCfg(new ControlFlowGraph(attsArray.getCfg().getHead(), endNode));
	}

	public void backpatch() {
		backpatch(false, false, null, null);
	}

	public void backpatch(boolean riskMethods, boolean finalPass, TypeAnalysis typeAnalysis, CallGraph callGraph) {

		for (List<CallPreparation> callList : this.methodCalls.values()) {
			for (CallPreparation prepNode : callList) {

				Call callNode = prepNode.getCallNode();
				CallReturn retNode = prepNode.getCallRetNode();

				boolean reachable = true;
				TacFunction enclosingFunction = callNode.getEnclosingFunction();
				if (callGraph != null) {
					if (!callGraph.reachable(enclosingFunction)) {
						reachable = false;
					}
				}

				AbstractTacPlace functionNamePlace = prepNode.getFunctionNamePlace();

				TacFunction callee = null;
				Map<String, TacFunction> class2Method = this.userMethods.get(functionNamePlace.toString());

				if (class2Method != null) {
					String calleeClassName = callNode.getCalleeClassName();

					if (calleeClassName != null) {
						callee = class2Method.get(calleeClassName);
					} else if (riskMethods) {

						if (class2Method.size() == 1) {
							callee = class2Method.values().iterator().next();
						} else {
							boolean resolved = false;

							if (typeAnalysis != null) {
								Set<Type> types = typeAnalysis.getType(callNode.getObject(), callNode);
								if (types != null && types.size() == 1) {
									Type type = types.iterator().next();
									callee = class2Method.get(type.getClassName());
									if (callee != null) {
										resolved = true;
									} else {
									}
								}
							}
							if (finalPass && !resolved && reachable) {
								System.out.println("reachable: " + enclosingFunction.getName());
								System.out
										.println("Warning: can't resolve method call (same name in different classes)");
								System.out.println("- name:    " + functionNamePlace);
								System.out.println("- call:    " + prepNode.getLoc());
								System.out.println("- classes: " + class2Method.keySet());
							}
						}
					}

				} else {

					if (finalPass && reachable) {
						System.out.println("Warning: can't resolve method call (no definition found)");
						System.out.println("- name:    " + functionNamePlace);
						System.out.println("- call:    " + prepNode.getLoc());
					}

				}

				if (callee == null) {

					if (finalPass) {
						this.replaceUnknownCall(prepNode, functionNamePlace.toString(), true);
					}
					continue;

				} else {
					List<TacActualParameter> actualParams = prepNode.getParamList();
					List<TacFormalParameter> formalParams = callee.getParams();
					int actualSize = actualParams.size();
					int formalSize = formalParams.size();
					if (actualSize != formalSize) {
						if (actualSize > formalSize) {
							System.out.println("Warning: More actual than formal params");
							System.out.println("- call:    " + prepNode.getLoc());
							System.out.println("- callee:  " + prepNode.getFunctionNamePlace().toString());
							System.out.println("- decl:    " + callee.getLoc());
							while (actualParams.size() > formalParams.size()) {
								actualParams.remove(actualParams.size() - 1);
							}
						} else {
							if (finalPass) {
								int i = 0;
								for (TacFormalParameter formalParam : formalParams) {
									i++;
									if (i <= actualSize) {
										continue;
									}
									if (!formalParam.hasDefault()) {
										System.out.println("Warning: Not enough actual params");
										System.out.println("- call:    " + prepNode.getLoc());
										System.out.println("- callee:  " + prepNode.getFunctionNamePlace().toString());
										System.out.println("- decl:    " + callee.getLoc());
										break;
									}
								}
							}
						}
					}
				}

				callNode.setCallee(callee);
				retNode.setRetVar((Variable) callee.getRetVar());
			}
		}
		for (List<CallPreparation> callList : this.functionCalls.values()) {
			for (CallPreparation prepNode : callList) {

				Call callNode = prepNode.getCallNode();
				CallReturn retNode = (CallReturn) callNode.getOutEdge(0).getDest();

				AbstractTacPlace functionNamePlace = prepNode.getFunctionNamePlace();

				boolean reachable = true;
				if (callGraph != null) {
					TacFunction enclosingFunction = callNode.getEnclosingFunction();
					if (!callGraph.reachable(enclosingFunction)) {
						reachable = false;
					}
				}

				TacFunction callee = (TacFunction) this.userFunctions.get(functionNamePlace.toString());
				if (callee == null) {

					if (BuiltinFunctions.isBuiltinFunction(functionNamePlace.toString())) {

						if (true)
							throw new RuntimeException("SNH");

					} else {

						if (finalPass) {

							if (reachable) {
								System.out.println("Warning: can't find function " + functionNamePlace);
								System.out.println("- " + prepNode.getLoc());
							}

							this.replaceUnknownCall(prepNode, functionNamePlace.toString(), false);
						}

						continue;
					}

				} else {

					List<TacActualParameter> actualParams = prepNode.getParamList();
					List<TacFormalParameter> formalParams = callee.getParams();
					int actualSize = actualParams.size();
					int formalSize = formalParams.size();
					if (actualSize != formalSize) {
						if (actualSize > formalSize) {

							System.out.println("Warning: More actual than formal params");
							System.out.println("- call:    " + prepNode.getLoc());
							System.out.println("- callee:  " + prepNode.getFunctionNamePlace().toString());
							System.out.println("- decl:    " + callee.getLoc());
							while (actualParams.size() > formalParams.size()) {
								actualParams.remove(actualParams.size() - 1);
							}
						} else {

							if (finalPass) {
								int i = 0;
								for (TacFormalParameter formalParam : formalParams) {
									i++;
									if (i <= actualSize) {
										continue;
									}
									if (!formalParam.hasDefault()) {
										System.out.println("Warning: Not enough actual params");
										System.out.println("- call:    " + prepNode.getLoc());
										System.out.println("- callee:  " + prepNode.getFunctionNamePlace().toString());
										break;
									}
								}
							}
						}
					}
				}

				callNode.setCallee(callee);
				retNode.setRetVar((Variable) callee.getRetVar());
			}
		}
	}

	private void replaceUnknownCall(CallPreparation prepNode, String functionName, boolean isMethod) {

		CallReturn callRet = prepNode.getCallRetNode();

		CallUnknownFunction callUnknown = new CallUnknownFunction(functionName, prepNode.getParamList(),
				callRet.getTempVar(), prepNode.getParseNode(), isMethod);

		for (CfgEdge inEdge : prepNode.getInEdges()) {
			inEdge.setDest(callUnknown);
			callUnknown.addInEdge(inEdge);
		}

		List<AbstractCfgNode> succs = prepNode.getCallRetNode().getSuccessors();
		if (succs.size() == 1) {
			AbstractCfgNode succ = succs.get(0);
			succ.removeInEdge(callRet);
			connect(callUnknown, succ);
		} else if (succs.size() == 0) {
		} else {
			throw new RuntimeException("SNH");
		}

	}

	void start(ParseNode node) {

		AbstractCfgNode entryNode = new CfgEntry(node);
		AbstractCfgNode exitNode = new CfgExit(node);

		ControlFlowGraph ControlFlowGraph = new ControlFlowGraph(entryNode, exitNode, CfgEdge.NO_EDGE);

		String mainFunctionName = InternalStrings.mainFunctionName;

		TacFunction function = new TacFunction(mainFunctionName, ControlFlowGraph,
				this.makeReturnPlace(mainFunctionName), false, node, "");
		List<TacFormalParameter> l = Collections.emptyList();
		function.setParams(l);
		function.setIsMain(true);

		this.userFunctions.put(mainFunctionName, function);

		this.mainFunction = function;

		this.mainSymbolTable = function.getSymbolTable();

		this.functionStack.add(function);

		TacAttributes atts0 = this.top_statement_list(node.getChild(0));

		connect(entryNode, atts0.getCfg());
		connect(atts0.getCfg(), exitNode);

		this.functionStack.removeLast();
		this.optimize(ControlFlowGraph);

		for (Iterator<TacFunction> iter = this.userFunctions.values().iterator(); iter.hasNext();) {
			TacFunction userFunction = (TacFunction) iter.next();
			this.transformGlobals(userFunction.getCfg());
		}

	}

	TacAttributes top_statement_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.top_statement_list: {
			if (firstChild != null && node != null) {
				if (node.children().size() > 1) {
					int logId = this.tempId;
					TacAttributes atts0 = this.top_statement_list(firstChild);
					TacAttributes atts1 = this.top_statement(node.getChild(1));

					connect(atts0.getCfg(), atts1.getCfg());
					myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
							atts1.getCfg().getTailEdgeType()));
					this.resetId(logId);
				}
			}
			break;
		}
		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		}
		return myAtts;
	}

	TacAttributes namespace_name(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		myAtts.setEncapsList(new EncapsList());

		if (node.children().size() == 1) {
			ParseNode firstChild = node.getChild(0);
			myAtts.getEncapsList().add(new Literal(firstChild.tokenContent()));
			myAtts.setPlace(new Literal(firstChild.getLexeme()));
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
		} else {
			ParseNode firstChild = node.getChild(0);
			myAtts = this.namespace_name(firstChild);
			myAtts.getEncapsList().add(new Literal(node.getChild(2).getLexeme()));
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
		}

		return myAtts;
	}

	TacAttributes top_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.statement: {
			TacAttributes atts0 = this.statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.function_declaration_statement: {
			TacAttributes atts0 = this.function_declaration_statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.class_declaration_statement: {
			TacAttributes atts0 = this.class_declaration_statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_HALT_COMPILER: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		case PhpSymbols.T_NAMESPACE: {
			if (node.getChild(2).getSymbol() == PhpSymbols.T_SEMICOLON) {
				TacAttributes atts0 = this.namespace_name(node.getChild(1));
				String namespaceName = atts0.getEncapsListString();
				TacNamespace n = new TacNamespace(namespaceName, node);
				TacNamespace existingNamespace = this.userNameSpaces.get(namespaceName);
				if (existingNamespace == null) {
					this.userNameSpaces.put(namespaceName, n);
					this.nameSpaceStack.add(n);
					this.nameSpaceStack.removeLast();
				}
				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				break;
			}

			else if (node.getChild(2).getSymbol() == PhpSymbols.T_OPEN_CURLY_BRACES) {
				TacAttributes atts0 = this.namespace_name(node.getChild(1));
				String namespaceName = atts0.getEncapsListString();
				TacNamespace n = new TacNamespace(namespaceName, node);
				TacNamespace existingNamespace = this.userNameSpaces.get(namespaceName);
				if (existingNamespace == null) {
					this.userNameSpaces.put(namespaceName, n);
					this.nameSpaceStack.add(n);
					this.top_statement_list(node.getChild(3));
					this.nameSpaceStack.removeLast();
				}
				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				break;
			} else if (node.getChild(2).getSymbol() == PhpSymbols.top_statement_list) {
				this.top_statement_list(node.getChild(2));

				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				break;
			}
		}
		case PhpSymbols.T_USE: {
			TacAttributes atts0 = this.use_declarations(node.getChild(1));
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.constant_declaration: {
			this.constant_declaration(node.getChild(0));
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		}
		return myAtts;
	}

	TacAttributes use_declarations(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.use_declarations: {
			int logId = this.tempId;
			TacAttributes atts0 = this.use_declarations(firstChild);
			TacAttributes atts1 = this.use_declaration(node.getChild(2));

			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));
			this.resetId(logId);
			break;
		}
		case PhpSymbols.use_declaration: {
			TacAttributes atts0 = this.use_declaration(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		}
		return myAtts;

	}

	TacAttributes use_declaration(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.namespace_name: {
			TacAttributes atts0 = this.namespace_name(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_NS_SEPARATOR: {
			TacAttributes atts0 = this.namespace_name(node.getChild(1));
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		}
		return myAtts;

	}

	TacAttributes constant_declaration(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.constant_declaration: {
			int logId = this.tempId;
			TacAttributes atts0 = this.constant_declaration(firstChild);
			TacAttributes atts1 = this.static_expr(node.getChild(4));

			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));
			this.resetId(logId);
			break;
		}
		case PhpSymbols.T_CONST: {
			TacAttributes atts0 = this.static_expr(node.getChild(3));

			myAtts.setCfg(atts0.getCfg());
			break;
		}

		}
		return myAtts;

	}

	TacAttributes inner_statement_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.inner_statement_list: {
			int logId = this.tempId;
			TacAttributes atts0 = this.inner_statement_list(firstChild);
			TacAttributes atts1 = this.inner_statement(node.getChild(1));
			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));
			this.resetId(logId);
			break;
		}

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes inner_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.statement: {
			TacAttributes atts0 = this.statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.function_declaration_statement: {
			TacAttributes atts0 = this.function_declaration_statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.class_declaration_statement: {
			TacAttributes atts0 = this.class_declaration_statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.T_HALT_COMPILER: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_OPEN_CURLY_BRACES: {
			TacAttributes atts1 = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		case PhpSymbols.T_IF: {
			if (node.getChild(4).getSymbol() == PhpSymbols.statement) {

				AbstractCfgNode endIfNode = new Empty();

				int logId = this.tempId;
				TacAttributes attsExpr = this.expr(node.getChild(2));
				this.resetId(logId);
				TacAttributes attsStatement = this.statement(node.getChild(4));
				TacAttributes attsElse = this.else_single(node.getChild(6));
				TacAttributes attsElif = this.elseif_list(node.getChild(5), endIfNode, attsElse.getCfg().getHead());

				AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
						node.getChild(2));

				connect(attsExpr.getCfg(), ifNode);
				connect(ifNode, attsElif.getCfg(), CfgEdge.FALSE_EDGE);
				connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
				connect(attsStatement.getCfg(), endIfNode);
				connect(attsElse.getCfg(), endIfNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), endIfNode));

			} else {

				AbstractCfgNode endIfNode = new Empty();

				int logId = this.tempId;
				TacAttributes attsExpr = this.expr(node.getChild(2));
				this.resetId(logId);
				TacAttributes attsStatement = this.inner_statement_list(node.getChild(5));
				TacAttributes attsElse = this.new_else_single(node.getChild(7));
				TacAttributes attsElif = this.new_elseif_list(node.getChild(6), endIfNode, attsElse.getCfg().getHead());

				AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
						node.getChild(2));

				connect(attsExpr.getCfg(), ifNode);
				connect(ifNode, attsElif.getCfg(), CfgEdge.FALSE_EDGE);
				connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
				connect(attsStatement.getCfg(), endIfNode);
				connect(attsElse.getCfg(), endIfNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), endIfNode));
			}

			break;
		}

		case PhpSymbols.T_WHILE: {
			AbstractCfgNode endWhileNode = new Empty();
			this.breakTargetStack.add(endWhileNode);
			int logId = this.tempId;
			TacAttributes attsExpr = this.expr(node.getChild(2));
			this.resetId(logId);
			this.continueTargetStack.add(attsExpr.getCfg().getHead());
			TacAttributes attsStatement = this.while_statement(node.getChild(4));

			AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
					node.getChild(2));

			connect(attsExpr.getCfg(), ifNode);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(ifNode, endWhileNode, CfgEdge.FALSE_EDGE);
			connect(attsStatement.getCfg(), attsExpr.getCfg());

			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), endWhileNode));

			this.continueTargetStack.removeLast();
			this.breakTargetStack.removeLast();

			break;
		}

		case PhpSymbols.T_DO: {
			AbstractCfgNode endDoNode = new Empty();
			this.breakTargetStack.add(endDoNode);
			TacAttributes attsStatement = this.statement(node.getChild(1));
			int logId = this.tempId;
			TacAttributes attsExpr = this.expr(node.getChild(4));
			this.resetId(logId);
			this.continueTargetStack.add(attsStatement.getCfg().getHead());

			AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
					node.getChild(4));

			connect(attsStatement.getCfg(), attsExpr.getCfg());
			connect(attsExpr.getCfg(), ifNode);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(ifNode, endDoNode, CfgEdge.FALSE_EDGE);

			myAtts.setCfg(new ControlFlowGraph(attsStatement.getCfg().getHead(), endDoNode));

			this.continueTargetStack.removeLast();
			this.breakTargetStack.removeLast();

			break;
		}

		case PhpSymbols.T_FOR: {
			AbstractCfgNode endForNode = new Empty();
			this.breakTargetStack.add(endForNode);

			TacAttributes attsExpr1 = this.for_expr(node.getChild(2));
			TacAttributes attsExpr2 = this.for_expr(node.getChild(4));
			TacAttributes attsExpr3 = this.for_expr(node.getChild(6));

			this.continueTargetStack.add(attsExpr3.getCfg().getHead());

			TacAttributes attsStatement = this.for_statement(node.getChild(8));

			AbstractCfgNode ifNode = new If(attsExpr2.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
					node.getChild(4));

			connect(attsExpr1.getCfg(), attsExpr2.getCfg());
			connect(attsExpr2.getCfg(), ifNode);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(ifNode, endForNode, CfgEdge.FALSE_EDGE);
			connect(attsStatement.getCfg(), attsExpr3.getCfg());
			connect(attsExpr3.getCfg(), attsExpr2.getCfg());

			myAtts.setCfg(new ControlFlowGraph(attsExpr1.getCfg().getHead(), endForNode));

			this.continueTargetStack.removeLast();
			this.breakTargetStack.removeLast();

			break;
		}

		case PhpSymbols.T_SWITCH: {
			AbstractCfgNode endSwitchNode = new Empty();
			AbstractCfgNode defaultJumpNode = new Empty();

			this.continueTargetStack.add(endSwitchNode);
			this.breakTargetStack.add(endSwitchNode);

			TacAttributes attsExpr = this.expr(node.getChild(2));
			TacAttributes attsList = this.switch_case_list(node.getChild(4), attsExpr.getPlace(), defaultJumpNode,
					endSwitchNode);

			if (attsList.getDefaultNode() != null) {
				connect(defaultJumpNode, attsList.getDefaultNode());
			} else {
				connect(defaultJumpNode, endSwitchNode);
			}

			connect(attsExpr.getCfg(), attsList.getCfg());

			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), endSwitchNode));

			this.continueTargetStack.removeLast();
			this.breakTargetStack.removeLast();
			break;
		}

		case PhpSymbols.T_BREAK: {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_SEMICOLON) {
				AbstractCfgNode cfgNode = new Empty();
				AbstractCfgNode breakTarget = null;
				try {
					breakTarget = (AbstractCfgNode) this.breakTargetStack.getLast();
				} catch (NoSuchElementException e) {
					System.out.println("Invalid break statement:");
					throw new RuntimeException(e.getMessage());
				}
				connect(cfgNode, breakTarget);
				myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));
			} else {

				boolean isNumber = false;
				ParseNode maybeNumber = null;
				try {
					maybeNumber = node.getChild(1).getChild(0).getChild(0).getChild(0);
					if (maybeNumber.getSymbol() == PhpSymbols.T_LNUMBER) {
						isNumber = true;
					}
				} catch (IndexOutOfBoundsException ex) {
				}

				if (isNumber) {
					int breakDepth = Integer.parseInt(maybeNumber.getLexeme());

					AbstractCfgNode cfgNode = new Empty();
					AbstractCfgNode breakTarget = (AbstractCfgNode) this.breakTargetStack
							.get(this.breakTargetStack.size() - breakDepth);
					connect(cfgNode, breakTarget);
					myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));

				} else {
					System.out.println("Unsupported 'break' in file " + this.file.getAbsolutePath() + ", line "
							+ node.getLinenoLeft());
					throw new RuntimeException();
				}
			}

			break;
		}

		case PhpSymbols.T_CONTINUE: {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_SEMICOLON) {
				AbstractCfgNode cfgNode = new Empty();
				try {
					AbstractCfgNode continueTarget = (AbstractCfgNode) this.continueTargetStack.getLast();
					connect(cfgNode, continueTarget);
					myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));
				} catch (NoSuchElementException e) {
					System.out.println("Warning: Unsupported 'continue'");
					System.out.println("- " + node.getName());
					myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
				}
			} else {
				boolean isNumber = false;
				ParseNode maybeNumber = null;
				try {
					maybeNumber = node.getChild(1).getChild(0).getChild(0).getChild(0);
					if (maybeNumber.getSymbol() == PhpSymbols.T_LNUMBER) {
						isNumber = true;
					}
				} catch (IndexOutOfBoundsException ex) {
				}

				if (isNumber) {
					int continueDepth = Integer.parseInt(maybeNumber.getLexeme());

					AbstractCfgNode cfgNode = new Empty();
					AbstractCfgNode continueTarget = (AbstractCfgNode) this.continueTargetStack
							.get(this.continueTargetStack.size() - continueDepth);
					connect(cfgNode, continueTarget);
					myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));

				} else {
					System.out.println("Unsupported 'continue' in file " + this.file.getAbsolutePath() + ", line "
							+ node.getLinenoLeft());
					throw new RuntimeException();
				}

			}
			break;
		}

		case PhpSymbols.T_RETURN: {
			ParseNode secondChild = node.getChild(1);
			int secondSymbol = secondChild.getSymbol();

			if (secondSymbol == PhpSymbols.T_SEMICOLON) {
				AbstractCfgNode emptyNode = new Empty();
				connect(emptyNode, ((TacFunction) this.functionStack.getLast()).getCfg().getTail());

				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode, CfgEdge.NO_EDGE));

			}

			else {
				TacAttributes attsExpr = this.expr(secondChild);

				TacFunction function = (TacFunction) this.functionStack.getLast();
				Variable retVarPlace = function.getRetVar();
				AbstractCfgNode exitNode = function.getCfg().getTail();

				AbstractCfgNode cfgNode = new AssignSimple(retVarPlace, attsExpr.getPlace(), secondChild);

				connect(cfgNode, exitNode);

				connect(attsExpr.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode, CfgEdge.NO_EDGE));
			}

			break;
		}
		case PhpSymbols.T_GLOBAL: {
			TacAttributes atts1 = this.global_var_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		case PhpSymbols.T_STATIC: {
			TacAttributes atts1 = this.static_var_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		case PhpSymbols.T_ECHO: {
			TacAttributes atts1 = this.echo_expr_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		case PhpSymbols.T_INLINE_HTML: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.expr: {
			TacAttributes atts0 = this.expr(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_USE: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_UNSET: {
			TacAttributes atts2 = this.variable_list(node.getChild(2));
			myAtts.setCfg(atts2.getCfg());
			break;
		}

		case PhpSymbols.T_FOREACH: {

			if (node.getChild(2).getSymbol() == PhpSymbols.expr) {
				TacAttributes attsArray = this.expr(node.getChild(2));
				this.foreachHelper(node, attsArray, myAtts);

			}

			break;
		}

		case PhpSymbols.T_DECLARE: {
			TacAttributes attsStatement = this.declare_statement(node.getChild(4));
			myAtts.setCfg(attsStatement.getCfg());
			break;
		}
		case PhpSymbols.T_SEMICOLON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_TRY: {
			int logId = this.tempId;
			TacAttributes atts0 = this.inner_statement_list(node.getChild(2));
			TacAttributes atts1 = this.inner_statement_list(node.getChild(10));
			TacAttributes atts2 = this.additional_catches(node.getChild(12));

			connect(atts0.getCfg(), atts1.getCfg());
			connect(atts1.getCfg(), atts2.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail(),
					atts2.getCfg().getTailEdgeType()));
			this.resetId(logId);
			break;

		}

		case PhpSymbols.T_THROW: {
			TacAttributes attsExpr = this.expr(node.getChild(1));

			TacFunction function = (TacFunction) this.functionStack.getLast();
			AbstractCfgNode exitNode = function.getCfg().getTail();

			AbstractCfgNode cfgNodeThrow = new Throw(node.getChild(1));

			connect(cfgNodeThrow, exitNode);

			connect(attsExpr.getCfg(), cfgNodeThrow);

			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNodeThrow, CfgEdge.NO_EDGE));

			break;
		}
		case PhpSymbols.T_GOTO: {
			myAtts.setPlace(makeConstantPlace(node.getChild(1).getLexeme()));
			AbstractTacPlace goToString = myAtts.getPlace();

			AbstractCfgNode cfgNodeGoto = new Goto(goToString, node.getChild(1));
			myAtts.setCfg(new ControlFlowGraph(cfgNodeGoto, cfgNodeGoto));
			break;
		}
		case PhpSymbols.T_STRING: {
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			myAtts.setPlace(makeConstantPlace(firstChild.getLexeme()));
			break;
		}
		}
		return myAtts;
	}

	TacAttributes additional_catches(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.non_empty_additional_catches: {
			TacAttributes atts0 = this.non_empty_additional_catches(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		}
		return myAtts;
	}

	TacAttributes non_empty_additional_catches(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.additional_catch: {
			TacAttributes atts0 = this.additional_catch(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.non_empty_additional_catches:

		{
			int logId = this.tempId;
			TacAttributes atts0 = this.non_empty_additional_catches(firstChild);
			TacAttributes atts1 = this.additional_catch(node.getChild(1));
			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));
			this.resetId(logId);
			break;
		}

		}
		return myAtts;
	}

	TacAttributes additional_catch(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_CATCH: {
			TacAttributes atts0 = this.inner_statement_list(node.getChild(6));
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		}
		return myAtts;
	}

	TacAttributes variable_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {

		case PhpSymbols.variable: {
			TacAttributes atts0 = this.variable(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}
		case PhpSymbols.variable_list: {
			TacAttributes atts0 = this.variable_list(firstChild);
			TacAttributes atts2 = this.variable(node.getChild(2));

			connect(atts0.getCfg(), atts2.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));
			break;

		}

		}
		return myAtts;
	}

	TacAttributes function_declaration_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_FUNCTION: {
			this.functionHelper(node, 4, 7, myAtts);
			break;
		}
		}
		return myAtts;
	}

	TacAttributes class_declaration_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_entry_type: {
			String className = node.getChild(1).getLexeme().toLowerCase();
			TacClass c = new TacClass(className, node);
			TacClass existingClass = this.userClasses.get(className);
			if (existingClass == null) {
				this.extends_from(node.getChild(2), c);
				this.implements_list(node.getChild(3), c);
				this.class_entry_type(firstChild, c);
				this.userClasses.put(className, c);
				this.classStack.add(c);
				this.class_statement_list(node.getChild(5), c);
				this.classStack.removeLast();

				String constructorName = className + InternalStrings.methodSuffix;
				if (this.getMethod(constructorName, className) == null) {

					TacFunction constructor = this.constructorHelper(node.getChild(0), className);
					c.addMethod(constructorName, constructor);

					TacFunction existingMethod = this.addMethod(constructorName, className, constructor);
					if (existingMethod != null) {
						throw new RuntimeException("SNH");
					}
				}

			} else {
				System.out.println("\nWarning: Duplicate class definition: " + className);
				System.out.println("- using: " + existingClass.getLoc());
			}

			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;

		}

		case PhpSymbols.interface_entry: {
			String interfaceName = node.getChild(1).getLexeme().toLowerCase();

			TacInterface i = new TacInterface(interfaceName, node);
			TacInterface existingInteface = this.userInterfaces.get(interfaceName);
			if (existingInteface == null) {

				this.interface_extends_list(node.getChild(2), i);
				this.userInterfaces.put(interfaceName, i);
				this.interfaceStack.add(i);
				this.class_statement_list(node.getChild(4), i);
				this.interfaceStack.removeLast();

			} else {
				System.out.println("\nWarning: Duplicate interface definition: " + interfaceName);
				System.out.println("- using: " + existingInteface.getLoc());
			}

			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		}
		return myAtts;
	}

	TacAttributes class_entry_type(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_ABSTRACT: {
			c.setIsAbstrace(true);
			c.setIsFinal(false);
			break;

		}
		case PhpSymbols.T_FINAL: {
			c.setIsAbstrace(false);
			c.setIsFinal(true);
			break;
		}
		case PhpSymbols.T_CLASS: {
			c.setIsAbstrace(false);
			c.setIsFinal(false);
			break;
		}

		}
		return myAtts;
	}

	TacAttributes extends_from(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_EPSILON: {
			c.setSuperClassName("");
			c.setSuperClass(null);
			break;
		}

		case PhpSymbols.T_EXTENDS: {
			myAtts = this.fully_qualified_class_name(node.getChild(1));
			String ExtendedClassName = myAtts.getEncapsListString();
			c.setSuperClassName(ExtendedClassName);

			TacClass existingClass = this.userClasses.get(ExtendedClassName);
			if (existingClass != null) {
				c.setSuperClass(existingClass);
			} else {
				c.setSuperClass(null);
			}
		}

		}
		return myAtts;
	}

	TacAttributes interface_extends_list(ParseNode node, TacInterface i) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_EPSILON: {
			break;

		}
		case PhpSymbols.interface_list: {
			myAtts = this.interface_list(node.getChild(1), i);
		}

		}
		return myAtts;
	}

	TacAttributes implements_list(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_EPSILON: {
			break;
		}

		case PhpSymbols.T_IMPLEMENTS: {
			myAtts = this.interface_list(node.getChild(1), c);
		}

		}
		return myAtts;
	}

	TacAttributes interface_list(ParseNode node, TacInterface i) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.fully_qualified_class_name: {

			TacAttributes atts0 = this.fully_qualified_class_name(firstChild);
			String ImplementedInterfaceName = atts0.getEncapsListString();

			TacInterface existingInterface = this.userInterfaces.get(ImplementedInterfaceName);
			if (existingInterface != null) {
				i.addImplmentedInterface(ImplementedInterfaceName, existingInterface);
			} else {
				i.addImplmentedInterface(ImplementedInterfaceName, null);
			}
			break;

		}

		case PhpSymbols.interface_list: {

			this.interface_list(firstChild, i);
			TacAttributes atts0 = this.fully_qualified_class_name(node.getChild(2));

			String ImplementedInterfaceName = atts0.getEncapsListString();

			TacInterface existingInterface = this.userInterfaces.get(ImplementedInterfaceName);
			if (existingInterface != null) {
				i.addImplmentedInterface(ImplementedInterfaceName, existingInterface);
			} else {
				i.addImplmentedInterface(ImplementedInterfaceName, null);
			}

			break;

		}

		}
		return myAtts;
	}

	TacAttributes interface_list(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {

		case PhpSymbols.fully_qualified_class_name: {

			TacAttributes atts0 = this.fully_qualified_class_name(firstChild);
			String ImplementedInterfaceName = atts0.getEncapsListString();

			TacInterface existingInterface = this.userInterfaces.get(ImplementedInterfaceName);
			if (existingInterface != null) {
				c.addImplmentedInterface(ImplementedInterfaceName, existingInterface);
			} else {
				c.addImplmentedInterface(ImplementedInterfaceName, null);
			}
			break;
		}

		case PhpSymbols.interface_list: {

			this.interface_list(firstChild, c);
			TacAttributes atts0 = this.fully_qualified_class_name(node.getChild(2));

			String ImplementedInterfaceName = atts0.getEncapsListString();

			TacInterface existingInterface = this.userInterfaces.get(ImplementedInterfaceName);
			if (existingInterface != null) {
				c.addImplmentedInterface(ImplementedInterfaceName, existingInterface);
			} else {
				c.addImplmentedInterface(ImplementedInterfaceName, null);
			}

			break;
		}

		}
		return myAtts;
	}

	TacAttributes foreach_optional_arg(ParseNode node) {

		if (node.getChild(0).getSymbol() == PhpSymbols.T_EPSILON) {
			return null;
		} else {
			return (this.foreach_variable(node.getChild(1)));
		}
	}

	TacAttributes foreach_variable(ParseNode node) {

		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.variable: {
			TacAttributes attsCvar = this.variable(firstChild);
			myAtts.setCfg(attsCvar.getCfg());

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(attsCvar.getPlace(), false));
			myAtts.setActualParamList(paramList);

			break;

		}
		case PhpSymbols.T_BITWISE_AND: {

			TacAttributes attsCvar = this.variable(node.getChild(1));
			myAtts.setCfg(attsCvar.getCfg());

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(attsCvar.getPlace(), true));
			myAtts.setActualParamList(paramList);

			break;
		}

		}
		return myAtts;
	}

	TacAttributes for_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.statement: {
			TacAttributes atts0 = this.statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_COLON: {
			TacAttributes atts1 = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes foreach_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.statement) {
			TacAttributes attsStatement = this.statement(firstChild);
			myAtts.setCfg(attsStatement.getCfg());
		} else {
			TacAttributes attsList = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(attsList.getCfg());
		}

		return myAtts;
	}

	TacAttributes declare_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.statement: {
			TacAttributes atts0 = this.statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_COLON: {
			TacAttributes atts1 = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes switch_case_list(ParseNode node, AbstractTacPlace switchPlace, AbstractCfgNode nextTest,
			AbstractCfgNode nextStatement) {

		ParseNode listNode = null;
		switch (node.getChild(2).getSymbol()) {

		case PhpSymbols.T_CLOSE_CURLY_BRACES: {
			listNode = node.getChild(1);
			break;
		}

		case PhpSymbols.case_list: {
			if (node.getChild(0).getSymbol() == PhpSymbols.T_OPEN_CURLY_BRACES) {
				listNode = node.getChild(2);
			} else {
				listNode = node.getChild(2);
			}
			break;
		}
		case PhpSymbols.T_ENDSWITCH: {
			listNode = node.getChild(1);
			break;
		}

		}

		TacAttributes myAtts = this.case_list(listNode, switchPlace, nextTest, nextStatement);

		return myAtts;
	}

	TacAttributes case_list(ParseNode node, AbstractTacPlace switchPlace, AbstractCfgNode nextTest,
			AbstractCfgNode nextStatement) {

		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_EPSILON) {

			AbstractCfgNode emptyNode = new Empty();
			connect(emptyNode, nextTest);
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setDefaultNode(null);

		} else if (node.getChild(1).getSymbol() == PhpSymbols.T_CASE) {

			TacAttributes attsExpr = this.expr(node.getChild(2));
			TacAttributes attsStatement = this.inner_statement_list(node.getChild(4));
			TacAttributes attsCaseList = this.case_list(node.getChild(0), switchPlace, attsExpr.getCfg().getHead(),
					attsStatement.getCfg().getHead());

			Variable tempPlace = this.newTemp();
			AbstractCfgNode compareNode = new AssignBinary(tempPlace, switchPlace, attsExpr.getPlace(),
					TacOperators.IS_EQUAL, node.getChild(2));
			AbstractCfgNode ifNode = new If(tempPlace, Constant.TRUE, TacOperators.IS_EQUAL, node.getChild(2));

			connect(attsExpr.getCfg(), compareNode);
			connect(compareNode, ifNode);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(ifNode, nextTest, CfgEdge.FALSE_EDGE);
			connect(attsStatement.getCfg(), nextStatement);

			myAtts.setCfg(new ControlFlowGraph(attsCaseList.getCfg().getHead(), attsCaseList.getCfg().getHead()));

			myAtts.setDefaultNode(attsCaseList.getDefaultNode());

		} else {
			TacAttributes attsStatement = this.inner_statement_list(node.getChild(3));
			TacAttributes attsCaseList = this.case_list(node.getChild(0), switchPlace, nextTest,
					attsStatement.getCfg().getHead());

			connect(attsStatement.getCfg(), nextStatement);

			myAtts.setCfg(new ControlFlowGraph(attsCaseList.getCfg().getHead(), attsCaseList.getCfg().getHead()));

			myAtts.setDefaultNode(attsStatement.getCfg().getHead());

		}

		return myAtts;
	}

	TacAttributes while_statement(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.statement: {
			TacAttributes atts0 = this.statement(firstChild);
			myAtts.setCfg(atts0.getCfg());
			break;
		}

		case PhpSymbols.T_COLON: {
			TacAttributes atts1 = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		}

		return myAtts;
	}

	TacAttributes elseif_list(ParseNode node, AbstractCfgNode trueSucc, AbstractCfgNode falseSucc) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			connect(emptyNode, falseSucc);
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.elseif_list: {
			int logId = this.tempId;
			TacAttributes attsExpr = this.expr(node.getChild(3));
			this.resetId(logId);
			TacAttributes attsElif = this.elseif_list(firstChild, trueSucc, attsExpr.getCfg().getHead());
			TacAttributes attsStatement = this.statement(node.getChild(5));

			AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
					node.getChild(3));

			connect(attsExpr.getCfg(), ifNode);
			connect(ifNode, falseSucc, CfgEdge.FALSE_EDGE);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(attsStatement.getCfg(), trueSucc);

			myAtts.setCfg(new ControlFlowGraph(attsElif.getCfg().getHead(), attsStatement.getCfg().getTail(),
					attsStatement.getCfg().getTailEdgeType()));

			break;
		}
		}

		return myAtts;
	}

	TacAttributes new_elseif_list(ParseNode node, AbstractCfgNode trueSucc, AbstractCfgNode falseSucc) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			connect(emptyNode, falseSucc);
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.new_elseif_list: {
			int logId = this.tempId;
			TacAttributes attsExpr = this.expr(node.getChild(3));
			this.resetId(logId);
			TacAttributes attsElif = this.new_elseif_list(firstChild, trueSucc, attsExpr.getCfg().getHead());
			TacAttributes attsStatement = this.inner_statement_list(node.getChild(6));

			AbstractCfgNode ifNode = new If(attsExpr.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
					node.getChild(3));

			connect(attsExpr.getCfg(), ifNode);
			connect(ifNode, falseSucc, CfgEdge.FALSE_EDGE);
			connect(ifNode, attsStatement.getCfg(), CfgEdge.TRUE_EDGE);
			connect(attsStatement.getCfg(), trueSucc);

			myAtts.setCfg(new ControlFlowGraph(attsElif.getCfg().getHead(), attsStatement.getCfg().getTail(),
					attsStatement.getCfg().getTailEdgeType()));

			break;
		}
		}

		return myAtts;
	}

	TacAttributes else_single(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_ELSE: {
			TacAttributes attsStatement = this.statement(node.getChild(1));
			myAtts.setCfg(attsStatement.getCfg());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes new_else_single(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_ELSE: {
			TacAttributes attsStatement = this.inner_statement_list(node.getChild(2));
			myAtts.setCfg(attsStatement.getCfg());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes parameter_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.non_empty_parameter_list) {
			myAtts = this.non_empty_parameter_list(firstChild);
		} else {
			myAtts.setFormalParamList(new LinkedList<TacFormalParameter>());
		}

		return myAtts;
	}

	TacAttributes non_empty_parameter_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		ParseNode secondChild = node.getChild(1);
		switch (secondChild.getSymbol()) {

		case PhpSymbols.T_VARIABLE: {
			if (node.getNumChildren() == 2) {
				AbstractTacPlace paramPlace = this.makePlace(secondChild.getLexeme());
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable());
				this.optional_class_type(firstChild, param);
				List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);
			} else {
				TacAttributes atts2 = this.static_expr(node.getChild(3));
				Variable paramPlace = this.makePlace(secondChild.getLexeme());

				AbstractCfgNode cfgNode = new AssignSimple(paramPlace, atts2.getPlace(), node.getChild(3));
				connect(atts2.getCfg(), cfgNode);
				ControlFlowGraph defaultCfg = new ControlFlowGraph(atts2.getCfg().getHead(), cfgNode);
				this.optimize(defaultCfg);
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true, defaultCfg);
				this.optional_class_type(firstChild, param);
				List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);
			}

			break;
		}

		case PhpSymbols.T_BITWISE_AND: {
			if (node.getNumChildren() == 3) {
				AbstractTacPlace paramPlace = this.makePlace(node.getChild(2).getLexeme());
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true);
				this.optional_class_type(firstChild, param);
				List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);

			} else {
				TacAttributes atts2 = this.static_expr(node.getChild(4));
				Variable paramPlace = this.makePlace(node.getChild(2).getLexeme());

				AbstractCfgNode cfgNode = new AssignSimple(paramPlace, atts2.getPlace(), node.getChild(4));
				connect(atts2.getCfg(), cfgNode);
				ControlFlowGraph defaultCfg = new ControlFlowGraph(atts2.getCfg().getHead(), cfgNode);
				this.optimize(defaultCfg);
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true, defaultCfg);

				param.setIsReference(true);
				this.optional_class_type(firstChild, param);

				List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);
			}

			break;
		}

		case PhpSymbols.T_COMMA: {
			switch (node.getChild(3).getSymbol()) {

			case PhpSymbols.T_VARIABLE: {
				if (node.getNumChildren() == 4) {
					AbstractTacPlace paramPlace = this.makePlace(node.getChild(3).getLexeme());
					TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable());
					TacAttributes attsList = this.non_empty_parameter_list(firstChild);

					this.optional_class_type(node.getChild(2), param);

					List<TacFormalParameter> paramList = attsList.getFormalParamList();
					paramList.add(param);
					myAtts.setFormalParamList(paramList);
				} else {
					TacAttributes attsList = this.non_empty_parameter_list(firstChild);

					TacAttributes attsScalar = this.static_expr(node.getChild(5));
					Variable paramPlace = this.makePlace(node.getChild(3).getLexeme());

					AbstractCfgNode cfgNode = new AssignSimple(paramPlace, attsScalar.getPlace(), node.getChild(5));
					connect(attsScalar.getCfg(), cfgNode);
					ControlFlowGraph defaultCfg = new ControlFlowGraph(attsScalar.getCfg().getHead(), cfgNode);
					this.optimize(defaultCfg);
					TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true, defaultCfg);
					this.optional_class_type(node.getChild(2), param);

					List<TacFormalParameter> paramList = attsList.getFormalParamList();
					paramList.add(param);
					myAtts.setFormalParamList(paramList);
				}
				break;
			}
			case PhpSymbols.T_BITWISE_AND: {
				if (node.getNumChildren() == 5) {
					TacAttributes attsList = this.non_empty_parameter_list(firstChild);
					AbstractTacPlace paramPlace = this.makePlace(node.getChild(4).getLexeme());
					TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true);
					this.optional_class_type(node.getChild(2), param);
					List<TacFormalParameter> paramList = attsList.getFormalParamList();
					paramList.add(param);
					myAtts.setFormalParamList(paramList);
				} else {
					TacAttributes attsList = this.non_empty_parameter_list(firstChild);

					TacAttributes attsScalar = this.static_expr(node.getChild(6));
					Variable paramPlace = this.makePlace(node.getChild(4).getLexeme());

					AbstractCfgNode cfgNode = new AssignSimple(paramPlace, attsScalar.getPlace(), node.getChild(6));
					connect(attsScalar.getCfg(), cfgNode);
					ControlFlowGraph defaultCfg = new ControlFlowGraph(attsScalar.getCfg().getHead(), cfgNode);
					this.optimize(defaultCfg);
					TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true, defaultCfg);
					this.optional_class_type(node.getChild(2), param);

					List<TacFormalParameter> paramList = attsList.getFormalParamList();
					paramList.add(param);
					myAtts.setFormalParamList(paramList);
				}
				break;
			}

			}
			break;
		}
		}

		return myAtts;
	}

	TacAttributes optional_class_type(ParseNode node, TacFormalParameter Param) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.fully_qualified_class_name: {
			myAtts = this.fully_qualified_class_name(node.getChild(0));
			String CustomClass = myAtts.getEncapsListString();
			Param.getVariable().SetisCustomObject(true);
			Param.getVariable().SetCustomClass(CustomClass);
			break;
		}

		case PhpSymbols.T_ARRAY: {
			Param.getVariable().setIsArray(true);
			Param.getVariable().SetisCustomObject(false);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes function_call_parameter_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.non_empty_function_call_parameter_list) {

			TacAttributes attsList = this.non_empty_function_call_parameter_list(firstChild);
			myAtts.setCfg(attsList.getCfg());
			myAtts.setActualParamList(attsList.getActualParamList());

		} else {
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			List<TacActualParameter> ll = new LinkedList<TacActualParameter>();
			myAtts.setActualParamList(ll);
		}

		return myAtts;
	}

	TacAttributes non_empty_function_call_parameter_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.expr: {

			TacAttributes attsExpr = this.expr(firstChild);
			myAtts.setCfg(attsExpr.getCfg());

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(attsExpr.getPlace(), false));
			myAtts.setActualParamList(paramList);

			break;
		}

		case PhpSymbols.T_BITWISE_AND: {

			TacAttributes attsCvar = this.variable(node.getChild(1));
			myAtts.setCfg(attsCvar.getCfg());

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(attsCvar.getPlace(), true));
			myAtts.setActualParamList(paramList);

			break;
		}

		case PhpSymbols.non_empty_function_call_parameter_list: {
			ParseNode thirdChild = node.getChild(2);
			int thirdSymbol = thirdChild.getSymbol();
			if (thirdSymbol == PhpSymbols.expr) {

				TacAttributes attsList = this.non_empty_function_call_parameter_list(firstChild);
				TacAttributes attsExpr = this.expr(thirdChild);

				connect(attsList.getCfg(), attsExpr.getCfg());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), attsExpr.getCfg().getTail()));

				List<TacActualParameter> paramList = attsList.getActualParamList();
				paramList.add(new TacActualParameter(attsExpr.getPlace(), false));
				myAtts.setActualParamList(paramList);
			}

			else {
				TacAttributes attsList = this.non_empty_function_call_parameter_list(firstChild);
				TacAttributes attsCvar = this.variable(node.getChild(3));

				connect(attsList.getCfg(), attsCvar.getCfg());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), attsCvar.getCfg().getTail()));

				List<TacActualParameter> paramList = attsList.getActualParamList();
				paramList.add(new TacActualParameter(attsCvar.getPlace(), true));
				myAtts.setActualParamList(paramList);
			}

			break;
		}
		}

		return myAtts;
	}

	TacAttributes global_var_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);

		if (firstChild.getSymbol() == PhpSymbols.global_var_list) {
			TacAttributes atts0 = this.global_var_list(firstChild);
			TacAttributes atts2 = this.global_var(node.getChild(2));
			connect(atts0.getCfg(), atts2.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));
		} else {
			TacAttributes atts0 = this.global_var(firstChild);
			myAtts.setCfg(atts0.getCfg());
		}

		return myAtts;
	}

	TacAttributes global_var(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_VARIABLE) {
			String varLex = node.getChild(0).getLexeme();
			AbstractTacPlace varPlace = makePlace(varLex);
			makePlace(varLex, this.mainSymbolTable);
			AbstractCfgNode cfgNode = new Global(varPlace, node);
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
		}

		else if (node.getChild(1).getSymbol() == PhpSymbols.variable) {
			TacAttributes attsCvar = this.variable(node.getChild(1));
			AbstractTacPlace cvarPlace = attsCvar.getPlace();
			AbstractTacPlace varPlace = makePlace("${" + cvarPlace.toString() + "}");
			varPlace.getVariable().setDependsOn(cvarPlace);
			AbstractCfgNode cfgNode = new Global(varPlace, node);

			connect(attsCvar.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));
		} else {
			TacAttributes attsExpr = this.expr(node.getChild(2));
			AbstractTacPlace varPlace = this.exprVarHelper(attsExpr.getPlace());
			AbstractCfgNode cfgNode = new Global(varPlace, node);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
		}

		return myAtts;
	}

	TacAttributes static_var_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);

		if (firstChild.getSymbol() == PhpSymbols.static_var_list) {
			if (node.getNumChildren() == 3) {
				TacAttributes atts0 = this.static_var_list(firstChild);
				AbstractTacPlace varPlace = makePlace(node.getChild(2).getLexeme());
				AbstractCfgNode cfgNode = new Static(varPlace, node);
				connect(atts0.getCfg(), cfgNode);
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));

			} else {
				TacAttributes atts0 = this.static_var_list(firstChild);
				TacAttributes atts4 = this.static_expr(node.getChild(4));

				AbstractTacPlace varPlace = makePlace(node.getChild(2).getLexeme());
				AbstractCfgNode cfgNode = new Static(varPlace, atts4.getPlace(), node);

				connect(atts0.getCfg(), atts4.getCfg());
				connect(atts4.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));
			}
		} else {
			if (node.getNumChildren() == 1) {
				AbstractTacPlace varPlace = makePlace(firstChild.getLexeme());
				AbstractCfgNode cfgNode = new Static(varPlace, node);
				myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));

			} else {
				TacAttributes atts2 = this.static_expr(node.getChild(2));
				AbstractTacPlace varPlace = makePlace(firstChild.getLexeme());
				AbstractCfgNode cfgNode = new Static(varPlace, atts2.getPlace(), node);

				connect(atts2.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(atts2.getCfg().getHead(), cfgNode));
			}
		}

		return myAtts;
	}

	TacAttributes class_statement_list(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_statement_list: {
			int logId = this.tempId;
			this.class_statement_list(firstChild, c);
			this.class_statement(node.getChild(1), c);
			this.resetId(logId);
			break;
		}
		case PhpSymbols.T_EPSILON: {
			break;
		}
		}

		return myAtts;
	}

	TacAttributes class_statement_list(ParseNode node, TacInterface i) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_statement_list: {
			int logId = this.tempId;
			this.class_statement_list(firstChild, i);
			this.class_statement(node.getChild(1), i);
			this.resetId(logId);
			break;
		}

		case PhpSymbols.T_EPSILON: {
			break;
		}
		}

		return myAtts;
	}

	TacAttributes class_statement(ParseNode node, TacInterface i) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.variable_modifiers: {
			this.class_variable_decleration(node.getChild(1), i, node.getChild(0));
			break;
		}
		case PhpSymbols.method_modifiers: {

			String methodName = node.getChild(3).getLexeme().toLowerCase() + InternalStrings.methodSuffix;
			TacFunction method = this.methodHelper(node, 5, 7, methodName, true);
			this.method_modifiers(node.getChild(0), method);
			i.addMethod(methodName, method);

			TacFunction existingMethod = this.addMethod(methodName, this.interfaceStack.getLast().getName(), method);
			if (existingMethod != null) {
				System.out.println("\nWarning: Duplicate method definition: " + methodName);
				System.out.println("- found: " + existingMethod.getLoc());
				System.out.println("- using: " + method.getLoc());
			}

			break;
		}
		case PhpSymbols.class_constant_declaration: {

			this.class_constant_declaration(node.getChild(1), i);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes class_statement(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.variable_modifiers: {
			this.class_variable_decleration(node.getChild(1), c, node.getChild(0));
			break;
		}
		case PhpSymbols.method_modifiers: {
			String methodName = node.getChild(3).getLexeme().toLowerCase() + InternalStrings.methodSuffix;
			TacFunction method = this.methodHelper(node, 5, 7, methodName, false);
			this.method_modifiers(node.getChild(0), method);
			c.addMethod(methodName, method);

			TacFunction existingMethod = this.addMethod(methodName, this.classStack.getLast().getName(), method);
			if (existingMethod != null) {
				System.out.println("\nWarning: Duplicate method definition: " + methodName);
				System.out.println("- found: " + existingMethod.getLoc());
				System.out.println("- using: " + method.getLoc());
			}

			break;
		}

		case PhpSymbols.class_constant_declaration: {
			this.class_constant_declaration(node.getChild(0), c);
			break;

		}

		}

		return myAtts;
	}

	TacAttributes method_body(ParseNode node, TacFunction method) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_SEMICOLON: {
			break;
		}

		case PhpSymbols.T_OPEN_CURLY_BRACES: {
			TacAttributes atts0 = this.inner_statement_list(node.getChild(1));
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());

			break;
		}
		}
		return myAtts;
	}

	void variable_modifiers(ParseNode node, Variable var) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.non_empty_member_modifiers: {
			this.non_empty_member_modifiers(firstChild, var);
			break;

		}
		case PhpSymbols.T_VAR: {

			break;
		}
		}

	}

	void method_modifiers(ParseNode node, TacFunction method) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			break;
		}
		case PhpSymbols.non_empty_member_modifiers: {
			this.non_empty_member_modifiers(firstChild, method);

			break;

		}
		}

	}

	void non_empty_member_modifiers(ParseNode node, TacFunction method) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.member_modifier: {
			this.member_modifier(firstChild, method);
			break;
		}

		case PhpSymbols.non_empty_member_modifiers: {

			this.non_empty_member_modifiers(firstChild, method);
			this.member_modifier(node.getChild(1), method);

			break;

		}
		}

	}

	void non_empty_member_modifiers(ParseNode node, Variable Var) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.member_modifier: {
			this.member_modifier(firstChild, Var);
			break;
		}

		case PhpSymbols.non_empty_member_modifiers: {

			this.non_empty_member_modifiers(firstChild, Var);
			this.member_modifier(node.getChild(1), Var);

			break;

		}
		}

	}

	void member_modifier(ParseNode node, TacFunction method) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_PUBLIC: {

			method.setAccessModifier("public");
			break;
		}

		case PhpSymbols.T_PROTECTED: {
			method.setAccessModifier("protected");
			break;

		}
		case PhpSymbols.T_PRIVATE: {
			method.setAccessModifier("private");
			break;

		}
		case PhpSymbols.T_STATIC: {
			method.SetStatic(true);
			break;

		}
		case PhpSymbols.T_ABSTRACT: {
			method.SetAbstract(true);
			method.SetFinal(false);
			break;

		}
		case PhpSymbols.T_FINAL: {
			method.SetAbstract(false);
			method.SetFinal(true);
			break;

		}
		}

	}

	void member_modifier(ParseNode node, Variable Var) {

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_PUBLIC: {
			Var.setAccessModifier("public");
			break;
		}
		case PhpSymbols.T_PROTECTED: {
			Var.setAccessModifier("protected");
			break;
		}
		case PhpSymbols.T_PRIVATE: {
			Var.setAccessModifier("private");
			break;
		}
		case PhpSymbols.T_STATIC: {
			Var.SetStatic(true);
			break;
		}
		case PhpSymbols.T_ABSTRACT: {
			Var.SetAbstract(true);
			Var.SetFinal(false);
			break;
		}
		case PhpSymbols.T_FINAL: {
			Var.SetAbstract(false);
			Var.SetFinal(true);
			break;
		}
		}

	}

	TacAttributes class_variable_decleration(ParseNode node, TacClass c, ParseNode Modifiers) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_variable_declaration: {
			if (node.getNumChildren() == 3) {
				this.class_variable_decleration(firstChild, c, Modifiers);

				Variable var = this.newTemp();
				this.variable_modifiers(Modifiers, var);
				AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node);
				ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
				c.addMember(node.getChild(2).getLexeme(), nullCfg, var);
			} else {
				this.class_variable_decleration(firstChild, c, Modifiers);

				TacAttributes atts4 = this.static_expr(node.getChild(4));
				c.addMember(node.getChild(2).getLexeme(), atts4.getCfg(), atts4.getPlace());
			}
			break;
		}

		case PhpSymbols.T_VARIABLE: {
			if (node.getNumChildren() == 1) {
				Variable var = this.newTemp();
				this.variable_modifiers(Modifiers, var);
				AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node);
				ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
				c.addMember(firstChild.getLexeme(), nullCfg, var);
			} else {
				TacAttributes atts2 = this.static_expr(node.getChild(2));
				c.addMember(firstChild.getLexeme(), atts2.getCfg(), atts2.getPlace());
			}
			break;
		}
		}

		return myAtts;
	}

	TacAttributes class_variable_decleration(ParseNode node, TacInterface i, ParseNode Modifiers) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.class_variable_declaration: {
			if (node.getNumChildren() == 3) {
				this.class_variable_decleration(firstChild, i, Modifiers);
				Variable var = this.newTemp();
				this.variable_modifiers(Modifiers, var);
				AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node);
				ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
				i.addMember(node.getChild(2).getLexeme(), nullCfg, var);
			} else {
				this.class_variable_decleration(firstChild, i, Modifiers);
				TacAttributes atts4 = this.static_expr(node.getChild(4));
				i.addMember(node.getChild(2).getLexeme(), atts4.getCfg(), atts4.getPlace());
			}
			break;
		}

		case PhpSymbols.T_VARIABLE: {
			if (node.getNumChildren() == 1) {
				Variable var = this.newTemp();
				this.variable_modifiers(Modifiers, var);
				AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node);
				ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
				i.addMember(firstChild.getLexeme(), nullCfg, var);
			} else {
				TacAttributes atts2 = this.static_expr(node.getChild(2));
				i.addMember(firstChild.getLexeme(), atts2.getCfg(), atts2.getPlace());
			}
			break;
		}

		}

		return myAtts;
	}

	TacAttributes class_constant_declaration(ParseNode node, TacClass c) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_constant_declaration: {
			this.class_constant_declaration(firstChild, c);

			TacAttributes atts4 = this.static_expr(node.getChild(4));
			c.addMember(node.getChild(2).getLexeme(), atts4.getCfg(), atts4.getPlace());

			break;
		}

		case PhpSymbols.T_CONST: {
			TacAttributes atts2 = this.static_expr(node.getChild(3));
			c.addMember(node.getChild(1).getLexeme(), atts2.getCfg(), atts2.getPlace());
			break;
		}

		}

		return myAtts;
	}

	TacAttributes class_constant_declaration(ParseNode node, TacInterface c) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.class_constant_declaration: {
			this.class_constant_declaration(firstChild, c);

			TacAttributes atts4 = this.static_expr(node.getChild(4));
			c.addMember(node.getChild(2).getLexeme(), atts4.getCfg(), atts4.getPlace());

			break;
		}

		case PhpSymbols.T_CONST: {
			TacAttributes atts2 = this.static_expr(node.getChild(3));
			c.addMember(node.getChild(1).getLexeme(), atts2.getCfg(), atts2.getPlace());
			break;
		}

		}

		return myAtts;
	}

	TacAttributes echo_expr_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.echo_expr_list: {
			TacAttributes attsList = this.echo_expr_list(firstChild);
			TacAttributes attsExpr = this.expr(node.getChild(2));

			AbstractCfgNode cfgNode = new Echo(attsExpr.getPlace(), node);

			connect(attsList.getCfg(), attsExpr.getCfg());
			connect(attsExpr.getCfg(), cfgNode);

			myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));

			break;
		}

		case PhpSymbols.expr: {
			TacAttributes atts0 = this.expr(firstChild);
			AbstractCfgNode cfgNode = new Echo(atts0.getPlace(), node);
			connect(atts0.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes for_expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(Constant.TRUE);
			break;
		}
		case PhpSymbols.non_empty_for_expr: {
			TacAttributes atts0 = this.non_empty_for_expr(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes non_empty_for_expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.non_empty_for_expr: {
			TacAttributes atts0 = this.non_empty_for_expr(firstChild);
			TacAttributes atts2 = this.expr(node.getChild(2));

			connect(atts0.getCfg(), atts2.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail(),
					atts2.getCfg().getTailEdgeType()));

			myAtts.setPlace(atts2.getPlace());

			break;
		}

		case PhpSymbols.expr: {
			TacAttributes atts0 = this.expr(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes lexical_vars(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(Constant.TRUE);
			break;
		}

		case PhpSymbols.T_USE: {
			TacAttributes atts0 = this.lexical_var_list(node.getChild(2));
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;

		}
		}

		return myAtts;
	}

	TacAttributes lexical_var_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.lexical_var_list: {
			if (node.getNumChildren() == 3) {

				AbstractTacPlace paramPlace = this.makePlace(node.getChild(2).getLexeme());
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true);
				TacAttributes attsList = this.lexical_var_list(firstChild);
				List<TacFormalParameter> paramList = attsList.getFormalParamList();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);
			} else {
				TacAttributes attsList = this.lexical_var_list(firstChild);
				AbstractTacPlace paramPlace = this.makePlace(node.getChild(3).getLexeme());
				TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true);
				List<TacFormalParameter> paramList = attsList.getFormalParamList();
				paramList.add(param);
				myAtts.setFormalParamList(paramList);
			}

			break;
		}
		case PhpSymbols.T_VARIABLE: {
			AbstractTacPlace paramPlace = this.makePlace(firstChild.getLexeme());
			TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable());
			List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
			paramList.add(param);
			myAtts.setFormalParamList(paramList);
			break;

		}
		case PhpSymbols.T_BITWISE_AND: {

			AbstractTacPlace paramPlace = this.makePlace(node.getChild(1).getLexeme());
			TacFormalParameter param = new TacFormalParameter(paramPlace.getVariable(), true);
			List<TacFormalParameter> paramList = new LinkedList<TacFormalParameter>();
			paramList.add(param);
			myAtts.setFormalParamList(paramList);
			break;

		}
		}

		return myAtts;
	}

	TacAttributes function_call(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		AbstractTacPlace tempVar = newTemp();
		if (node.getChild(0).getSymbol() == PhpSymbols.namespace_name) {

			TacAttributes attsList = this.function_call_parameter_list(node.getChild(2));
			TacAttributes attsnamespace_name = this.namespace_name(node.getChild(0));
			String functionName = attsnamespace_name.getEncapsListString();

			ControlFlowGraph callCfg = this.functionCallHelper(functionName, false, null, attsList.getActualParamList(),
					tempVar, true, node, null, null);

			connect(attsList.getCfg(), callCfg.getHead());

			myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

			myAtts.setPlace(tempVar);

		} else if (node.getChild(0).getSymbol() == PhpSymbols.T_NAMESPACE) {

			TacAttributes atts1 = this.function_call_parameter_list(node.getChild(4));
			TacAttributes attsnamespace_name = this.namespace_name(node.getChild(2));
			String functionName = attsnamespace_name.getEncapsListString();

			ControlFlowGraph callCfg = this.functionCallHelper(functionName, false, null, atts1.getActualParamList(),
					tempVar, true, node, null, null);

			connect(atts1.getCfg(), callCfg.getHead());

			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), callCfg.getTail()));

			myAtts.setPlace(tempVar);

		}

		else if (node.getChild(0).getSymbol() == PhpSymbols.T_NS_SEPARATOR) {

			TacAttributes atts1 = this.function_call_parameter_list(node.getChild(4));

			TacAttributes attsnamespace_name = this.namespace_name(node.getChild(1));
			String functionName = attsnamespace_name.getEncapsListString();

			ControlFlowGraph callCfg = this.functionCallHelper(functionName, false, null, atts1.getActualParamList(),
					tempVar, true, node, null, null);

			connect(atts1.getCfg(), callCfg.getHead());

			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), callCfg.getTail()));

			myAtts.setPlace(tempVar);

		}

		else if (node.getChild(0).getSymbol() == PhpSymbols.class_name) {
			if (node.getChild(2).getSymbol() == PhpSymbols.T_STRING) {

				TacAttributes attsList = this.function_call_parameter_list(node.getChild(4));

				TacAttributes atts0 = this.class_name(node.getChild(0));
				String className = atts0.getEncapsListString();

				String methodName = null;
				try {
					ParseNode nameNode = node.getChild(2);
					if (nameNode.getSymbol() == PhpSymbols.T_STRING) {
						methodName = nameNode.getLexeme().toLowerCase() + InternalStrings.methodSuffix;
					}
				} catch (NullPointerException e) {
				}

				ControlFlowGraph callCfg;
				if (methodName == null) {
					CallUnknownFunction callUnknown = new CallUnknownFunction("<unknown>",
							attsList.getActualParamList(), tempVar, node, true);
					callCfg = new ControlFlowGraph(callUnknown, callUnknown);

				} else {
					callCfg = this.functionCallHelper(methodName, true, null, attsList.getActualParamList(), tempVar,
							true, node, className, null);
				}

				connect(attsList.getCfg(), callCfg.getHead());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

				myAtts.setPlace(tempVar);
			}

			else {

				TacAttributes attsList = this.function_call_parameter_list(node.getChild(4));

				String className = "";
				if (node.getChild(0).tokenContent() != "") {
					className = node.getChild(0).tokenContent().toLowerCase();
				}

				String methodName = null;
				try {
					ParseNode nameNode = node.getChild(2).getChild(0);
					if (nameNode.getSymbol() == PhpSymbols.variable_without_objects) {

						methodName = nameNode.getLexeme().toLowerCase() + InternalStrings.methodSuffix;
					}
				} catch (NullPointerException e) {
				}

				ControlFlowGraph callCfg;
				if (methodName == null) {
					CallUnknownFunction callUnknown = new CallUnknownFunction("<unknown>",
							attsList.getActualParamList(), tempVar, node, true);
					callCfg = new ControlFlowGraph(callUnknown, callUnknown);

				} else {
					callCfg = this.functionCallHelper(methodName, true, null, attsList.getActualParamList(), tempVar,
							true, node, className, null);
				}

				connect(attsList.getCfg(), callCfg.getHead());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

				myAtts.setPlace(tempVar);

			}

		}

		else if (node.getChild(0).getSymbol() == PhpSymbols.reference_variable) {

			if (node.getChild(2).getSymbol() == PhpSymbols.T_STRING) {

				TacAttributes attsList = this.function_call_parameter_list(node.getChild(4));
				String className = node.getChild(0).getLexeme().toLowerCase();
				String methodName = null;
				try {
					ParseNode nameNode = node.getChild(2).getChild(0);
					if (nameNode.getSymbol() == PhpSymbols.T_STRING) {
						methodName = nameNode.getLexeme().toLowerCase() + InternalStrings.methodSuffix;
					}
				} catch (NullPointerException e) {
				}

				ControlFlowGraph callCfg;
				if (methodName == null) {
					CallUnknownFunction callUnknown = new CallUnknownFunction("<unknown>",
							attsList.getActualParamList(), tempVar, node, true);
					callCfg = new ControlFlowGraph(callUnknown, callUnknown);

				} else {
					callCfg = this.functionCallHelper(methodName, true, null, attsList.getActualParamList(), tempVar,
							true, node, className, null);
				}

				connect(attsList.getCfg(), callCfg.getHead());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

				myAtts.setPlace(tempVar);
			}

			else {

				TacAttributes attsList = this.function_call_parameter_list(node.getChild(4));
				String className = node.getChild(0).getLexeme().toLowerCase();

				String methodName = null;
				try {
					ParseNode nameNode = node.getChild(2).getChild(0);
					if (nameNode.getSymbol() == PhpSymbols.variable_without_objects) {

						methodName = nameNode.getLexeme().toLowerCase() + InternalStrings.methodSuffix;
					}
				} catch (NullPointerException e) {
				}

				ControlFlowGraph callCfg;
				if (methodName == null) {
					CallUnknownFunction callUnknown = new CallUnknownFunction("<unknown>",
							attsList.getActualParamList(), tempVar, node, true);
					callCfg = new ControlFlowGraph(callUnknown, callUnknown);

				} else {
					callCfg = this.functionCallHelper(methodName, true, null, attsList.getActualParamList(), tempVar,
							true, node, className, null);
				}

				connect(attsList.getCfg(), callCfg.getHead());

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

				myAtts.setPlace(tempVar);

			}

		} else if (node.getChild(0).getSymbol() == PhpSymbols.variable_without_objects) {

			TacAttributes attsList = this.function_call_parameter_list(node.getChild(2));
			TacAttributes attsnamespace_name = this.variable_without_objects(node.getChild(0));
			String functionName = ((Variable) attsnamespace_name.getPlace()).getName();

			ControlFlowGraph callCfg = this.functionCallHelper(functionName, false, null, attsList.getActualParamList(),
					tempVar, true, node, null, null);

			connect(attsList.getCfg(), callCfg.getHead());

			myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

			myAtts.setPlace(tempVar);
		} else if (node.getChild(0).getSymbol() == PhpSymbols.object_dim_list) {
			if (node.getParent() == null || node.getParent().getChild(3) == null
					|| node.getParent().getChild(3).getChild(1) == null) {
				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				return myAtts;
			}
			TacAttributes attsList = this.function_call_parameter_list(node.getParent().getChild(3).getChild(1));
			String functionName = node.getChild(0).getChild(0).getChild(0).tokenContent();
			String functionFullName = "" + "->" + functionName;
			ControlFlowGraph callCfg = this.functionCallHelper(functionFullName, true, null,
					attsList.getActualParamList(), tempVar, true, node, "", null);

			connect(attsList.getCfg(), callCfg.getHead());

			myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), callCfg.getTail()));

			myAtts.setPlace(tempVar);
		}
		return myAtts;
	}

	TacAttributes class_name(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_STATIC) {

			myAtts.getEncapsList().add(new Literal("static"));
			myAtts = this.namespace_name(node.getChild(0));

		} else if (node.getChild(0).getSymbol() == PhpSymbols.namespace_name) {
			myAtts = this.namespace_name(node.getChild(0));

		} else if (node.getChild(0).getSymbol() == PhpSymbols.T_NAMESPACE) {
			myAtts = this.namespace_name(node.getChild(2));

		} else if (node.getChild(0).getSymbol() == PhpSymbols.T_NS_SEPARATOR) {
			myAtts = this.namespace_name(node.getChild(1));
		}
		return myAtts;
	}

	TacAttributes fully_qualified_class_name(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.namespace_name: {
			myAtts = this.namespace_name(node.getChild(0));
			break;
		}
		case PhpSymbols.T_NAMESPACE: {
			myAtts = this.namespace_name(node.getChild(2));
			break;
		}
		case PhpSymbols.T_NS_SEPARATOR: {
			myAtts = this.namespace_name(node.getChild(1));
			break;
		}
		}
		return myAtts;
	}

	TacAttributes class_name_reference(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.class_name: {
			myAtts = this.class_name(node.getChild(0));

			break;
		}
		case PhpSymbols.dynamic_class_name_reference: {
			myAtts = this.dynamic_class_name_reference(node.getChild(0));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes dynamic_class_name_reference(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.base_variable: {
			if (node.getNumChildren() > 1) {
				TacAttributes atts0 = this.base_variable(node.getChild(0));
				TacAttributes atts2 = this.object_property(node.getChild(2), atts0.getPlace().getVariable(), null,
						null);

				connect(atts0.getCfg(), atts2.getCfg());
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));
				myAtts.setPlace(atts2.getPlace());
				myAtts.setIsKnownCall(atts2.isKnownCall());
			} else {
				myAtts = this.base_variable(node.getChild(0));
				myAtts.setIsKnownCall(false);

			}
			break;
		}
		}
		return myAtts;
	}

	TacAttributes dynamic_class_name_variable_properties(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.dynamic_class_name_variable_properties: {
			TacAttributes atts0 = this.dynamic_class_name_variable_properties(node.getChild(0));
			TacAttributes atts2 = this.object_property(node.getChild(2), atts0.getPlace().getVariable(), null, null);

			connect(atts0.getCfg(), atts2.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));
			myAtts.setPlace(atts2.getPlace());
			myAtts.setIsKnownCall(atts2.isKnownCall());

			break;
		}
		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(Constant.TRUE);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes exit_expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_EPSILON) {
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));
		} else {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_CLOSE_BRACES) {
				AbstractCfgNode cfgNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));
			} else {
				TacAttributes attsExpr = this.expr(node.getChild(1));
				myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), attsExpr.getCfg().getTail(),
						CfgEdge.NO_EDGE));
			}
		}

		return myAtts;
	}

	TacAttributes backticks_expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_EPSILON) {
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode, CfgEdge.NO_EDGE));

		} else if (node.getChild(0).getSymbol() == PhpSymbols.T_STRING) {
			myAtts.setPlace(new Literal(node.getChild(0).getLexeme()));

		} else if (node.getChild(0).getSymbol() == PhpSymbols.T_ENCAPSED_AND_WHITESPACE) {
			myAtts.setPlace(new Literal(node.getChild(0).getLexeme()));
		} else if (node.getChild(0).getSymbol() == PhpSymbols.encaps_list) {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsList = this.encaps_list(node.getChild(0));

			EncapsList encapsList = attsList.getEncapsList();
			TacAttributes deepList = encapsList.makeAtts(newTemp(), node);

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(deepList.getPlace(), false));

			ControlFlowGraph execCallCfg = this.functionCallHelper("shell_exec", false, null, paramList, tempPlace,
					true, node, null, null);

			connect(deepList.getCfg(), execCallCfg.getHead());
			myAtts.setCfg(new ControlFlowGraph(deepList.getCfg().getHead(), execCallCfg.getTail()));

			myAtts.setPlace(tempPlace);
		}

		return myAtts;
	}

	TacAttributes ctor_arguments(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getChild(0).getSymbol() == PhpSymbols.T_EPSILON) {
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			List<TacActualParameter> ll = new LinkedList<TacActualParameter>();
			myAtts.setActualParamList(ll);
		} else {
			TacAttributes attsList = this.function_call_parameter_list(node.getChild(1));
			myAtts.setCfg(attsList.getCfg());
			myAtts.setActualParamList(attsList.getActualParamList());
		}

		return myAtts;
	}

	TacAttributes common_scalar(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_LNUMBER: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(new Literal(firstChild.getLexeme()));
			break;
		}

		case PhpSymbols.T_DNUMBER: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(new Literal(firstChild.getLexeme()));
			break;
		}

		case PhpSymbols.T_CONSTANT_ENCAPSED_STRING: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(new Literal(firstChild.tokenContent()));
			break;
		}

		case PhpSymbols.T_LINE: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.lineCPlace);
			break;
		}

		case PhpSymbols.T_FILE: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(new Literal(this.file.getPath()));
			break;
		}

		case PhpSymbols.T_CLASS_C: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.classCPlace);
			break;
		}

		case PhpSymbols.T_FUNC_C: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.functionCPlace);
			break;
		}

		case PhpSymbols.T_DIR: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_METHOD_C: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.methodCPlace);
			break;
		}
		case PhpSymbols.T_NS_C: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.namespaceCPlace);
			break;
		}

		case PhpSymbols.T_START_HEREDOC: {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_ENCAPSED_AND_WHITESPACE) {

				TacAttributes atts = new TacAttributes();
				atts.setPlace(new Literal(node.getChild(1).getLexeme()));
				AbstractCfgNode emptyNode = new Empty();
				atts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				myAtts.setCfg(atts.getCfg());
				myAtts.setPlace(atts.getPlace());
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_CONSTANT_ENCAPSED_STRING) {

				TacAttributes atts = new TacAttributes();

				atts.setPlace(new Literal(node.getChild(1).getLexeme()));

				AbstractCfgNode emptyNode = new Empty();
				atts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));

				myAtts.setCfg(atts.getCfg());
				myAtts.setPlace(atts.getPlace());
			} else {
				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			}

			break;
		}
		}

		return myAtts;
	}

	TacAttributes static_expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.common_scalar: {
			myAtts = this.common_scalar(firstChild);
			break;
		}
		case PhpSymbols.namespace_name: {
			TacAttributes atts = this.namespace_name(firstChild);
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			myAtts.setPlace(makeConstantPlace(atts.getEncapsListString()));
			break;
		}
		case PhpSymbols.T_NAMESPACE: {

			TacAttributes atts = this.namespace_name(node.getChild(2));
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			myAtts.setPlace(makeConstantPlace(atts.getEncapsListString()));
			break;

		}

		case PhpSymbols.T_NS_SEPARATOR: {

			TacAttributes atts = this.namespace_name(node.getChild(1));
			AbstractCfgNode cfgNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			myAtts.setPlace(makeConstantPlace(atts.getEncapsListString()));
			break;
		}

		case PhpSymbols.T_PLUS: {
			Variable tempPlace = this.newTemp();
			TacAttributes atts1 = this.static_expr(node.getChild(1));
			AbstractCfgNode cfgNode = new AssignUnary(tempPlace, atts1.getPlace(), TacOperators.PLUS, node);
			connect(atts1.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_MINUS: {
			Variable tempPlace = this.newTemp();
			TacAttributes atts1 = this.static_expr(node.getChild(1));
			AbstractCfgNode cfgNode = new AssignUnary(tempPlace, atts1.getPlace(), TacOperators.MINUS, node);
			connect(atts1.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_ARRAY: {
			Variable arrayPlace = this.newTemp();
			TacAttributes attsList = this.static_array_pair_list(node.getChild(2), arrayPlace);

			AbstractCfgNode cfgNode = new AssignArray(arrayPlace, node);
			connect(cfgNode, attsList.getCfg());

			myAtts.setCfg(new ControlFlowGraph(cfgNode, attsList.getCfg().getTail()));
			myAtts.setPlace(arrayPlace);

			break;
		}

		case PhpSymbols.static_class_constant: {
			Variable tempPlace = this.newTemp();
			TacAttributes atts1 = this.static_class_constant(node.getChild(0));
			AbstractCfgNode cfgNode = new Static(atts1.getPlace(), node.getChild(0));

			connect(atts1.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;

		}
		}

		return myAtts;
	}

	TacAttributes static_class_constant(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_name: {

			Variable var = this.newTemp();
			var.SetStatic(true);
			TacAttributes atts0 = this.class_name(firstChild);
			String ClassName = atts0.getEncapsListString();
			var.SetisCustomObject(true);
			var.SetCustomClass(ClassName);

			AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node.getChild(2));
			ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);

			myAtts.setCfg(nullCfg);

			break;
		}

		}

		return myAtts;
	}

	TacAttributes scalar(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.class_constant: {
			myAtts = this.class_constant(node.getChild(0));

			break;
		}

		case PhpSymbols.namespace_name: {
			myAtts = this.namespace_name(firstChild);
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));

			break;
		}

		case PhpSymbols.T_NAMESPACE: {
			myAtts = this.namespace_name(firstChild);
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}

		case PhpSymbols.T_NS_SEPARATOR: {
			myAtts = this.namespace_name(firstChild);
			break;
		}

		case PhpSymbols.common_scalar: {
			TacAttributes atts0 = this.common_scalar(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;
		}

		case PhpSymbols.T_DOUBLE_QUOTE: {
			if (node.getChild(1).getSymbol() == PhpSymbols.encaps_list) {
				TacAttributes attsList = this.encaps_list(node.getChild(1));
				EncapsList encapsList = attsList.getEncapsList();
				TacAttributes deepList = encapsList.makeAtts(newTemp(), node);
				myAtts.setCfg(deepList.getCfg());
				myAtts.setPlace(deepList.getPlace());
			}

			else {
				TacAttributes atts = new TacAttributes();

				atts.setPlace(new Literal(node.getChild(1).getLexeme()));

				AbstractCfgNode emptyNode = new Empty();
				atts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));

				myAtts.setCfg(atts.getCfg());
				myAtts.setPlace(atts.getPlace());
			}
			break;
		}

		case PhpSymbols.T_START_HEREDOC: {
			TacAttributes attsList = this.encaps_list(node.getChild(1));
			EncapsList encapsList = attsList.getEncapsList();
			TacAttributes deepList = encapsList.makeAtts(newTemp(), node);
			myAtts.setCfg(deepList.getCfg());
			myAtts.setPlace(deepList.getPlace());

			break;
		}

		}

		return myAtts;
	}

	TacAttributes static_array_pair_list(ParseNode node, AbstractTacPlace arrayPlace) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);

		if (firstChild.getSymbol() == PhpSymbols.T_EPSILON) {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
		} else {
			TacAttributes atts0 = this.non_empty_static_array_pair_list(firstChild, arrayPlace);
			myAtts.setCfg(atts0.getCfg());
		}

		return myAtts;
	}

	TacAttributes non_empty_static_array_pair_list(ParseNode node, AbstractTacPlace arrayPlace) {
		TacAttributes myAtts = new TacAttributes();

		int logId = this.tempId;
		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.non_empty_static_array_pair_list) {
			if (node.getNumChildren() == 5) {
				TacAttributes attsList = null;
				try {
					attsList = this.non_empty_static_array_pair_list(firstChild, arrayPlace);
				} catch (StackOverflowError e) {
					System.out.println(node.getName());
					Utils.bail();
				}
				TacAttributes attsScalar1 = this.static_expr(node.getChild(2));
				TacAttributes attsScalar2 = this.static_expr(node.getChild(4));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsScalar1.getPlace(),
						attsScalar2.getPlace(), false, node);

				connect(attsList.getCfg(), attsScalar1.getCfg());
				connect(attsScalar1.getCfg(), attsScalar2.getCfg());
				connect(attsScalar2.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));

			} else {

				TacAttributes attsList = this.non_empty_static_array_pair_list(firstChild, arrayPlace);
				TacAttributes attsScalar = this.static_expr(node.getChild(2));

				AbstractTacPlace offsetPlace;
				int largestIndex = attsList.getArrayIndex();
				if (largestIndex == -1) {
					offsetPlace = this.emptyOffsetPlace;
				} else {
					largestIndex++;
					offsetPlace = new Literal(String.valueOf(largestIndex));
					myAtts.setArrayIndex(largestIndex);
				}

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, offsetPlace, attsScalar.getPlace(),
						false, node);

				connect(attsList.getCfg(), attsScalar.getCfg());
				connect(attsScalar.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));
			}
		} else {
			if (node.getNumChildren() == 3) {

				TacAttributes attsScalar1 = this.static_expr(firstChild);
				TacAttributes attsScalar2 = this.static_expr(node.getChild(2));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsScalar1.getPlace(),
						attsScalar2.getPlace(), false, node);
				connect(attsScalar1.getCfg(), attsScalar2.getCfg());
				connect(attsScalar2.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsScalar1.getCfg().getHead(), cfgNode));

			} else {

				TacAttributes attsScalar = this.static_expr(firstChild);

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, new Literal("0"), attsScalar.getPlace(),
						false, node);

				connect(attsScalar.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsScalar.getCfg().getHead(), cfgNode));

				myAtts.setArrayIndex(0);
			}
		}

		this.resetId(logId);
		return myAtts;
	}

	TacAttributes internal_functions_in_yacc(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_ISSET: {
			TacAttributes atts2 = this.isset_variables(node.getChild(2));
			myAtts.setCfg(atts2.getCfg());
			myAtts.setPlace(atts2.getPlace());
			break;
		}

		case PhpSymbols.T_EMPTY: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsCvar = this.variable(node.getChild(2));
			AbstractCfgNode cfgNode = new EmptyTest(tempPlace, attsCvar.getPlace(), node);
			connect(attsCvar.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_INCLUDE: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsExpr = this.expr(node.getChild(1));
			Include cfgNode = new Include(tempPlace, attsExpr.getPlace(), this.file,
					(TacFunction) this.functionStack.getLast(), node);
			this.includeNodes.add(cfgNode);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_INCLUDE_ONCE: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsExpr = this.expr(node.getChild(1));
			Include cfgNode = new Include(tempPlace, attsExpr.getPlace(), this.file,
					(TacFunction) this.functionStack.getLast(), node);
			this.includeNodes.add(cfgNode);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_EVAL: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsExpr = this.expr(node.getChild(2));
			AbstractCfgNode cfgNode = new Eval(tempPlace, attsExpr.getPlace(), node);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_REQUIRE: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsExpr = this.expr(node.getChild(1));
			Include cfgNode = new Include(tempPlace, attsExpr.getPlace(), this.file,
					(TacFunction) this.functionStack.getLast(), node);
			this.includeNodes.add(cfgNode);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}

		case PhpSymbols.T_REQUIRE_ONCE: {
			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsExpr = this.expr(node.getChild(1));
			Include cfgNode = new Include(tempPlace, attsExpr.getPlace(), this.file,
					(TacFunction) this.functionStack.getLast(), node);
			this.includeNodes.add(cfgNode);
			connect(attsExpr.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes isset_variables(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		Variable tempPlace = this.newTemp();

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.variable) {
			TacAttributes attsCvar = this.variable(firstChild);

			AbstractCfgNode cfgNode = new Isset(tempPlace, attsCvar.getPlace(), node);

			connect(attsCvar.getCfg(), cfgNode);
			myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));
			myAtts.setPlace(tempPlace);

		} else {
			TacAttributes attsVariables = this.isset_variables(firstChild);
			TacAttributes attsCvar = this.variable(node.getChild(2));

			AbstractTacPlace tempPlaceIsset = this.newTemp();
			AbstractCfgNode cfgNodeIsset = new Isset(tempPlaceIsset, attsCvar.getPlace(), node);
			AbstractCfgNode cfgNode = new AssignBinary(tempPlace, attsVariables.getPlace(), tempPlaceIsset,
					TacOperators.BOOLEAN_AND, node);

			connect(attsVariables.getCfg(), attsCvar.getCfg());
			connect(attsCvar.getCfg(), cfgNodeIsset);
			connect(cfgNodeIsset, cfgNode);
			myAtts.setPlace(tempPlace);
			myAtts.setCfg(new ControlFlowGraph(attsVariables.getCfg().getHead(), cfgNode));
		}

		return myAtts;
	}

	TacAttributes class_constant(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		if (firstChild.getSymbol() == PhpSymbols.class_name) {
			Variable var = this.newTemp();
			TacAttributes atts0 = this.class_name(firstChild);
			String ClassName = atts0.getEncapsListString();
			var.SetisCustomObject(true);
			var.SetCustomClass(ClassName);
			AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node.getChild(2));
			ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
			myAtts.setPlace(var);
			myAtts.setCfg(nullCfg);
		} else {
			Variable var = this.newTemp();
			var.SetStatic(true);
			this.reference_variable(firstChild);
			AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node.getChild(2));
			ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
			myAtts.setPlace(var);
			myAtts.setCfg(nullCfg);
		}

		return myAtts;
	}

	TacAttributes encaps_var_offset(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_STRING: {
			myAtts.setPlace(new Literal(firstChild.getLexeme()));
			break;
		}

		case PhpSymbols.T_NUM_STRING: {
			myAtts.setPlace(new Literal(firstChild.getLexeme()));
			break;
		}

		case PhpSymbols.T_VARIABLE: {
			myAtts.setPlace(this.makePlace(firstChild.getLexeme()));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes encaps_var(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getNumChildren() == 1) {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(makePlace(node.getChild(0).getLexeme()));
			return myAtts;
		}

		switch (node.getChild(1).getSymbol()) {
		case PhpSymbols.T_OPEN_RECT_BRACES: {
			TacAttributes attsOffset = this.encaps_var_offset(node.getChild(2));
			AbstractTacPlace varPlace = this.makePlace(node.getChild(0).getLexeme());
			myAtts.setPlace(this.makeArrayElementPlace(varPlace, attsOffset.getPlace()));
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		case PhpSymbols.T_OBJECT_OPERATOR: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.memberPlace);
			break;
		}
		case PhpSymbols.expr: {
			TacAttributes attsExpr = this.expr(node.getChild(1));
			AbstractTacPlace varPlace = this.exprVarHelper(attsExpr.getPlace());
			myAtts.setPlace(varPlace);
			myAtts.setCfg(attsExpr.getCfg());
			break;
		}
		case PhpSymbols.T_STRING_VARNAME: {
			if (node.getChild(2).getSymbol() == PhpSymbols.T_OPEN_RECT_BRACES) {
				TacAttributes attsExpr = this.expr(node.getChild(3));
				AbstractTacPlace arrayPlace = this.makePlace("$" + node.getChild(1).getLexeme());
				AbstractTacPlace myPlace = this.makeArrayElementPlace(arrayPlace, attsExpr.getPlace());
				myAtts.setCfg(attsExpr.getCfg());
				myAtts.setPlace(myPlace);
			} else if (node.getChild(2).getSymbol() == PhpSymbols.T_CLOSE_CURLY_BRACES) {
				AbstractCfgNode cfgNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
				myAtts.setPlace(new Literal(node.getChild(0).getLexeme()));
			}
			break;
		}
		case PhpSymbols.variable: {
			TacAttributes attsCvar = this.variable(node.getChild(1));
			myAtts.setPlace(attsCvar.getPlace());
			myAtts.setCfg(attsCvar.getCfg());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes expr(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.variable: {
			if (node.getNumChildren() == 1) {
				myAtts = this.variable(firstChild);
			}

			else {
				if (node.getChild(1).getSymbol() == PhpSymbols.T_ASSIGN) {

					if (node.getChild(2).getSymbol() == PhpSymbols.expr) {
						TacAttributes atts0 = this.variable(firstChild);
						TacAttributes atts2 = this.expr(node.getChild(2));

						AbstractCfgNode cfgNode = new AssignSimple((Variable) atts0.getPlace(), atts2.getPlace(), node);
						connect(atts0.getCfg(), atts2.getCfg());
						connect(atts2.getCfg(), cfgNode);

						myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode));

						myAtts.setPlace(atts0.getPlace());

					} else {
						if (node.getChild(3).getSymbol() == PhpSymbols.variable) {
							TacAttributes atts0 = this.variable(firstChild);
							TacAttributes atts3 = this.variable(node.getChild(3));

							AbstractCfgNode cfgNode = new AssignReference((Variable) atts0.getPlace(),
									(Variable) atts3.getPlace(), node);
							connect(atts0.getCfg(), atts3.getCfg());
							connect(atts3.getCfg(), cfgNode);

							myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), cfgNode, CfgEdge.NORMAL_EDGE));
							myAtts.setPlace(atts0.getPlace());
							break;
						} else if (node.getChild(3).getSymbol() == PhpSymbols.T_NEW) {

							TacAttributes attsCvar = this.variable(firstChild);
							TacAttributes attsCtor = this.ctor_arguments(node.getChild(5));

							AbstractCfgNode cfgNode;

							ParseNode classNameNode = node.getChild(4).getChild(0);
							if (classNameNode.getSymbol() == PhpSymbols.T_STRING) {

								String className = classNameNode.getLexeme().toLowerCase();

								Variable tempPlace = newTemp();

								ControlFlowGraph callCfg = this.functionCallHelper(
										className + InternalStrings.methodSuffix, true, null,
										attsCtor.getActualParamList(), tempPlace, true, node, className, null);

								cfgNode = new AssignReference((Variable) attsCvar.getPlace(), tempPlace, node);

								connect(attsCvar.getCfg(), attsCtor.getCfg());
								connect(attsCtor.getCfg(), callCfg.getHead());
								connect(callCfg.getTail(), cfgNode);

								myAtts.setCfg(new ControlFlowGraph(attsCtor.getCfg().getHead(), callCfg.getTail()));

							} else {
								cfgNode = new AssignReference((Variable) attsCvar.getPlace(), this.objectPlace, node);
								connect(attsCvar.getCfg(), attsCtor.getCfg());
								connect(attsCtor.getCfg(), cfgNode);
							}

							myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));
							myAtts.setPlace(attsCvar.getPlace());

							break;

						}
					}
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_PLUS_EQUAL) {
					this.cvarOpExp(node, TacOperators.PLUS, myAtts);
				}

				else if (node.getChild(1).getSymbol() == PhpSymbols.T_MINUS_EQUAL) {
					this.cvarOpExp(node, TacOperators.MINUS, myAtts);

				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_MUL_EQUAL) {
					this.cvarOpExp(node, TacOperators.MULT, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_DIV_EQUAL) {
					this.cvarOpExp(node, TacOperators.DIV, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_CONCAT_EQUAL) {
					this.cvarOpExp(node, TacOperators.CONCAT, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_MOD_EQUAL) {
					this.cvarOpExp(node, TacOperators.MODULO, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_AND_EQUAL) {
					this.cvarOpExp(node, TacOperators.BITWISE_AND, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_OR_EQUAL) {
					this.cvarOpExp(node, TacOperators.BITWISE_OR, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_XOR_EQUAL) {
					this.cvarOpExp(node, TacOperators.BITWISE_XOR, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_SL_EQUAL) {
					this.cvarOpExp(node, TacOperators.SL, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_SR_EQUAL) {
					this.cvarOpExp(node, TacOperators.SR, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_INC) {
					postIncDec(node, TacOperators.PLUS, myAtts);
				} else if (node.getChild(1).getSymbol() == PhpSymbols.T_DEC) {
					postIncDec(node, TacOperators.MINUS, myAtts);
				}
			}
			break;
		}

		case PhpSymbols.T_LIST: {

			TacAttributes attsExpr = this.expr(node.getChild(5));
			TacAttributes attsList = this.assignment_list(node.getChild(2), attsExpr.getPlace(), 0);

			connect(attsExpr.getCfg(), attsList.getCfg());
			myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), attsList.getCfg().getTail()));

			myAtts.setPlace(attsExpr.getPlace());
			break;

		}
		case PhpSymbols.T_NEW: {
			TacAttributes attsCtor = this.ctor_arguments(node.getChild(2));

			ParseNode classNameNode = node.getChild(1).getChild(0);
			if (classNameNode.getSymbol() == PhpSymbols.T_STRING) {

				String className = classNameNode.getLexeme().toLowerCase();

				AbstractTacPlace tempPlace = newTemp();

				ControlFlowGraph callCfg = this.functionCallHelper(className + InternalStrings.methodSuffix, true, null,
						attsCtor.getActualParamList(), tempPlace, true, node, className, null);

				connect(attsCtor.getCfg(), callCfg.getHead());

				myAtts.setCfg(new ControlFlowGraph(attsCtor.getCfg().getHead(), callCfg.getTail()));

				myAtts.setPlace(tempPlace);
			} else {
				myAtts.setCfg(attsCtor.getCfg());
				myAtts.setPlace(this.objectPlace);
			}

			break;

		}
		case PhpSymbols.T_CLONE: {
			Clone(node.getParent().getChild(0), node, myAtts);
			break;

		}
		case PhpSymbols.T_INC: {
			preIncDec(node, TacOperators.PLUS, myAtts);
			break;

		}
		case PhpSymbols.T_DEC: {
			preIncDec(node, TacOperators.MINUS, myAtts);
			break;
		}

		case PhpSymbols.expr: {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_BOOLEAN_OR) {
				this.booleanHelper(node, myAtts, PhpSymbols.T_BOOLEAN_OR);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_BOOLEAN_AND) {
				this.booleanHelper(node, myAtts, PhpSymbols.T_BOOLEAN_AND);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_LOGICAL_OR) {
				this.booleanHelper(node, myAtts, PhpSymbols.T_LOGICAL_OR);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_LOGICAL_AND) {
				this.booleanHelper(node, myAtts, PhpSymbols.T_LOGICAL_AND);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_LOGICAL_XOR) {
				Variable tempPlace = newTemp();
				TacAttributes atts0 = this.expr(node.getChild(0));
				TacAttributes atts2 = this.expr(node.getChild(2));

				AbstractCfgNode trueNode = new AssignSimple(tempPlace, Constant.TRUE, node);
				AbstractCfgNode falseNode = new AssignSimple(tempPlace, Constant.FALSE, node);

				AbstractCfgNode emptyNode = new Empty();
				connect(trueNode, emptyNode);
				connect(falseNode, emptyNode);

				AbstractCfgNode ifNode0 = new If(atts0.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
						node.getChild(0));

				connect(atts0.getCfg(), atts2.getCfg());
				connect(atts2.getCfg(), ifNode0);

				AbstractCfgNode ifNode2WasTrue = new If(atts2.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
						node.getChild(2));
				AbstractCfgNode ifNode2WasFalse = new If(atts2.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
						node.getChild(2));

				connect(ifNode0, ifNode2WasTrue, CfgEdge.TRUE_EDGE);
				connect(ifNode0, ifNode2WasFalse, CfgEdge.FALSE_EDGE);

				connect(ifNode2WasTrue, trueNode, CfgEdge.FALSE_EDGE);
				connect(ifNode2WasTrue, falseNode, CfgEdge.TRUE_EDGE);
				connect(ifNode2WasFalse, trueNode, CfgEdge.TRUE_EDGE);
				connect(ifNode2WasFalse, falseNode, CfgEdge.FALSE_EDGE);

				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), emptyNode));
				myAtts.setPlace(tempPlace);

				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_BITWISE_OR) {

				this.expOpExp(node, TacOperators.BITWISE_OR, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_BITWISE_AND) {

				this.expOpExp(node, TacOperators.BITWISE_AND, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_BITWISE_XOR) {

				this.expOpExp(node, TacOperators.BITWISE_XOR, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_POINT) {

				this.expOpExp(node, TacOperators.CONCAT, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_PLUS) {

				this.expOpExp(node, TacOperators.PLUS, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_MINUS) {

				this.expOpExp(node, TacOperators.MINUS, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_MULT) {

				this.expOpExp(node, TacOperators.MULT, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_DIV) {

				this.expOpExp(node, TacOperators.DIV, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_MODULO) {

				this.expOpExp(node, TacOperators.MODULO, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_SL) {

				this.expOpExp(node, TacOperators.SL, myAtts);
				break;
			}

			else if (node.getChild(1).getSymbol() == PhpSymbols.T_SR) {

				this.expOpExp(node, TacOperators.SR, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_IDENTICAL) {

				this.expOpExp(node, TacOperators.IS_IDENTICAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_NOT_IDENTICAL) {
				this.expOpExp(node, TacOperators.IS_NOT_IDENTICAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_EQUAL) {
				this.expOpExp(node, TacOperators.IS_EQUAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_NOT_EQUAL) {
				this.expOpExp(node, TacOperators.IS_NOT_EQUAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_SMALLER) {
				this.expOpExp(node, TacOperators.IS_SMALLER, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_SMALLER_OR_EQUAL) {
				this.expOpExp(node, TacOperators.IS_SMALLER_OR_EQUAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_GREATER) {
				this.expOpExp(node, TacOperators.IS_GREATER, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_IS_GREATER_OR_EQUAL) {
				this.expOpExp(node, TacOperators.IS_GREATER_OR_EQUAL, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_INSTANCEOF) {

				this.expOpExp(node, TacOperators.INSTANCE_OF, myAtts);
				break;
			} else if (node.getChild(1).getSymbol() == PhpSymbols.T_QUESTION) {

				if (node.getChild(2).getSymbol() == PhpSymbols.expr) {
					Variable tempPlace = newTemp();
					TacAttributes attsExprTest = this.expr(node.getChild(0));
					TacAttributes attsExprThen = this.expr(node.getChild(2));
					TacAttributes attsExprElse = this.expr(node.getChild(4));

					AbstractCfgNode assignThen = new AssignSimple(tempPlace, attsExprThen.getPlace(), node.getChild(2));
					AbstractCfgNode assignElse = new AssignSimple(tempPlace, attsExprElse.getPlace(), node.getChild(4));

					AbstractCfgNode endIfNode = new Empty();

					AbstractCfgNode ifNode = new If(attsExprTest.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
							node.getChild(0));

					connect(attsExprTest.getCfg(), ifNode);

					connect(ifNode, attsExprElse.getCfg(), CfgEdge.FALSE_EDGE);
					connect(ifNode, attsExprThen.getCfg(), CfgEdge.TRUE_EDGE);

					connect(attsExprThen.getCfg(), assignThen);
					connect(attsExprElse.getCfg(), assignElse);

					connect(assignThen, endIfNode);
					connect(assignElse, endIfNode);

					myAtts.setCfg(new ControlFlowGraph(attsExprTest.getCfg().getHead(), endIfNode));
					myAtts.setPlace(tempPlace);

					break;
				} else {
					Variable tempPlace = newTemp();
					TacAttributes attsExprTest = this.expr(node.getChild(0));
					TacAttributes attsExprElse = this.expr(node.getChild(3));

					AbstractCfgNode assignThen = new AssignSimple(tempPlace, attsExprTest.getPlace(), node.getChild(2));
					AbstractCfgNode assignElse = new AssignSimple(tempPlace, attsExprElse.getPlace(), node.getChild(4));

					AbstractCfgNode endIfNode = new Empty();

					AbstractCfgNode ifNode = new If(attsExprTest.getPlace(), Constant.TRUE, TacOperators.IS_EQUAL,
							node.getChild(0));

					connect(attsExprTest.getCfg(), ifNode);

					connect(ifNode, attsExprElse.getCfg(), CfgEdge.FALSE_EDGE);
					connect(ifNode, attsExprTest.getCfg(), CfgEdge.TRUE_EDGE);

					connect(attsExprTest.getCfg(), assignThen);
					connect(attsExprElse.getCfg(), assignElse);

					connect(assignThen, endIfNode);
					connect(assignElse, endIfNode);

					myAtts.setCfg(new ControlFlowGraph(attsExprTest.getCfg().getHead(), endIfNode));
					myAtts.setPlace(tempPlace);

					break;
				}
			}

			break;
		}
		case PhpSymbols.T_PLUS: {
			this.opExp(node, TacOperators.PLUS, myAtts);
			break;
		}
		case PhpSymbols.T_MINUS: {
			this.opExp(node, TacOperators.MINUS, myAtts);
			break;
		}
		case PhpSymbols.T_NOT: {
			this.opExp(node, TacOperators.NOT, myAtts);
			break;
		}
		case PhpSymbols.T_BITWISE_NOT: {
			boolean special = false;
			String marker = null;
			if (this.specialNodes) {
				try {
					ParseNode constantNode = node.getChild(1).getChild(0).getChild(0).getChild(0);
					if (constantNode.getSymbol() == PhpSymbols.T_STRING && constantNode.getLexeme().startsWith("_")) {
						special = true;
						marker = constantNode.getLexeme();
					}
				} catch (Exception e) {
				}
			}

			if (!special) {
				this.opExp(node, TacOperators.BITWISE_NOT, myAtts);
			} else {
				AbstractCfgNode cfgNode = SpecialNodes.get(marker, (TacFunction) this.functionStack.getLast(), this);
				myAtts.setCfg(new ControlFlowGraph(cfgNode, cfgNode));
			}

			break;

		}

		case PhpSymbols.T_OPEN_BRACES: {
			TacAttributes atts1 = this.expr(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			myAtts.setPlace(atts1.getPlace());

			break;

		}

		case PhpSymbols.internal_functions_in_yacc: {
			TacAttributes atts0 = this.internal_functions_in_yacc(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;

		}

		case PhpSymbols.T_INT_CAST: {
			this.opExp(node, TacOperators.INT_CAST, myAtts);
			break;

		}

		case PhpSymbols.T_DOUBLE_CAST: {
			this.opExp(node, TacOperators.DOUBLE_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_STRING_CAST: {
			this.opExp(node, TacOperators.STRING_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_ARRAY_CAST: {
			this.opExp(node, TacOperators.ARRAY_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_BOOL_CAST: {
			this.opExp(node, TacOperators.BOOL_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_OBJECT_CAST: {
			this.opExp(node, TacOperators.OBJECT_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_UNSET_CAST: {
			this.opExp(node, TacOperators.UNSET_CAST, myAtts);
			break;
		}

		case PhpSymbols.T_EXIT: {
			TacAttributes atts1 = this.exit_expr(node.getChild(1));
			myAtts.setPlace(this.voidPlace);
			myAtts.setCfg(atts1.getCfg());
			break;

		}
		case PhpSymbols.T_AT: {
			TacAttributes atts1 = this.expr(node.getChild(1));
			myAtts.setPlace(atts1.getPlace());
			myAtts.setCfg(atts1.getCfg());
			break;
		}

		case PhpSymbols.scalar: {
			TacAttributes atts0 = this.scalar(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;
		}

		case PhpSymbols.T_ARRAY: {
			Variable arrayPlace = newTemp();
			TacAttributes attsList = this.array_pair_list(node.getChild(2), arrayPlace);

			AbstractCfgNode cfgNode = new AssignArray(arrayPlace, node);
			connect(cfgNode, attsList.getCfg());

			myAtts.setCfg(new ControlFlowGraph(cfgNode, attsList.getCfg().getTail()));
			myAtts.setPlace(arrayPlace);

			break;
		}

		case PhpSymbols.T_BACKTICK: {

			AbstractTacPlace tempPlace = this.newTemp();
			TacAttributes attsList = this.encaps_list(node.getChild(1));

			EncapsList encapsList = attsList.getEncapsList();
			TacAttributes deepList = encapsList.makeAtts(newTemp(), node);

			List<TacActualParameter> paramList = new LinkedList<TacActualParameter>();
			paramList.add(new TacActualParameter(deepList.getPlace(), false));

			ControlFlowGraph execCallCfg = this.functionCallHelper("shell_exec", false, null, paramList, tempPlace,
					true, node, null, null);

			connect(deepList.getCfg(), execCallCfg.getHead());
			myAtts.setCfg(new ControlFlowGraph(deepList.getCfg().getHead(), execCallCfg.getTail()));

			myAtts.setPlace(tempPlace);

			break;
		}

		case PhpSymbols.T_PRINT: {
			TacAttributes atts1 = this.expr(node.getChild(1));
			AbstractCfgNode cfgNode = new Echo(atts1.getPlace(), node);
			connect(atts1.getCfg(), cfgNode);

			myAtts.setPlace(new Literal("1"));
			myAtts.setCfg(new ControlFlowGraph(atts1.getCfg().getHead(), cfgNode));
			break;
		}

		case PhpSymbols.T_FUNCTION: {
			this.functionHelper(node, 3, 7, myAtts);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes variable(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.base_variable_with_function_calls:

		{

			if (node.getNumChildren() == 1) {
				myAtts = this.base_variable_with_function_calls(node.getChild(0));
				myAtts.setIsKnownCall(false);
			} else {
				TacAttributes atts0 = this.base_variable_with_function_calls(node.getChild(0));
				List<TacActualParameter> paramlist = null;
				this.newTemp();
				if (node.getChild(3).children().size() >= 2) {
					if (node.getChild(3).getChild(1).getSymbol() == PhpSymbols.function_call_parameter_list) {
						if (node.getChild(3).getChild(1).children().size() >= 1) {
							if (node.getChild(3).getChild(1).getChild(0)
									.getSymbol() == PhpSymbols.non_empty_function_call_parameter_list) {
								TacAttributes attsList = this.non_empty_function_call_parameter_list(
										node.getChild(3).getChild(1).getChild(0));
								paramlist = attsList.getActualParamList();
							}
						}

					}
				}
				TacAttributes atts2 = this.object_property(node.getChild(2), atts0.getPlace().getVariable(), paramlist,
						atts0.getPlace().getVariable());
				TacAttributes atts3 = this.method_or_not(node.getChild(2), node.getChild(3));
				TacAttributes atts4 = this.variable_properties(node.getChild(4), atts0.getPlace().getVariable(), null,
						null);

				connect(atts0.getCfg(), atts2.getCfg());
				connect(atts2.getCfg(), atts3.getCfg());
				connect(atts3.getCfg(), atts4.getCfg());
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts4.getCfg().getTail()));
				myAtts.setPlace(atts2.getPlace());
				myAtts.setIsKnownCall(atts2.isKnownCall());
			}

			break;

		}

		}
		return myAtts;
	}

	TacAttributes variable_properties(ParseNode node, Variable leftPlace, List<TacActualParameter> paramList,
			Variable catchVar) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.variable_properties: {
			TacAttributes atts0 = this.variable_properties(firstChild, leftPlace, paramList, catchVar);
			TacAttributes atts1 = this.variable_property(node.getChild(1), leftPlace, paramList, catchVar);

			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));

			break;

		}
		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.emptyOffsetPlace);
			break;

		}

		}
		return myAtts;
	}

	TacAttributes variable_property(ParseNode node, Variable leftPlace, List<TacActualParameter> paramList,
			Variable catchVar) {

		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_OBJECT_OPERATOR: {
			leftPlace = this.memberPlace;

			TacAttributes atts0 = this.object_property(node.getChild(1), leftPlace, paramList, catchVar);
			TacAttributes atts1 = this.method_or_not(node.getChild(1), node.getChild(2));

			connect(atts0.getCfg(), atts1.getCfg());
			myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts1.getCfg().getTail(),
					atts1.getCfg().getTailEdgeType()));

			break;
		}
		}
		return myAtts;
	}

	TacAttributes method_or_not(ParseNode FunctionNode, ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);
		TacAttributes atts0 = null;
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_OPEN_BRACES: {
			if (FunctionNode.getSymbol() == PhpSymbols.object_property) {
				if (FunctionNode.children().size() >= 1) {
					if (FunctionNode.getChild(0).getSymbol() == PhpSymbols.object_dim_list) {
						if (FunctionNode.getChild(0).children().size() >= 1) {
							if (FunctionNode.getChild(0).getChild(0).getSymbol() == PhpSymbols.variable_name) {
								if (FunctionNode.getChild(0).getChild(0).children().size() >= 1) {
									if (FunctionNode.getChild(0).getChild(0).getChild(0)
											.getSymbol() == PhpSymbols.T_STRING) {
										atts0 = this.function_call(FunctionNode);
									}
								}
							}
						}
					}
				}
			}
			TacAttributes attsList = this.function_call_parameter_list(node.getChild(1));
			if (atts0 != null) {
				connect(atts0.getCfg(), attsList.getCfg());
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), attsList.getCfg().getTail()));
			} else {
				myAtts.setCfg(attsList.getCfg());
				myAtts.setActualParamList(attsList.getActualParamList());
			}
			break;
		}
		case PhpSymbols.T_EPSILON:

		{
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.emptyOffsetPlace);
			break;
		}
		}
		return myAtts;
	}

	TacAttributes variable_without_objects(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.reference_variable: {
			TacAttributes attsVar = this.reference_variable(firstChild);
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsVar.getPlace());
			break;
		}
		case PhpSymbols.simple_indirect_reference: {
			TacAttributes attsVar = this.reference_variable(node.getChild(1));
			TacAttributes attsRef = this.simple_indirect_reference(firstChild, attsVar.getPlace());
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsRef.getPlace());
			break;
		}

		}
		return myAtts;
	}

	TacAttributes static_member(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.class_name: {
			Variable var = this.newTemp();
			TacAttributes attsVar = this.variable_without_objects(node.getChild(2));
			var = attsVar.getPlace().getVariable();
			var.SetStatic(true);
			TacAttributes atts0 = this.class_name(firstChild);
			String ClassName = atts0.getEncapsListString();
			var.SetisCustomObject(true);
			var.SetCustomClass(ClassName);
			AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node.getChild(2));
			ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
			myAtts.setCfg(nullCfg);
			myAtts.setPlace(var);
			break;

		}
		case PhpSymbols.reference_variable: {
			Variable var = this.newTemp();
			TacAttributes attsVar = this.variable_without_objects(node.getChild(2));
			var = attsVar.getPlace().getVariable();
			var.SetStatic(true);
			this.reference_variable(firstChild);
			AbstractCfgNode cfgNode = new AssignSimple(var, this.constantsTable.getConstant("NULL"), node.getChild(2));
			ControlFlowGraph nullCfg = new ControlFlowGraph(cfgNode, cfgNode);
			myAtts.setCfg(nullCfg);
			myAtts.setPlace(var);
			break;
		}
		}
		return myAtts;
	}

	TacAttributes base_variable_with_function_calls(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.base_variable: {
			TacAttributes attsVar = this.base_variable(firstChild);
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsVar.getPlace());
			break;
		}
		case PhpSymbols.function_call: {
			TacAttributes atts0 = this.function_call(firstChild);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			break;
		}
		}
		return myAtts;
	}

	TacAttributes base_variable(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		switch (firstChild.getSymbol()) {
		case PhpSymbols.reference_variable:

		{
			TacAttributes attsVar = this.reference_variable(firstChild);
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsVar.getPlace());
			break;

		}
		case PhpSymbols.simple_indirect_reference:

		{
			TacAttributes attsVar = this.reference_variable(node.getChild(1));
			TacAttributes attsRef = this.simple_indirect_reference(firstChild, attsVar.getPlace());
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsRef.getPlace());
			break;
		}
		case PhpSymbols.static_member: {
			TacAttributes attsVar = this.static_member(firstChild);
			myAtts.setCfg(attsVar.getCfg());
			myAtts.setPlace(attsVar.getPlace());
			break;
		}
		}
		return myAtts;
	}

	TacAttributes reference_variable(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.reference_variable: {
			if (node.getChild(1).getSymbol() == PhpSymbols.T_OPEN_RECT_BRACES) {

				TacAttributes atts0 = this.reference_variable(firstChild);
				TacAttributes atts2 = this.dim_offset(node.getChild(2));
				myAtts.setPlace(this.makeArrayElementPlace(atts0.getPlace(), atts2.getPlace()));
				connect(atts0.getCfg(), atts2.getCfg());
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));
			} else {

				TacAttributes atts0 = this.reference_variable(firstChild);
				TacAttributes atts2 = this.expr(node.getChild(2));
				myAtts.setPlace(this.makeArrayElementPlace(atts0.getPlace(), atts2.getPlace()));
				connect(atts0.getCfg(), atts2.getCfg());
				myAtts.setCfg(new ControlFlowGraph(atts0.getCfg().getHead(), atts2.getCfg().getTail()));

			}
			break;
		}

		case PhpSymbols.compound_variable: {
			myAtts = this.compound_variable(firstChild);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes dim_offset(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_EPSILON: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.emptyOffsetPlace);
			break;
		}

		case PhpSymbols.expr: {
			TacAttributes attsExpr = this.expr(firstChild);
			myAtts.setCfg(attsExpr.getCfg());
			myAtts.setPlace(attsExpr.getPlace());
			break;
		}
		}

		return myAtts;
	}

	TacAttributes compound_variable(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_VARIABLE: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(makePlace(firstChild.getLexeme()));

			break;
		}

		case PhpSymbols.T_DOLLAR: {
			TacAttributes attsExpr = this.expr(node.getChild(2));
			AbstractTacPlace myPlace = this.exprVarHelper(attsExpr.getPlace());
			myAtts.setCfg(attsExpr.getCfg());
			myAtts.setPlace(myPlace);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes object_property(ParseNode node, Variable leftPlace, List<TacActualParameter> paramList,
			Variable catchVar) {

		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.object_dim_list: {
			TacAttributes atts0 = this.object_dim_list(firstChild, leftPlace, paramList, catchVar);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			myAtts.setIsKnownCall(atts0.isKnownCall());
			break;
		}

		case PhpSymbols.variable_without_objects: {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.memberPlace);
			myAtts.setIsKnownCall(false);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes object_dim_list(ParseNode node, Variable leftPlace, List<TacActualParameter> paramList,
			Variable catchVar) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.object_dim_list: {
			if (paramList != null) {
				System.out.println(node.getName());
				throw new RuntimeException("not yet");
			}
			if (node.getChild(1).getSymbol() == PhpSymbols.T_OPEN_RECT_BRACES) {
			} else {
			}
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			myAtts.setPlace(this.memberPlace);
			myAtts.setIsKnownCall(false);
			break;
		}
		case PhpSymbols.variable_name: {
			TacAttributes atts0 = this.variable_name(firstChild, leftPlace, paramList, catchVar);
			myAtts.setCfg(atts0.getCfg());
			myAtts.setPlace(atts0.getPlace());
			myAtts.setIsKnownCall(atts0.isKnownCall());
			break;
		}
		}
		if (myAtts.getPlace() == null) {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
		}

		return myAtts;
	}

	TacAttributes variable_name(ParseNode node, Variable leftPlace, List<TacActualParameter> paramList,
			Variable catchVar) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_STRING: {
			if (paramList != null) {
				String methodName = firstChild.getLexeme().toLowerCase();
				String className = null;
				if (leftPlace.getName().equals("$this")) {
					if (classStack.size() > 0) {
						className = this.classStack.getLast().getName();
					}
				}
				ControlFlowGraph callCfg = this.functionCallHelper(methodName, true, null, paramList, catchVar, true,
						node, className, (Variable) leftPlace);
				myAtts.setCfg(callCfg);
				myAtts.setPlace(catchVar);
				myAtts.setIsKnownCall(true);

			} else {
				firstChild.getLexeme().toLowerCase();
				if (leftPlace.getName().equals("$this")) {
					if (classStack.size() > 0) {
						this.classStack.getLast().getName();
					}
				}
				myAtts = this.namespace_name(node);
				AbstractCfgNode emptyNode = new Empty();
				myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
				myAtts.setPlace(catchVar);
				myAtts.setIsKnownCall(true);
			}
			break;
		}
		case PhpSymbols.T_OPEN_CURLY_BRACES: {
			TacAttributes atts1 = this.expr(node.getChild(1));
			myAtts.setCfg(atts1.getCfg());
			myAtts.setPlace(this.memberPlace);
			myAtts.setIsKnownCall(false);
			break;
		}
		}

		return myAtts;
	}

	TacAttributes simple_indirect_reference(ParseNode node, AbstractTacPlace depPlace) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.T_DOLLAR) {
			AbstractTacPlace myPlace = this.makePlace("${" + depPlace.toString() + "}");
			myPlace.getVariable().setDependsOn(depPlace);
			myAtts.setPlace(myPlace);
		} else {
			AbstractTacPlace transPlace = this.makePlace("${" + depPlace.toString() + "}");
			transPlace.getVariable().setDependsOn(depPlace);
			TacAttributes attsRef = this.simple_indirect_reference(firstChild, transPlace);
			myAtts.setPlace(attsRef.getPlace());
		}

		return myAtts;
	}

	TacAttributes assignment_list(ParseNode node, AbstractTacPlace arrayPlace, int arrayIndex) {
		TacAttributes myAtts = new TacAttributes();

		if (node.getNumChildren() == 3) {
			TacAttributes attsList = this.assignment_list(node.getChild(0), arrayPlace, arrayIndex);
			TacAttributes attsElement = this.assignment_list_element(node.getChild(2), arrayPlace,
					attsList.getArrayIndex());
			connect(attsList.getCfg(), attsElement.getCfg());
			myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), attsElement.getCfg().getTail()));
			myAtts.setArrayIndex(attsList.getArrayIndex() + 1);

		} else {
			TacAttributes attsElement = this.assignment_list_element(node.getChild(0), arrayPlace, arrayIndex);

			myAtts.setCfg(attsElement.getCfg());
			myAtts.setArrayIndex(arrayIndex + 1);
		}
		return myAtts;
	}

	TacAttributes assignment_list_element(ParseNode node, AbstractTacPlace arrayPlace, int arrayIndex) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.variable: {
			TacAttributes attsCvar = this.variable(firstChild);

			AbstractTacPlace arrayElementPlace = this.makeArrayElementPlace(arrayPlace,
					new Literal(String.valueOf(arrayIndex)));

			AbstractCfgNode cfgNode = new AssignSimple((Variable) attsCvar.getPlace(), arrayElementPlace, firstChild);

			connect(attsCvar.getCfg(), cfgNode);

			myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));

			break;
		}

		case PhpSymbols.T_LIST: {
			AbstractTacPlace arrayElementPlace = this.makeArrayElementPlace(arrayPlace,
					new Literal(String.valueOf(arrayIndex)));

			TacAttributes attsList = this.assignment_list(node.getChild(2), arrayElementPlace, 0);

			myAtts.setCfg(attsList.getCfg());

			break;
		}

		case PhpSymbols.T_EPSILON: {

			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
			break;
		}
		}

		return myAtts;
	}

	TacAttributes non_empty_array_pair_list(ParseNode node, Variable arrayPlace) {
		TacAttributes myAtts = new TacAttributes();

		int logId = this.tempId;
		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.non_empty_array_pair_list: {
			if (node.getNumChildren() == 3) {

				TacAttributes attsList = this.non_empty_array_pair_list(firstChild, arrayPlace);
				TacAttributes attsExpr = this.expr(node.getChild(2));

				AbstractTacPlace offsetPlace;
				int largestIndex = attsList.getArrayIndex();
				if (largestIndex == -1) {
					offsetPlace = this.emptyOffsetPlace;
				} else {
					largestIndex++;
					offsetPlace = new Literal(String.valueOf(largestIndex));
					myAtts.setArrayIndex(largestIndex);
				}

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, offsetPlace, attsExpr.getPlace(), false,
						node);

				connect(attsList.getCfg(), attsExpr.getCfg());
				connect(attsExpr.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));

			} else if (node.getChild(2).getSymbol() == PhpSymbols.T_BITWISE_AND) {

				TacAttributes attsList = this.non_empty_array_pair_list(firstChild, arrayPlace);
				TacAttributes attsCvar = this.variable(node.getChild(3));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, this.emptyOffsetPlace,
						attsCvar.getPlace(), true, node);

				connect(attsList.getCfg(), attsCvar.getCfg());
				connect(attsCvar.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));

			} else if (node.getChild(4).getSymbol() == PhpSymbols.expr) {

				TacAttributes attsList = this.non_empty_array_pair_list(firstChild, arrayPlace);
				TacAttributes attsExpr1 = this.expr(node.getChild(2));
				TacAttributes attsExpr2 = this.expr(node.getChild(4));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsExpr1.getPlace(),
						attsExpr2.getPlace(), false, node);

				connect(attsList.getCfg(), attsExpr1.getCfg());
				connect(attsExpr1.getCfg(), attsExpr2.getCfg());
				connect(attsExpr2.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));

			} else {

				TacAttributes attsList = this.non_empty_array_pair_list(firstChild, arrayPlace);
				TacAttributes attsExpr1 = this.expr(node.getChild(2));
				TacAttributes attsCvar = this.variable(node.getChild(5));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsExpr1.getPlace(),
						attsCvar.getPlace(), true, node);

				connect(attsList.getCfg(), attsExpr1.getCfg());
				connect(attsExpr1.getCfg(), attsCvar.getCfg());
				connect(attsCvar.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsList.getCfg().getHead(), cfgNode));
			}

			break;
		}

		case PhpSymbols.expr: {
			if (node.getNumChildren() == 1) {

				TacAttributes attsExpr = this.expr(firstChild);

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, new Literal("0"), attsExpr.getPlace(),
						false, node);

				connect(attsExpr.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr.getCfg().getHead(), cfgNode));

				myAtts.setArrayIndex(0);

			} else if (node.getChild(2).getSymbol() == PhpSymbols.expr) {

				TacAttributes attsExpr1 = this.expr(firstChild);
				TacAttributes attsExpr2 = this.expr(node.getChild(2));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsExpr1.getPlace(),
						attsExpr2.getPlace(), false, node);

				connect(attsExpr1.getCfg(), attsExpr2.getCfg());
				connect(attsExpr2.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr1.getCfg().getHead(), cfgNode));
			} else {

				TacAttributes attsExpr1 = this.expr(firstChild);
				TacAttributes attsCvar = this.variable(node.getChild(3));

				AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, attsExpr1.getPlace(),
						attsCvar.getPlace(), true, node);

				connect(attsExpr1.getCfg(), attsCvar.getCfg());
				connect(attsCvar.getCfg(), cfgNode);

				myAtts.setCfg(new ControlFlowGraph(attsExpr1.getCfg().getHead(), cfgNode));
			}
			break;
		}

		case PhpSymbols.T_BITWISE_AND: {
			TacAttributes attsCvar = this.variable(node.getChild(1));

			AbstractCfgNode cfgNode = this.arrayPairListHelper(arrayPlace, new Literal("0"), attsCvar.getPlace(), true,
					node);

			connect(attsCvar.getCfg(), cfgNode);

			myAtts.setCfg(new ControlFlowGraph(attsCvar.getCfg().getHead(), cfgNode));

			break;
		}
		}

		this.resetId(logId);
		return myAtts;
	}

	TacAttributes CreateNewEncaps_list() {
		TacAttributes myAtts = new TacAttributes();
		myAtts.setEncapsList(new EncapsList());
		return myAtts;
	}

	TacAttributes encaps_list(ParseNode node) {
		TacAttributes myAtts = new TacAttributes();
		ParseNode firstChild = node.getChild(0);

		ParseNode secondChild = node.getChild(1);
		if (node.getChild(0).getSymbol() == PhpSymbols.encaps_list) {
			switch (secondChild.getSymbol()) {

			case PhpSymbols.encaps_var: {
				TacAttributes attsList = this.encaps_list(firstChild);
				TacAttributes attsVar = this.encaps_var(secondChild);

				EncapsList encapsList = attsList.getEncapsList();
				encapsList.add(attsVar.getPlace(), attsVar.getCfg());
				myAtts.setEncapsList(encapsList);
				break;
			}

			case PhpSymbols.T_ENCAPSED_AND_WHITESPACE: {
				this.encapsListHelper(node, myAtts);
				break;
			}
			}

		} else {
			switch (firstChild.getSymbol()) {
			case PhpSymbols.encaps_var: {
				TacAttributes attsVar = this.encaps_var(firstChild);
				myAtts.setCfg(attsVar.getCfg());
				myAtts.getEncapsList().add(attsVar.getPlace(), attsVar.getCfg());

				break;
			}
			case PhpSymbols.T_ENCAPSED_AND_WHITESPACE: {
				TacAttributes attsVar = this.encaps_var(secondChild);
				myAtts.setCfg(attsVar.getCfg());
				myAtts.getEncapsList().add(attsVar.getPlace(), attsVar.getCfg());
				break;
			}
			}

		}
		return myAtts;
	}

	TacAttributes array_pair_list(ParseNode node, Variable arrayPlace) {
		TacAttributes myAtts = new TacAttributes();

		ParseNode firstChild = node.getChild(0);

		if (firstChild.getSymbol() == PhpSymbols.T_EPSILON) {
			AbstractCfgNode emptyNode = new Empty();
			myAtts.setCfg(new ControlFlowGraph(emptyNode, emptyNode));
		} else {
			TacAttributes atts0 = this.non_empty_array_pair_list(firstChild, arrayPlace);
			myAtts.setCfg(atts0.getCfg());
		}

		return myAtts;
	}
}
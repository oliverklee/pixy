package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.AssignArray;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.AssignReference;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.CallReturn;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.CallReturnUnknown;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.CallUnknown;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.Define;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.FunctionEntry;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.Isset;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.Tester;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Global;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;

public class LiteralAnalysis extends AbstractInterproceduralAnalysis {

	private TacConverter tac;

	private GenericRepository<AbstractLatticeElement> repos;

	private AliasAnalysis aliasAnalysis;

	private List<Include> includeNodes;

	public LiteralAnalysis(TacConverter tac, AliasAnalysis aliasAnalysis, AbstractAnalysisType analysisType,
			InterproceduralWorklist workList) {

		this.tac = tac;
		this.repos = new GenericRepository<AbstractLatticeElement>();
		this.aliasAnalysis = aliasAnalysis;
		this.includeNodes = new LinkedList<Include>();

		this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(), analysisType, workList);
	}

	public LiteralAnalysis() {
	}

	protected void initLattice() {
		this.lattice = new LiteralLattice(this.tac.getPlacesList(), this.tac.getConstantsTable(), this.functions,
				this.tac.getSuperSymbolTable());
		this.startValue = new LiteralLatticeElement();
		this.initialValue = this.lattice.getBottom();
	}

	protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();
		Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
		Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

		return new AssignSimple(left, cfgNode.getRight(), mustAliases, mayAliases);
	}

	protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();
		Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(left, aliasInNode);
		Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(left, aliasInNode);

		return new AssignUnary(left, cfgNode.getRight(), cfgNode.getOperator(), mustAliases, mayAliases);
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
		return new AssignReference(cfgNode.getLeft(), cfgNode.getRight());
	}

	protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
		return new Unset(cfgNode.getOperand());
	}

	protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray) cfgNodeX;
		return new AssignArray(cfgNode.getLeft());
	}

	protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation) cfgNodeX;
		TacFunction calledFunction = cfgNode.getCallee();
		TacFunction callingFunction = traversedFunction;

		if (calledFunction == null) {
			return TransferFunctionId.INSTANCE;
		}

		List<TacActualParameter> actualParams = cfgNode.getParamList();
		List<TacFormalParameter> formalParams = calledFunction.getParams();

		AbstractTransferFunction tf = null;

		if (actualParams.size() > formalParams.size()) {
			throw new RuntimeException(
					"More actual than formal params for function " + cfgNode.getFunctionNamePlace().toString()
							+ " in file " + cfgNode.getFileName() + ", line " + cfgNode.getOriginalLineNumber());

		} else {
			tf = new CallPreparation(actualParams, formalParams, callingFunction, calledFunction, this, cfgNode);
		}
		return tf;
	}

	protected AbstractTransferFunction entry(TacFunction traversedFunction) {
		return new FunctionEntry(traversedFunction);
	}

	protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn cfgNodeRet = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn) cfgNodeX;
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNodePrep = cfgNodeRet.getCallPrepNode();
		TacFunction callingFunction = traversedFunction;
		TacFunction calledFunction = cfgNodePrep.getCallee();

		AbstractTransferFunction tf;
		if (calledFunction == null) {
			tf = new CallReturnUnknown(cfgNodeRet);
		} else {

			tf = new CallReturn(this.interproceduralAnalysisInformation.getAnalysisNode(cfgNodePrep), callingFunction,
					calledFunction, cfgNodePrep, cfgNodeRet, this.aliasAnalysis, this.lattice.getBottom());
		}

		return tf;
	}

	protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction) cfgNodeX;
		return new CallBuiltinFunction(cfgNode);
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

			Set<Variable> mustAliases = this.aliasAnalysis.getMustAliases(globalOp, cfgNode);
			Set<Variable> mayAliases = this.aliasAnalysis.getMayAliases(globalOp, cfgNode);

			return new AssignSimple(globalOp, Literal.TOP, mustAliases, mayAliases);
		} else {
			return new AssignReference(globalOp, realGlobal);
		}
	}

	protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
		return new Isset(cfgNode.getLeft(), cfgNode.getRight());
	}

	protected AbstractTransferFunction define(AbstractCfgNode cfgNodeX) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define) cfgNodeX;
		return new Define(this.tac.getConstantsTable(), cfgNode);
	}

	protected AbstractTransferFunction tester(AbstractCfgNode cfgNodeX) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester) cfgNodeX;
		return new Tester(cfgNode);
	}

	protected AbstractTransferFunction include(AbstractCfgNode cfgNodeX) {
		this.includeNodes.add((Include) cfgNodeX);
		return TransferFunctionId.INSTANCE;
	}

	public Literal getLiteral(AbstractTacPlace place, AbstractCfgNode cfgNode) {

		LiteralLatticeElement element = (LiteralLatticeElement) (this.interproceduralAnalysisInformation
				.getAnalysisNode(cfgNode)).getUnrecycledFoldedValue();

		if (element == null) {
			return Literal.TOP;
		} else {
			return element.getLiteral(place);
		}
	}

	public Literal getLiteral(String varName, AbstractCfgNode cfgNode) {
		Variable var = this.tac.getVariable(cfgNode.getEnclosingFunction(), varName);
		if (var == null) {
			return Literal.TOP;
		}
		return this.getLiteral(var, cfgNode);
	}

	public List<Include> getIncludeNodes() {
		return this.includeNodes;
	}

	protected Boolean evalIf(If ifNode, AbstractLatticeElement inValueX) {

		return null;

	}

	public Boolean evalIf(If ifNode) {

		LiteralLatticeElement folded = (LiteralLatticeElement) getAnalysisNode(ifNode).getUnrecycledFoldedValue();
		if (folded == null) {
			return null;
		}
		return this.evalIf(ifNode, folded);
	}

	public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
		return this.repos.recycle(recycleMe);
	}

	public void clean() {

		this.interproceduralAnalysisInformation.foldRecycledAndClean(this);

	}

}

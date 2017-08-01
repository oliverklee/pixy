package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction.Assign;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction.CallReturn;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction.FunctionEntry;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklistPoor;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AliasAnalysis extends AbstractInterproceduralAnalysis {
	private GenericRepository<AbstractLatticeElement> repos;

	public AliasAnalysis(TacConverter tac, AbstractAnalysisType analysisType) {
		this.repos = new GenericRepository<AbstractLatticeElement>();
		this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(), analysisType, new InterproceduralWorklistPoor());
	}

	protected AliasAnalysis() {
	}

	protected void initLattice() {
		this.lattice = new AliasLattice(this);
		this.startValue = this.recycle(new AliasLatticeElement());
		this.initialValue = this.lattice.getBottom();
	}

	protected AbstractTransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {
		AssignReference cfgNode = (AssignReference) cfgNodeX;
		return new Assign(cfgNode.getLeft(), cfgNode.getRight(), this, cfgNode);
	}

	protected AbstractTransferFunction global(AbstractCfgNode cfgNodeX) {

		Global cfgNode = (Global) cfgNodeX;

		Variable globalOp = cfgNode.getOperand();

		TacFunction mainFunc = this.mainFunction;
		SymbolTable mainSymTab = mainFunc.getSymbolTable();
		Variable realGlobal = mainSymTab.getVariable(globalOp.getName());

		if (realGlobal == null) {
			System.out.println("Warning: access to non-existent global " + globalOp.getName());

			return TransferFunctionId.INSTANCE;
		} else {
			return new Assign(globalOp, realGlobal, this, cfgNode);
		}
	}

	protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset) cfgNodeX;
		return new Unset(cfgNode.getOperand(), this);
	}

	protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		CallPreparation cfgNode = (CallPreparation) cfgNodeX;
		TacFunction calledFunction = cfgNode.getCallee();
		TacFunction callingFunction = traversedFunction;
		if (calledFunction == null) {

			throw new RuntimeException("SNH");
		}

		List<TacActualParameter> actualParams = cfgNode.getParamList();
		List<TacFormalParameter> formalParams = calledFunction.getParams();

		AbstractTransferFunction tf = null;

		if (actualParams.size() > formalParams.size()) {
			throw new RuntimeException("More actual than formal params for function "
					+ cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOriginalLineNumber());
		} else {
			tf = new at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction.CallPreparation(callingFunction,
					this, cfgNode);
		}

		return tf;
	}

	protected AbstractTransferFunction entry(TacFunction traversedFunction) {
		return new FunctionEntry(traversedFunction, this);
	}

	protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn) cfgNodeX;
		CallPreparation cfgNodePrep = cfgNode.getCallPrepNode();
		TacFunction calledFunction = cfgNodePrep.getCallee();

		if (calledFunction == null) {
			throw new RuntimeException("SNH");
		}

		AbstractTransferFunction tf = new CallReturn(
				this.interproceduralAnalysisInformation.getAnalysisNode(cfgNodePrep), calledFunction, this,
				cfgNodePrep);

		return tf;
	}

	public Set<Variable> getMustAliases(Variable var, AbstractCfgNode cfgNode) {
		AbstractInterproceduralAnalysisNode aNode = this.interproceduralAnalysisInformation.getAnalysisNode(cfgNode);
		if (aNode == null) {
			System.out.println(cfgNode);
			throw new RuntimeException("gotcha");
		}
		AliasLatticeElement value = this.getFoldedValue(aNode);
		if (value == null) {

			Set<Variable> retMe = new HashSet<Variable>();
			retMe.add(var);
			return retMe;
		} else {
			return value.getMustAliases(var);
		}
	}

	public Set<Variable> getMayAliases(Variable var, AbstractCfgNode cfgNode) {
		AbstractInterproceduralAnalysisNode aNode = this.interproceduralAnalysisInformation.getAnalysisNode(cfgNode);
		AliasLatticeElement value = this.getFoldedValue(aNode);
		if (value == null) {
			Set<Variable> retMe = new HashSet<Variable>();
			retMe.add(var);
			return retMe;
		} else {
			return value.getMayAliases(var);
		}
	}

	public Variable getGlobalMustAlias(Variable var, AbstractCfgNode cfgNode) {
		for (Variable mustAlias : this.getMustAliases(var, cfgNode)) {
			if (mustAlias.isGlobal()) {
				return mustAlias;
			}
		}
		return null;
	}

	public Set<Variable> getLocalMustAliases(Variable var, AbstractCfgNode cfgNode) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Variable mustAlias : this.getMustAliases(var, cfgNode)) {
			if (mustAlias.isLocal()) {
				retMe.add(mustAlias);
			}
		}

		return retMe;
	}

	public Set<Variable> getGlobalMayAliases(Variable var, AbstractCfgNode cfgNode) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Variable mayAlias : this.getMayAliases(var, cfgNode)) {
			if (mayAlias.isGlobal()) {
				retMe.add(mayAlias);
			}
		}

		return retMe;
	}

	public Set<Variable> getLocalMayAliases(Variable var, AbstractCfgNode cfgNode) {
		Set<Variable> retMe = new HashSet<Variable>();
		for (Variable mayAlias : this.getMayAliases(var, cfgNode)) {
			if (mayAlias.isLocal()) {
				retMe.add(mayAlias);
			}
		}
		return retMe;
	}

	public static boolean isSupported(Variable left, Variable right, boolean verbose, int lineno) {

		StringBuilder message = new StringBuilder();
		String description = left + " = & " + right;
		boolean supported = true;
		if (left.isArray()) {
			message.append("Warning: Rereferencing of arrays not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (right.isArray()) {
			message.append("Warning: Rereferencing to arrays not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (left.isArrayElement()) {
			message.append(
					"Warning: Rereferencing of array elements not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (right.isArrayElement()) {
			message.append(
					"Warning: Rereferencing to array elements not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (left.isVariableVariable()) {
			message.append(
					"Warning: Rereferencing of variable variables not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (right.isVariableVariable()) {
			message.append(
					"Warning: Rereferencing to variable variables not supported: " + description + "\nLine: " + lineno);
			supported = false;
		} else if (left.isMember()) {
			supported = false;
		} else if (right.isMember()) {
			supported = false;
		}
		return supported;
	}

	public static boolean isSupported(Variable var) {

		boolean supported = true;

		if (var.isArray()) {
			supported = false;
		} else if (var.isArrayElement()) {
			supported = false;
		} else if (var.isVariableVariable()) {
			supported = false;
		} else if (var.isMember()) {
			supported = false;
		}
		return supported;
	}

	public AliasLatticeElement getFoldedValue(AbstractInterproceduralAnalysisNode node) {

		if (node.hasFoldedValue()) {
			return (AliasLatticeElement) node.getRecycledFoldedValue();
		}

		AliasLatticeElement foldedValue = (AliasLatticeElement) node.computeFoldedValue();
		if (foldedValue == null) {
			return foldedValue;
		}

		foldedValue = (AliasLatticeElement) this.recycle(foldedValue);

		node.setFoldedValue(foldedValue);

		return foldedValue;
	}

	protected Boolean evalIf(If ifNode, AbstractLatticeElement inValue) {

		return null;
	}

	public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
		return this.repos.recycle(recycleMe);
	}

	public void clean() {
		this.interproceduralAnalysisInformation.foldRecycledAndClean(this);
	}
}
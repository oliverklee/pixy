package at.ac.tuwien.infosys.www.pixy.analysis.type;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunctionId;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignArray;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignReference;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallBuiltin;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallReturn;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.CallReturnUnknown;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.Isset;
import at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction.Unset;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

public class TypeAnalysis extends AbstractInterproceduralAnalysis {

	private GenericRepository<AbstractLatticeElement> repos;
	private Collection<String> classNames;

	public TypeAnalysis(TacConverter tac, AbstractAnalysisType analysisType, InterproceduralWorklist workList) {

		this.repos = new GenericRepository<AbstractLatticeElement>();
		this.classNames = tac.getUserClasses().keySet();
		this.initGeneral(tac.getAllFunctions(), tac.getMainFunction(), analysisType, workList);
	}

	public Set<Type> getType(Variable var, AbstractCfgNode cfgNode) {
		AbstractInterproceduralAnalysisNode ian = this.interproceduralAnalysisInformation.getAnalysisNode(cfgNode);
		if (ian == null) {
			System.out.println(var);
			System.out.println(cfgNode);
			System.out.println(cfgNode.getLoc());
			throw new RuntimeException("SNH");
		}
		TypeLatticeElement elem = (TypeLatticeElement) ian.computeFoldedValue();
		if (elem == null) {
			return null;
		}
		return elem.getType(var);
	}

	protected Boolean evalIf(If ifNode, AbstractLatticeElement inValue) {
		return null;
	}

	protected void initLattice() {

		this.lattice = new TypeLattice(this.classNames);
		this.startValue = new TypeLatticeElement();
		this.initialValue = this.lattice.getBottom();
	}

	public AbstractLatticeElement recycle(AbstractLatticeElement recycleMe) {
		return this.repos.recycle(recycleMe);
	}

	protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();

		return new AssignSimple(left, cfgNode.getRight());
	}

	protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();

		return new AssignUnary(left);
	}

	protected AbstractTransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();

		return new AssignBinary(left);
	}

	protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference) cfgNodeX;
		Variable left = (Variable) cfgNode.getLeft();

		return new AssignReference(left, cfgNode.getRight());
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
			throw new RuntimeException("More actual than formal params for function "
					+ cfgNode.getFunctionNamePlace().toString() + " on line " + cfgNode.getOriginalLineNumber());
		} else {
			tf = new CallPreparation(actualParams, formalParams, callingFunction, calledFunction, this);
		}
		return tf;
	}

	protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {

		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn cfgNodeRet = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn) cfgNodeX;
		Call cfgNodeCall = cfgNodeRet.getCallNode();
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation cfgNodePrep = cfgNodeRet.getCallPrepNode();

		TacFunction callingFunction = traversedFunction;
		TacFunction calledFunction = cfgNodeCall.getCallee();
		AbstractTransferFunction tf;
		if (calledFunction == null) {
			tf = new CallReturnUnknown(cfgNodeRet);
		} else {
			tf = new CallReturn(this.interproceduralAnalysisInformation.getAnalysisNode(cfgNodePrep), callingFunction,
					calledFunction, cfgNodeCall);
		}
		return tf;
	}

	protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;
		return new CallBuiltin(cfgNode);
	}

	protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {
		at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset cfgNode = (at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset) cfgNodeX;
		return new Isset((Variable) cfgNode.getLeft());
	}
}
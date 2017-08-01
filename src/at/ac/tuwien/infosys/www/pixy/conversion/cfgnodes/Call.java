package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class Call extends AbstractCfgNode {

	private AbstractTacPlace functionNamePlace;
	private TacFunction callee;

	private Variable retVar;
	private Variable tempVar;
	private List<TacActualParameter> paramList;
	private List<List<Variable>> cbrParamList;

	private String calleeClassName;

	private Variable object;

	public Call(AbstractTacPlace functionNamePlace, TacFunction calledFunction, ParseNode node,
			TacFunction enclosingFunction, Variable retVar, AbstractTacPlace tempPlace,
			List<TacActualParameter> paramList, Variable object) {

		super(node);
		this.functionNamePlace = functionNamePlace;
		if (calledFunction != null) {
			calledFunction.addCalledFrom(this);
		}
		this.setEnclosingFunction(enclosingFunction);

		this.retVar = retVar;
		this.tempVar = (Variable) tempPlace;

		this.paramList = paramList;
		this.cbrParamList = null;

		this.calleeClassName = null;
		this.object = object;

	}

	public TacFunction getCallee() {
		return this.callee;
	}

	public AbstractTacPlace getFunctionNamePlace() {
		return this.functionNamePlace;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		for (Iterator<TacActualParameter> iter = this.paramList.iterator(); iter.hasNext();) {
			TacActualParameter param = (TacActualParameter) iter.next();
			AbstractTacPlace paramPlace = param.getPlace();
			if (paramPlace instanceof Variable) {
				retMe.add((Variable) paramPlace);
			} else {
				retMe.add(null);
			}
		}
		return retMe;
	}

	public Variable getRetVar() {
		return this.retVar;
	}

	public Variable getTempVar() {
		return this.tempVar;
	}

	public List<TacActualParameter> getParamList() {
		return this.paramList;
	}

	public List<List<Variable>> getCbrParams() {

		if (this.cbrParamList != null) {
			return this.cbrParamList;
		}

		List<TacActualParameter> actualParams = this.paramList;
		List<?> formalParams = this.getCallee().getParams();

		this.cbrParamList = new LinkedList<List<Variable>>();

		Iterator<TacActualParameter> actualIter = actualParams.iterator();
		Iterator<?> formalIter = formalParams.iterator();

		while (actualIter.hasNext()) {

			TacActualParameter actualParam = (TacActualParameter) actualIter.next();
			TacFormalParameter formalParam = (TacFormalParameter) formalIter.next();

			if (actualParam.isReference() || formalParam.isReference()) {

				if (!(actualParam.getPlace() instanceof Variable)) {
					throw new RuntimeException("Error in the PHP file!");
				}

				Variable actualVar = (Variable) actualParam.getPlace();
				Variable formalVar = formalParam.getVariable();

				boolean supported = AliasAnalysis.isSupported(formalVar, actualVar, true, this.getOriginalLineNumber());

				if (!supported) {
					continue;
				}

				List<Variable> pairList = new LinkedList<Variable>();
				pairList.add(actualVar);
				pairList.add(formalVar);
				cbrParamList.add(pairList);
			}
		}

		return cbrParamList;
	}

	public String getCalleeClassName() {
		return this.calleeClassName;
	}

	public Variable getObject() {
		return this.object;
	}

	public void replaceVariable(int index, Variable replacement) {
		TacActualParameter param = (TacActualParameter) this.paramList.get(index);
		param.setPlace(replacement);
	}

	public void setCallee(TacFunction function) {
		this.callee = function;
		function.addCalledFrom(this);
	}

	public void setRetVar(Variable retVar) {
		this.retVar = retVar;
	}

	public void setCalleeClassName(String s) {
		this.calleeClassName = s;
	}

}
package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class CallUnknownFunction extends AbstractCfgNode {

	private String functionName;

	private List<TacActualParameter> paramList;

	private Variable tempVar;

	private boolean isMethod;

	public CallUnknownFunction(String functionName, List<TacActualParameter> paramList, AbstractTacPlace tempPlace,
			ParseNode node, boolean isMethod) {

		super(node);
		this.functionName = functionName.toLowerCase();
		this.paramList = paramList;
		this.tempVar = (Variable) tempPlace;
		this.isMethod = isMethod;
	}

	public String getFunctionName() {
		return this.functionName;
	}

	public List<TacActualParameter> getParamList() {
		return this.paramList;
	}

	public Variable getTempVar() {
		return this.tempVar;
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

	public boolean isMethod() {
		return this.isMethod;
	}

	public void replaceVariable(int index, Variable replacement) {
		TacActualParameter param = (TacActualParameter) this.paramList.get(index);
		param.setPlace(replacement);
	}
}
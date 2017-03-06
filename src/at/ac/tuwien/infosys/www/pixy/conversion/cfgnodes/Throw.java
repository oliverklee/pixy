package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class Throw extends AbstractCfgNode {

	public Throw(ParseNode parseNode) {
		super(parseNode);
	}

	public Variable getRetVar() {
		return this.getCallNode().getRetVar();
	}

	public Variable getTempVar() {
		return this.getCallNode().getTempVar();
	}

	public CallPreparation getCallPrepNode() {
		return (CallPreparation) this.getPredecessor().getPredecessor();
	}

	public Call getCallNode() {
		return (Call) this.getPredecessor();
	}

	@SuppressWarnings("rawtypes")
	List getParamsList() {
		return this.getCallPrepNode().getParamList();
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public void replaceVariable(int index, Variable replacement) {
	}

	public void setRetVar(Variable retVar) {
		this.getCallNode().setRetVar(retVar);
	}
}
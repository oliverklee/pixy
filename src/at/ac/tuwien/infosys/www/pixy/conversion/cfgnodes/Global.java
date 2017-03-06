package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class Global extends AbstractCfgNode {

	private Variable operand;

	public Global(AbstractTacPlace operand, ParseNode node) {
		super(node);
		this.operand = (Variable) operand;
	}

	public Variable getOperand() {
		return this.operand;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		if (this.operand instanceof Variable) {
			retMe.add((Variable) this.operand);
		} else {
			retMe.add(null);
		}
		return retMe;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.operand = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}
}
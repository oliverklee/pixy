package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class AssignArray extends AbstractCfgNode {

	private Variable left;

	public AssignArray(Variable left, ParseNode node) {
		super(node);
		this.left = left;
	}

	public Variable getLeft() {
		return this.left;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		retMe.add(this.left);
		return retMe;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.left = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}

}

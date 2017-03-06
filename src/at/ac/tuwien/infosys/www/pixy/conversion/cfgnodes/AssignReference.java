package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class AssignReference extends AbstractCfgNode {

	private Variable left;
	private Variable right;

	public AssignReference(Variable left, Variable right, ParseNode node) {
		super(node);
		this.left = left;
		this.right = right;
	}

	public Variable getLeft() {
		return this.left;
	}

	public AbstractTacPlace getRight() {
		return this.right;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		retMe.add(this.left);
		retMe.add(this.right);
		return retMe;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.left = replacement;
			break;
		case 1:
			this.right = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}
}
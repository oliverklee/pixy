package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class AssignUnary extends AbstractCfgNode {

	private Variable left;
	private AbstractTacPlace right;
	private int op;

	public AssignUnary(Variable left, AbstractTacPlace right, int op, ParseNode node) {
		super(node);
		this.left = left;
		this.right = right;
		this.op = op;
	}

	public Variable getLeft() {
		return this.left;
	}

	public AbstractTacPlace getRight() {
		return this.right;
	}

	public int getOperator() {
		return this.op;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		retMe.add(this.left);
		if (this.right instanceof Variable) {
			retMe.add((Variable) this.right);
		} else {
			retMe.add(null);
		}
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
package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class AssignBinary extends AbstractCfgNode {

	private Variable left;
	private AbstractTacPlace leftOperand;
	private AbstractTacPlace rightOperand;
	private int op;

	public AssignBinary(Variable left, AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op,
			ParseNode node) {

		super(node);
		this.left = left;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.op = op;
	}

	public Variable getLeft() {
		return this.left;
	}

	public AbstractTacPlace getLeftOperand() {
		return this.leftOperand;
	}

	public AbstractTacPlace getRightOperand() {
		return this.rightOperand;
	}

	public int getOperator() {
		return this.op;
	}

	public List<Variable> getVariables() {
		List<Variable> retMe = new LinkedList<Variable>();
		retMe.add(this.left);
		if (this.leftOperand instanceof Variable) {
			retMe.add((Variable) this.leftOperand);
		} else {
			retMe.add(null);
		}
		if (this.rightOperand instanceof Variable) {
			retMe.add((Variable) this.rightOperand);
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
			this.leftOperand = replacement;
			break;
		case 2:
			this.rightOperand = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}
}
package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class If extends AbstractCfgNode {

	private AbstractTacPlace leftOperand;
	private AbstractTacPlace rightOperand;
	private int op;

	public If(AbstractTacPlace leftOperand, AbstractTacPlace rightOperand, int op, ParseNode node) {
		super(node);
		if (!(rightOperand == Constant.TRUE || rightOperand == Constant.FALSE)) {
			throw new RuntimeException("SNH: illegal right operand for if node at line " + node.getLinenoLeft());
		}
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.op = op;
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
		if (this.leftOperand instanceof Variable) {
			retMe.add((Variable) this.leftOperand);
		} else {
			retMe.add(null);
		}
		return retMe;
	}

	public void replaceVariable(int index, Variable replacement) {
		switch (index) {
		case 0:
			this.leftOperand = replacement;
			break;
		default:
			throw new RuntimeException("SNH");
		}
	}
}
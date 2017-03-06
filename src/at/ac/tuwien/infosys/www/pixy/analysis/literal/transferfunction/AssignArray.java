package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class AssignArray extends AbstractTransferFunction {

	private Variable left;
	private boolean supported;

	public AssignArray(AbstractTacPlace left) {

		this.left = (Variable) left;

		if (this.left.isVariableVariable() || this.left.isMember()) {
			this.supported = false;
		} else {
			this.supported = true;
		}
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!supported) {
			return inX;
		}
		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);
		out.assignArray(left);
		return out;
	}
}

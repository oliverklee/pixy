package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class AssignArray extends AbstractTransferFunction {

	private Variable operand;

	public AssignArray(Variable operand) {
		this.operand = operand;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		TypeLatticeElement in = (TypeLatticeElement) inX;
		TypeLatticeElement out = new TypeLatticeElement(in);

		out.assignArray(operand);

		return out;
	}
}

package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class AssignUnary extends AbstractTransferFunction {

	private Variable left;

	public AssignUnary(Variable left) {
		this.left = left;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		TypeLatticeElement in = (TypeLatticeElement) inX;
		TypeLatticeElement out = new TypeLatticeElement(in);
		out.assignUnary(left);
		return out;
	}
}
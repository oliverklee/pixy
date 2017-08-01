package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Tester extends AbstractTransferFunction {

	private Variable retVar;

	public Tester(at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode) {
		this.retVar = (Variable) cfgNode.getEnclosingFunction().getRetVar();
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		out.setRetVar(this.retVar, Literal.TOP);
		return out;
	}
}
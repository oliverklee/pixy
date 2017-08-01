package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;

public class CallReturnUnknown extends AbstractTransferFunction {

	private CallReturn retNode;

	public CallReturnUnknown(CallReturn retNode) {
		this.retNode = retNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		out.handleReturnValueUnknown(this.retNode.getTempVar());

		return out;

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		throw new RuntimeException("SNH");
	}

}

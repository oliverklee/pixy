package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;

public class CallUnknown extends AbstractTransferFunction {

	private CallUnknownFunction cfgNode;

	public CallUnknown(CallUnknownFunction cfgNode) {
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		out.handleReturnValueUnknown(this.cfgNode.getTempVar());

		return out;

	}
}
package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

public class TransferFunctionId extends at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction {

	public static final TransferFunctionId INSTANCE = new TransferFunctionId();

	private TransferFunctionId() {
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement in) {

		return in;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement in, AbstractContext context) {
		return in;
	}

}

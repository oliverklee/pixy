package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

public abstract class AbstractTransferFunction {

	public abstract AbstractLatticeElement transfer(AbstractLatticeElement in);

	public AbstractLatticeElement transfer(AbstractLatticeElement in, AbstractContext context) {
		throw new RuntimeException("SNH: " + this.getClass());
	}

}

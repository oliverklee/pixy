package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;

public class IntraproceduralAnalysisNode extends AbstractAnalysisNode {

	AbstractLatticeElement inValue;

	protected IntraproceduralAnalysisNode(AbstractTransferFunction tf) {
		super(tf);
		this.inValue = null;
	}

	public AbstractLatticeElement getInValue() {
		return this.inValue;
	}

	protected void setInValue(AbstractLatticeElement inValue) {
		this.inValue = inValue;
	}
}
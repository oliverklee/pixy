package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class CallStringAnalysisNode extends AbstractInterproceduralAnalysisNode {

	public CallStringAnalysisNode(AbstractCfgNode node, AbstractTransferFunction tf) {
		super(tf);
	}

}

package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallStringAnalysisNode extends InterproceduralAnalysisNode {
    public CallStringAnalysisNode(AbstractCfgNode node, TransferFunction tf) {
        super(tf);
    }
}
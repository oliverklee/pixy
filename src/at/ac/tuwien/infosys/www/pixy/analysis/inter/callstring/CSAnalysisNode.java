package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class CSAnalysisNode
    extends InterAnalysisNode {

    public CSAnalysisNode(CfgNode node, TransferFunction tf) {
        super(tf);
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CSAnalysisNode
    extends InterAnalysisNode {

    public CSAnalysisNode(CfgNode node, TransferFunction tf) {
        super(tf);
    }
}
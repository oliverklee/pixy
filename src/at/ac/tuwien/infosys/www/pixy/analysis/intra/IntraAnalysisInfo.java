package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IntraAnalysisInfo extends AnalysisInfo {
    public IntraAnalysisInfo() {
        super();
    }

    public IntraAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return (IntraAnalysisNode) this.map.get(cfgNode);
    }

    public TransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        AnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }
}
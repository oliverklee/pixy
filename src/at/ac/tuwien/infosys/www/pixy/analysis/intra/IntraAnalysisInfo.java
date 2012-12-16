package at.ac.tuwien.infosys.www.pixy.analysis.intra;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisInfo;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class IntraAnalysisInfo 
extends AnalysisInfo {

    public IntraAnalysisInfo() {
        super();
    }
    
    public IntraAnalysisNode getAnalysisNode(CfgNode cfgNode) {
        return (IntraAnalysisNode) this.map.get(cfgNode);
    }

    public TransferFunction getTransferFunction (CfgNode cfgNode) {
        AnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }

}

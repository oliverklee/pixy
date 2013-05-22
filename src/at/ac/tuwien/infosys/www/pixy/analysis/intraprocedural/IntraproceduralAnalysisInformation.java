package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IntraproceduralAnalysisInformation extends AnalysisInformation {
    public IntraproceduralAnalysisInformation() {
        super();
    }

    public IntraproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return (IntraproceduralAnalysisNode) this.map.get(cfgNode);
    }

    public TransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        AnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }
}
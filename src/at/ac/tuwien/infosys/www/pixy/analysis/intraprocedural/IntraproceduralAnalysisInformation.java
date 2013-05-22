package at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IntraproceduralAnalysisInformation extends AbstractAnalysisInformation {
    public IntraproceduralAnalysisInformation() {
        super();
    }

    public IntraproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return (IntraproceduralAnalysisNode) this.map.get(cfgNode);
    }

    public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        AbstractAnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }
}
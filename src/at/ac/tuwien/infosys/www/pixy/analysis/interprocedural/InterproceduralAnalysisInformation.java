package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class InterproceduralAnalysisInformation extends AbstractAnalysisInformation {
    public InterproceduralAnalysisInformation() {
        super();
    }

    // folds all analysis nodes (using recycling) and clears the phi maps
    // (=> saves memory)
    public void foldRecycledAndClean(AbstractInterproceduralAnalysis analysis) {
        for (AbstractAnalysisNode analysisNode1 : this.map.values()) {
            AbstractInterproceduralAnalysisNode analysisNode = (AbstractInterproceduralAnalysisNode) analysisNode1;
            AbstractLatticeElement foldedValue = analysisNode.computeFoldedValue();
            foldedValue = analysis.recycle(foldedValue);
            analysisNode.setFoldedValue(foldedValue);
            analysisNode.clearPhiMap();
        }
    }

    // note that not all cfg nodes have an associated analysis node:
    // - nodes inside basic blocks
    // - nodes inside function default cfgs
    // for such nodes, you should query the enclosing basic block, or the
    // entry node of the function default cfg; use the appropriate "get"
    // method of CfgNode to retrieve these nodes
    public AbstractInterproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
        return (AbstractInterproceduralAnalysisNode) this.map.get(cfgNode);
    }

    public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
        AbstractAnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
        return analysisNode.getTransferFunction();
    }
}
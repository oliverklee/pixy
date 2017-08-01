package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class InterproceduralAnalysisInformation extends AbstractAnalysisInformation {

	public InterproceduralAnalysisInformation() {
		super();
	}

	public void foldRecycledAndClean(AbstractInterproceduralAnalysis analysis) {
		for (Iterator<?> iter = this.map.values().iterator(); iter.hasNext();) {
			AbstractInterproceduralAnalysisNode analysisNode = (AbstractInterproceduralAnalysisNode) iter.next();
			AbstractLatticeElement foldedValue = analysisNode.computeFoldedValue();
			foldedValue = analysis.recycle(foldedValue);
			analysisNode.setFoldedValue(foldedValue);
			analysisNode.clearPhiMap();
		}
	}

	public AbstractInterproceduralAnalysisNode getAnalysisNode(AbstractCfgNode cfgNode) {
		return (AbstractInterproceduralAnalysisNode) this.map.get(cfgNode);
	}

	public AbstractTransferFunction getTransferFunction(AbstractCfgNode cfgNode) {
		AbstractAnalysisNode analysisNode = this.getAnalysisNode(cfgNode);
		return analysisNode.getTransferFunction();
	}

}

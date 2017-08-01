package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Add extends AbstractTransferFunction {

	private AbstractCfgNode cfgNode;
	private InclusionDominatorAnalysis incDomAnalysis;

	public Add(AbstractCfgNode cfgNode, InclusionDominatorAnalysis incDomAnalysis) {
		this.cfgNode = cfgNode;
		this.incDomAnalysis = incDomAnalysis;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		InclusionDominatorLatticeElement in = (InclusionDominatorLatticeElement) inX;
		InclusionDominatorLatticeElement out = new InclusionDominatorLatticeElement(in);
		out.add(this.cfgNode);

		out = (InclusionDominatorLatticeElement) this.incDomAnalysis.recycle(out);

		return out;
	}
}

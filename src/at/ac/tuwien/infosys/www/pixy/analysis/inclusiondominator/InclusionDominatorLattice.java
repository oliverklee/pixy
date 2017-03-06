package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;

public class InclusionDominatorLattice extends AbstractLattice {

	private InclusionDominatorAnalysis incDomAnalysis;

	public InclusionDominatorLattice(InclusionDominatorAnalysis incDomAnalysis) {
		this.incDomAnalysis = incDomAnalysis;
	}

	public AbstractLatticeElement lub(AbstractLatticeElement incomingElementX, AbstractLatticeElement targetElementX) {

		if (incomingElementX == this.bottom) {
			return targetElementX;
		}

		if (targetElementX == this.bottom) {
			return incomingElementX;
		}

		InclusionDominatorLatticeElement incomingElement = (InclusionDominatorLatticeElement) incomingElementX;
		InclusionDominatorLatticeElement targetElement = (InclusionDominatorLatticeElement) targetElementX;
		InclusionDominatorLatticeElement resultElement = new InclusionDominatorLatticeElement(targetElement);

		resultElement.lub(incomingElement);
		resultElement = (InclusionDominatorLatticeElement) this.incDomAnalysis.recycle(resultElement);
		return resultElement;
	}

}

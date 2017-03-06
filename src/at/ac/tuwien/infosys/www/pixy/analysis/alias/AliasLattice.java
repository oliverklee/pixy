package at.ac.tuwien.infosys.www.pixy.analysis.alias;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;

public class AliasLattice extends AbstractLattice {

	private AliasAnalysis aliasAnalysis;

	public AliasLattice(AliasAnalysis aliasAnalysis) {
		this.aliasAnalysis = aliasAnalysis;
	}

	public AbstractLatticeElement lub(AbstractLatticeElement incomingElementX, AbstractLatticeElement targetElementX) {
		if (incomingElementX == this.bottom) {

			return targetElementX;
		}

		if (targetElementX == this.bottom) {

			return incomingElementX;
		}

		AliasLatticeElement incomingElement = (AliasLatticeElement) incomingElementX;
		AliasLatticeElement targetElement = (AliasLatticeElement) targetElementX;

		AliasLatticeElement resultElement = new AliasLatticeElement(targetElement);

		resultElement.lub(incomingElement);

		resultElement = (AliasLatticeElement) this.aliasAnalysis.recycle(resultElement);

		return resultElement;
	}

}

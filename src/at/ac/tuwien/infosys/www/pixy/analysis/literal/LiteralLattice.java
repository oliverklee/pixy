package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import java.util.List;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;

public class LiteralLattice extends AbstractLattice {

	public LiteralLattice(List<?> places, ConstantsTable constantsTable, List<?> functions,
			SymbolTable superSymbolTable) {

		LiteralLatticeElement.initDefault(places, constantsTable, functions, superSymbolTable);
	}

	public AbstractLatticeElement lub(AbstractLatticeElement incomingElementX, AbstractLatticeElement targetElementX) {

		if (incomingElementX == this.bottom) {
			if (targetElementX != this.bottom) {
				return targetElementX.cloneMe();
			} else {
				return this.bottom;
			}
		}

		if (targetElementX == this.bottom) {
			return incomingElementX.cloneMe();
		}

		LiteralLatticeElement incomingElement = (LiteralLatticeElement) incomingElementX;
		LiteralLatticeElement targetElement = (LiteralLatticeElement) targetElementX;
		LiteralLatticeElement resultElement = new LiteralLatticeElement(targetElement);

		resultElement.lub(incomingElement);

		return resultElement;

	}

}

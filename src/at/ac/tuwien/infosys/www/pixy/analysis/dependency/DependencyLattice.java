package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

class DependencyLattice extends AbstractLattice {

	@SuppressWarnings("rawtypes")
	DependencyLattice(List<AbstractTacPlace> places, ConstantsTable constantsTable, List functions,
			SymbolTable superSymbolTable, Variable memberPlace) {

		DependencyLatticeElement.initDefault(places, constantsTable, functions, superSymbolTable, memberPlace);
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

		DependencyLatticeElement incomingElement = (DependencyLatticeElement) incomingElementX;
		DependencyLatticeElement targetElement = (DependencyLatticeElement) targetElementX;
		DependencyLatticeElement resultElement = new DependencyLatticeElement(targetElement);

		resultElement.lub(incomingElement);

		return resultElement;
	}

}

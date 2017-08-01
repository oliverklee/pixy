package at.ac.tuwien.infosys.www.pixy.analysis.type;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;

public class TypeLattice extends AbstractLattice {

	public TypeLattice(Collection<String> classNames) {
		Type.initTypes(classNames);
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

		TypeLatticeElement incomingElement = (TypeLatticeElement) incomingElementX;
		TypeLatticeElement targetElement = (TypeLatticeElement) targetElementX;
		TypeLatticeElement resultElement = new TypeLatticeElement(targetElement);

		resultElement.lub(incomingElement);

		return resultElement;
	}
}
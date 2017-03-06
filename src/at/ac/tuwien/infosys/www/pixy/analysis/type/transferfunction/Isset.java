package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Isset extends AbstractTransferFunction {

	private Variable setMe;

	public Isset(Variable setMe) {
		this.setMe = setMe;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		TypeLatticeElement in = (TypeLatticeElement) inX;
		TypeLatticeElement out = new TypeLatticeElement(in);

		out.unset(setMe);

		return out;
	}
}
package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Isset extends AbstractTransferFunction {

	private Variable setMe;

	public Isset(AbstractTacPlace setMe, AbstractTacPlace testMe) {
		this.setMe = (Variable) setMe;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);

		if (!setMe.isTemp()) {
			throw new RuntimeException("SNH");
		}
		Set<Variable> mustAliases = new HashSet<Variable>();
		mustAliases.add(setMe);
		Set<?> mayAliases = Collections.EMPTY_SET;
		out.assignSimple(setMe, Literal.TOP, mustAliases, mayAliases);
		return out;
	}
}
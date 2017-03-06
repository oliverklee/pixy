package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

public class DummyLiteralAnalysis extends LiteralAnalysis {

	public DummyLiteralAnalysis() {
		super();
	}

	public Literal getLiteral(AbstractTacPlace place, AbstractCfgNode cfgNode) {
		if (place instanceof Literal) {
			return (Literal) place;
		} else {
			return Literal.TOP;
		}
	}

	public Boolean evalIf(If ifNode) {
		return null;
	}
}

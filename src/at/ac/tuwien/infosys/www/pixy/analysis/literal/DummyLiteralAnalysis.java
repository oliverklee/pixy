package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DummyLiteralAnalysis extends LiteralAnalysis {
    public DummyLiteralAnalysis() {
        super();
    }

    // best-effort resolution
    public Literal getLiteral(AbstractTacPlace place, AbstractCfgNode cfgNode) {
        if (place instanceof Literal) {
            return (Literal) place;
        } else {
            return Literal.TOP;
        }
    }

    // best-effort evaluation
    public Boolean evalIf(If ifNode) {
        // be careful...
        return null;
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeIf;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DummyLiteralAnalysis extends LiteralAnalysis {
    public DummyLiteralAnalysis() {
        super();
    }

    // best-effort resolution
    public Literal getLiteral(TacPlace place, CfgNode cfgNode) {
        if (place instanceof Literal) {
            return (Literal) place;
        } else {
            return Literal.TOP;
        }
    }

    // best-effort evaluation
    public Boolean evalIf(CfgNodeIf ifNode) {
        // be careful...
        return null;
    }
}
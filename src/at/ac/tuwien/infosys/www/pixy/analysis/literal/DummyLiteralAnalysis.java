package at.ac.tuwien.infosys.www.pixy.analysis.literal;

import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.*;

public class DummyLiteralAnalysis
extends LiteralAnalysis {

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
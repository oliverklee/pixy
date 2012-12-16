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
        
        /*
        TacPlace left = ifNode.getLeftOperand();
        TacPlace right = ifNode.getRightOperand();
        
        // "if node" tests always have the form "place == <true or false>"
        if (ifNode.getOperator() != TacOperators.IS_EQUAL) {
            throw new RuntimeException("SNH");
        }

        Literal leftLit = null;
        if (left instanceof Literal) {
            leftLit = (Literal) left;
        } else if (left instanceof Constant) {
            if (left.equals(Constant.TRUE)) {
                leftLit = Literal.TRUE;
            } else if (left.equals(Constant.FALSE)) {
                leftLit = Literal.FALSE;
            } else {
                // no chance
                return null;
            }
        } else {
            // no chance
            return null;
        }
        
        // right operand's literal:
        // can only by true or false (that's what the TacConverter promised)
        Literal rightLit = null;
        if (right == Constant.TRUE) {
            rightLit = Literal.TRUE;
        } else if (right == Constant.FALSE) {
            rightLit = Literal.FALSE;
        } else {
            throw new RuntimeException("SNH: " + right + " is neither true nor false");
        }

        Literal leftBool = leftLit.getBoolValueLiteral();
        if (leftBool == Literal.TOP) {
            //System.out.println("can't determine boolean value of left operand");
            return null;
        } else if (leftBool == rightLit) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
        */
    }

}

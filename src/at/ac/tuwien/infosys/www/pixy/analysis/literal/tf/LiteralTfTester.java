package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeTester;

/**
 * Transfer function for special ~_test_ node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralTfTester
    extends TransferFunction {

    // provides access to the return variable of the function enclosing
    // this ~_test_ node
    private Variable retVar;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public LiteralTfTester(CfgNodeTester cfgNode) {
        this.retVar = cfgNode.getEnclosingFunction().getRetVar();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // this one is easy: just set the return variable to TOP
        out.setRetVar(this.retVar, Literal.TOP);
        return out;
    }
}
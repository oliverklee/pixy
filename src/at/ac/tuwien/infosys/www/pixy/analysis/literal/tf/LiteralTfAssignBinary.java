package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.Set;

/**
 * Transfer function for binary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralTfAssignBinary
    extends TransferFunction {

    private Variable left;
    private TacPlace leftOperand;
    private TacPlace rightOperand;
    private int op;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;
    private CfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public LiteralTfAssignBinary(
        TacPlace left, TacPlace leftOperand, TacPlace rightOperand, int op, Set<Variable> mustAliases,
        Set<Variable> mayAliases, CfgNode cfgNode
    ) {
        this.left = (Variable) left;  // must be a variable
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.op = op;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // let the lattice element handle the details
        out.assignBinary(left, leftOperand, rightOperand, op,
            mustAliases, mayAliases, cfgNode);

        return out;
    }
}
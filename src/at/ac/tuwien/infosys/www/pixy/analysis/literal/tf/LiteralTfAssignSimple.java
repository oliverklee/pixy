package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// transfer function for simple assignment nodes
public class LiteralTfAssignSimple
    extends TransferFunction {

    private Variable left;
    private TacPlace right;
    private Set mustAliases;
    private Set mayAliases;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public LiteralTfAssignSimple(TacPlace left, TacPlace right,
                                 Set mustAliases, Set mayAliases) {

        this.left = (Variable) left;  // must be a variable
        this.right = right;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // let the lattice element handle the details
        out.assignSimple(left, right, mustAliases, mayAliases);

        return out;
    }
}
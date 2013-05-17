package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * Transfer function for array assignment nodes ("left = array()").
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralTfAssignArray
    extends TransferFunction {

    private Variable left;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public LiteralTfAssignArray(TacPlace left) {

        this.left = (Variable) left;    // must be a variable

        // note that we DO support such statements for arrays and array elements
        if (this.left.isVariableVariable() || this.left.isMember()) {
            this.supported = false;
        } else {
            this.supported = true;
        }
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // if this statement is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // let the lattice element handle the details (set the whole subtree
        // to NULL);
        // LATER: the actual result is more imprecise than it seems, because
        // "$x = array()" is translated to "_t0 = array(); $x = _t0", and
        // since there are no known array elements of _t0, the elements of
        // $x become TOP instead of NULL; for taint analysis, this issue
        // is handled by using array labels; could also be done here
        out.assignArray(left);

        return out;
    }
}
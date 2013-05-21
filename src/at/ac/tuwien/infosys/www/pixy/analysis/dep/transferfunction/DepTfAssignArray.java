package at.ac.tuwien.infosys.www.pixy.analysis.dep.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

/**
 * Transfer function for array assignment nodes ("left = array()").
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepTfAssignArray extends TransferFunction {
    private Variable left;
    private boolean supported;
    private CfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfAssignArray(TacPlace left, CfgNode cfgNode) {

        this.left = (Variable) left;    // must be a variable
        this.cfgNode = cfgNode;

        // note that we DO support such statements for arrays and array elements
        this.supported = !(this.left.isVariableVariable() || this.left.isMember());
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

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // let the lattice element handle the details (set the whole subtree
        // and left's caFlag to HARMLESS (if it is an array));
        // NOTE:
        // "$x = array()" is translated to "_t0 = array(); $x = _t0", and
        // since there are no known array elements of _t0, the elements of
        // $x become would become TAINTED instead of UNTAINTED;
        // this is solved by using array flags
        out.assignArray(left, cfgNode);
        return out;
    }
}
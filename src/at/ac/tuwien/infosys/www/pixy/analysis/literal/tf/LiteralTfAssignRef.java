package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for reference assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LiteralTfAssignRef
    extends TransferFunction {

    private Variable left;
    private Variable right;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public LiteralTfAssignRef(TacPlace left, TacPlace right) {

        this.left = (Variable) left;    // must be a variable
        this.right = (Variable) right;  // must be a variable

        // check for unsupported features
        this.supported = AliasAnalysis.isSupported(this.left, this.right, false, -1);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // if this reference assignment is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // "left =& right" means that left is redirected to right;
        // for the literal mapping, this means that nothing changes
        // except that left receives the literal of right;
        // we achieve this through the following actions:

        Set<Variable> mustAliases = new HashSet<Variable>();
        mustAliases.add(left);
        Set mayAliases = Collections.EMPTY_SET;

        // let the lattice element handle the details
        out.assignSimple(left, right, mustAliases, mayAliases);

        return out;
    }
}
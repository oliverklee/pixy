package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for reference assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignReference extends AbstractTransferFunction {
    private Variable left;
    private Variable right;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignReference(AbstractTacPlace left, AbstractTacPlace right) {

        this.left = (Variable) left;    // must be a variable
        this.right = (Variable) right;  // must be a variable

        // check for unsupported features
        this.supported = AliasAnalysis.isSupported(this.left, this.right, false, -1);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

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

        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(left);
        Set<Variable> mayAliases = Collections.emptySet();

        // let the lattice element handle the details
        out.assignSimple(left, right, mustAliases, mayAliases);

        return out;
    }
}
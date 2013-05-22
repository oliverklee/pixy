package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Set;

/**
 * Transfer function for simple assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignSimple extends AbstractTransferFunction {
    private Variable left;
    private AbstractTacPlace right;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignSimple(AbstractTacPlace left, AbstractTacPlace right, Set<Variable> mustAliases, Set<Variable> mayAliases) {
        this.left = (Variable) left;  // must be a variable
        this.right = right;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);

        // let the lattice element handle the details
        out.assignSimple(left, right, mustAliases, mayAliases);

        return out;
    }
}
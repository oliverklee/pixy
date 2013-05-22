package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Set;

/**
 * Transfer function for unary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignUnary extends AbstractTransferFunction {
    private Variable left;
    private AbstractTacPlace right;
    private int op;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignUnary(
        AbstractTacPlace left, AbstractTacPlace right, int op, Set<Variable> mustAliases, Set<Variable> mayAliases
    ) {
        this.left = (Variable) left;  // must be a variable
        this.right = right;
        this.op = op;
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
        out.assignUnary(left, right, op, mustAliases, mayAliases);

        return out;
    }
}
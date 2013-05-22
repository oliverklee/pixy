package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * Transfer function for unary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignUnary extends AbstractTransferFunction {
    private Variable left;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignUnary(Variable left) {
        this.left = left;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        // let the lattice element handle the details
        out.assignUnary(left);

        return out;
    }
}
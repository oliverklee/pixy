package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * Transfer function for unary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTfAssignUnary extends TransferFunction {
    private Variable left;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public TypeTfAssignUnary(Variable left) {
        this.left = left;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        // let the lattice element handle the details
        out.assignUnary(left);

        return out;
    }
}
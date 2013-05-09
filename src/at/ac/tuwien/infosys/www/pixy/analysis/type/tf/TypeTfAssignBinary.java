package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// transfer function for unary assignment nodes
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTfAssignBinary
    extends TransferFunction {

    private Variable left;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public TypeTfAssignBinary(Variable left) {
        this.left = left;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        // let the lattice element handle the details
        out.assignBinary(left);

        return out;
    }
}
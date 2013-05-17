package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * Transfer function for reference assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTfAssignRef
    extends TransferFunction {

    private Variable left;
    private TacPlace right;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public TypeTfAssignRef(Variable left, TacPlace right) {
        this.left = left;
        this.right = right;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        // let the lattice element handle the details
        out.assign(left, right);

        return out;
    }
}
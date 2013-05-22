package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Isset extends TransferFunction {
    private Variable setMe;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Isset(Variable setMe) {
        this.setMe = setMe;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.unset(setMe);

        return out;
    }
}
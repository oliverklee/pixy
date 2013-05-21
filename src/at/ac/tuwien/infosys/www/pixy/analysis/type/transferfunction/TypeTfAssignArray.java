package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTfAssignArray extends TransferFunction {
    private Variable operand;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public TypeTfAssignArray(Variable operand) {
        this.operand = operand;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.assignArray(operand);

        return out;
    }
}
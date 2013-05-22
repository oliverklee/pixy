package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignArray extends AbstractTransferFunction {
    private Variable operand;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public AssignArray(Variable operand) {
        this.operand = operand;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.assignArray(operand);

        return out;
    }
}
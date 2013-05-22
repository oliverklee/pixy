package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallReturnUnknown extends AbstractTransferFunction {
    private CallReturn retNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public CallReturnUnknown(CallReturn retNode) {
        this.retNode = retNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.handleReturnValueUnknown(this.retNode.getTempVar());

        return out;
    }

    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
        throw new RuntimeException("SNH");
    }
}
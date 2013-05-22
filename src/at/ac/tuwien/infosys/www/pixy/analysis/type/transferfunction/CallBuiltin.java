package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallBuiltin extends AbstractTransferFunction {
    private CallBuiltinFunction cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public CallBuiltin(CallBuiltinFunction cfgNode) {
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.handleReturnValueBuiltin(this.cfgNode.getTempVar());

        return out;
    }
}
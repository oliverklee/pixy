package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallOfBuiltinFunction;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTfCallBuiltin extends TransferFunction {
    private CallOfBuiltinFunction cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public TypeTfCallBuiltin(CallOfBuiltinFunction cfgNode) {
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        TypeLatticeElement in = (TypeLatticeElement) inX;
        TypeLatticeElement out = new TypeLatticeElement(in);

        out.handleReturnValueBuiltin(this.cfgNode.getTempVar());

        return out;
    }
}
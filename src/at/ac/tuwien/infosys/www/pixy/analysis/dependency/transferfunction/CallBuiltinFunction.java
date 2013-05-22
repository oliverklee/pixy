package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Dep;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallBuiltinFunction extends TransferFunction {
    private at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public CallBuiltinFunction(at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction cfgNode) {
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // create an appropariate taint value (holding the function's name);
        // the array label is identic to the taint value
        Set<Dep> ets = new HashSet<>();
        ets.add(Dep.create(this.cfgNode));
        DepSet retDepSet = DepSet.create(ets);
        DepSet retArrayLabel = retDepSet;

        // assign this taint/label to the node's temporary
        out.handleReturnValueBuiltin(this.cfgNode.getTempVar(), retDepSet, retArrayLabel);

        return out;
    }
}
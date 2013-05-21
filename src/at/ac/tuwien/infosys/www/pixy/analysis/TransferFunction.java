package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.Context;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class TransferFunction {
    public abstract LatticeElement transfer(LatticeElement in);

    // method for transfer functions that need to know about the
    // current context; needed for the transfer
    // function of Call Return Nodes; otherwise, they would not
    // have access to the information entering the Call Preparation Node
    public LatticeElement transfer(LatticeElement in, Context context) {
        throw new RuntimeException("SNH: " + this.getClass());
    }
}
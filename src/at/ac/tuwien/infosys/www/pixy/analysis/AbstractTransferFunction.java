package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractTransferFunction {
    public abstract AbstractLatticeElement transfer(AbstractLatticeElement in);

    // method for transfer functions that need to know about the
    // current context; needed for the transfer
    // function of Call Return Nodes; otherwise, they would not
    // have access to the information entering the Call Preparation Node
    public AbstractLatticeElement transfer(AbstractLatticeElement in, AbstractContext context) {
        throw new RuntimeException("SNH: " + this.getClass());
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TransferFunctionId extends AbstractTransferFunction {
    public static final TransferFunctionId INSTANCE = new TransferFunctionId();

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // Singleton class
    private TransferFunctionId() {
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement in) {
        // no need to clone the incoming element:
        // only has to be done if the output element is different,
        // since in that case changing the output element would also
        // affect the input element (would be wrong)
        return in;
    }

    public AbstractLatticeElement transfer(AbstractLatticeElement in, AbstractContext context) {
        return in;
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;

public class TransferFunctionId
    extends TransferFunction {

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

    public LatticeElement transfer(LatticeElement in) {
        // no need to clone the incoming element:
        // only has to be done if the output element is different,
        // since in that case changing the output element would also
        // affect the input element (would be wrong)
        return in;
    }

    public LatticeElement transfer(LatticeElement in, Context context) {
        return in;
    }
}
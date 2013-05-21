package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CallString;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;

/**
 * Worklist element for connector computation.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class ConnectorWorkListElement {
    private final TacFunction function;
    private final CallString callString;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    ConnectorWorkListElement(TacFunction function, CallString callString) {
        this.function = function;
        this.callString = callString;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    TacFunction getFunction() {
        return this.function;
    }

    CallString getCallString() {
        return this.callString;
    }
}
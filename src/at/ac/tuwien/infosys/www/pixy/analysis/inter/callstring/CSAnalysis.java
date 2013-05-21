package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.*;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNodeCall;

import java.util.List;

/**
 * Base class for analysis using the call string approach of Sharir and Pnueli.
 *
 * Use this if your lattice has infinite breadth.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CSAnalysis extends AnalysisType {
    // INPUT ***********************************************************************

    // results from preceding connector computation (for interprocedural
    // propagation)
    ConnectorComputation connectorComp;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public CSAnalysis(ConnectorComputation connectorComp) {
        super();
        this.connectorComp = connectorComp;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

//  getPropagationContext ***********************************************************

    public Context getPropagationContext(CfgNodeCall callNode, Context contextX) {

        CSContext context = (CSContext) contextX;
        return this.connectorComp.getTargetContext(callNode, context.getPosition());
    }

//  getReverseTargets ***************************************************************

    public List<ReverseTarget> getReverseTargets(TacFunction exitedFunction, Context contextX) {

        CSContext context = (CSContext) contextX;
        return this.connectorComp.getReverseTargets(exitedFunction, context.getPosition());
    }

//  *********************************************************************************

    public ConnectorComputation getConnectorComputation() {
        return this.connectorComp;
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

    public boolean useSummaries() {
        return false;
    }

    public InterAnalysisNode makeAnalysisNode(CfgNode cfgNode, TransferFunction tf) {
        return new CSAnalysisNode(cfgNode, tf);
    }

    public Context initContext(InterAnalysis analysis) {
        return new CSContext(0);
    }
}
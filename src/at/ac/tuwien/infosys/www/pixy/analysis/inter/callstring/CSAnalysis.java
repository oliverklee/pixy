package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ReverseTarget;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

// base class for analysis using the call string approach of Sharir and
// Pnueli; use this if your lattice has infinite breadth
public class CSAnalysis 
extends AnalysisType {

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
    /*
    public CallGraph getCallGraph() {
        return this.connectorComp.getCallGraph();
    }
    */

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




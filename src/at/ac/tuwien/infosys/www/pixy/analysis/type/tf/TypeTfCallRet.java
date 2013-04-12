package at.ac.tuwien.infosys.www.pixy.analysis.type.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

public class TypeTfCallRet
    extends TransferFunction {

    private InterAnalysisNode analysisNodeAtCallPrep;
    private TacFunction caller;
    private TacFunction callee;
    private CfgNodeCall callNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public TypeTfCallRet(
        InterAnalysisNode analysisNodeAtCallPrep,
        TacFunction caller,
        TacFunction callee,
        CfgNodeCall retNode) {

        this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
        this.caller = caller;
        this.callee = callee;

        this.callNode = retNode;

    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX, Context context) {

        // lattice element entering the call prep node under the current
        // context
        TypeLatticeElement origInfo =
            (TypeLatticeElement) this.analysisNodeAtCallPrep.getPhiValue(context);

        // lattice element coming in from the callee (= base for interprocedural info);
        // still contains the callee's locals
        TypeLatticeElement calleeIn = (TypeLatticeElement) inX;

        // start only with default mappings
        TypeLatticeElement outInfo = new TypeLatticeElement();

        // copy mappings of "global-like" places from calleeIn to outInfo
        // ("global-like": globals, superglobals, and constants)
        outInfo.copyGlobalLike(calleeIn);

        // LOCAL VARIABLES *************

        if (this.caller.isMain()) {
            // if the caller is main:
            // its local variables are global variables

            // don't forget the main function's temporaries
            outInfo.copyMainTemporaries(origInfo);

            outInfo.handleReturnValue(this.callNode, calleeIn, callee);

        } else {

            // initialize local variables with the mappings at call-time
            outInfo.copyLocals(origInfo);

            outInfo.handleReturnValue(this.callNode, calleeIn, callee);

        }

        // if we are in this transfer function, it means that we know
        // which function was called; if there is an object upon which
        // this function was invoked (i.e., if this is a method call),
        // we also know for sure that this object has to be an instance
        // of the class that this method belongs to; add this info
        Variable invocObject = this.callNode.getObject();
        if (invocObject != null) {
            outInfo.setTypeString(invocObject, callee.getClassName());
        }

        return outInfo;

    }

    // just a dummy method in order to make me conform to the interface;
    // the Analysis uses the other transfer method instead
    public LatticeElement transfer(LatticeElement inX) {
        throw new RuntimeException("SNH");
    }
}
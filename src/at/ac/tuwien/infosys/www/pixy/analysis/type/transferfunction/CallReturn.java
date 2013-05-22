package at.ac.tuwien.infosys.www.pixy.analysis.type.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallReturn extends AbstractTransferFunction {
    private AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep;
    private TacFunction caller;
    private TacFunction callee;
    private Call callNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public CallReturn(
        AbstractInterproceduralAnalysisNode analysisNodeAtCallPrep,
        TacFunction caller,
        TacFunction callee,
        Call retNode) {

        this.analysisNodeAtCallPrep = analysisNodeAtCallPrep;
        this.caller = caller;
        this.callee = callee;

        this.callNode = retNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX, AbstractContext context) {

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
    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {
        throw new RuntimeException("SNH");
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DepSet;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester;

import java.util.LinkedList;
import java.util.List;

/**
 * Transfer function for special ~_test_ node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepTfTester extends TransferFunction {
    // provides access to the return variable of the function enclosing
    // this ~_test_ node
    private Variable retVar;

    // test taint or array label? corresponds to the final fields in Tester
    private int whatToTest;

    // List of Variables (formal params) that are to be tested
    private List<Variable> testUs;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfTester(Tester cfgNode) {

        TacFunction function = cfgNode.getEnclosingFunction();
        this.retVar = function.getRetVar();
        this.whatToTest = cfgNode.getWhatToTest();
        this.testUs = new LinkedList<>();

        // extract formals that are to be tested
        for (Integer paramNumber : cfgNode.getParamNumbers()) {
            int param_int = paramNumber;
            TacFormalParameter formalParam = function.getParam(param_int);
            if (formalParam == null) {
                throw new RuntimeException("Error: Function " + function.getName() +
                    " has no param #" + param_int + "; check builtin functions" +
                    "file");
            }
            this.testUs.add(formalParam.getVariable());
        }
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        if (whatToTest == Tester.TEST_TAINT) {

            // test taint

            // compute the least upper bound of the variables to be tested
            DepSet useMe = null;
            for (Variable testMe : this.testUs) {
                DepSet testMeTaint = in.getDep(testMe);
                if (useMe == null) {
                    useMe = testMeTaint;
                } else {
                    useMe = DepSet.lub(useMe, testMeTaint);
                }
            }

            out.setRetVar(this.retVar, useMe, useMe);
            return out;
        } else if (whatToTest == Tester.TEST_ARRAYLABEL) {
            throw new RuntimeException("not yet");
        } else {
            throw new RuntimeException("SNH");
        }
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.LinkedList;
import java.util.List;

/**
 * Transfer function for special ~_test_ node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Tester extends AbstractTransferFunction {
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

    public Tester(at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode) {

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

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        DependencyLatticeElement in = (DependencyLatticeElement) inX;
        DependencyLatticeElement out = new DependencyLatticeElement(in);

        if (whatToTest == at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester.TEST_TAINT) {

            // test taint

            // compute the least upper bound of the variables to be tested
            DependencySet useMe = null;
            for (Variable testMe : this.testUs) {
                DependencySet testMeTaint = in.getDep(testMe);
                if (useMe == null) {
                    useMe = testMeTaint;
                } else {
                    useMe = DependencySet.lub(useMe, testMeTaint);
                }
            }

            out.setRetVar(this.retVar, useMe, useMe);
            return out;
        } else if (whatToTest == at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester.TEST_ARRAYLABEL) {
            throw new RuntimeException("not yet");
        } else {
            throw new RuntimeException("SNH");
        }
    }
}
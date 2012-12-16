package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;


// *********************************************************************************
// CfgNodeTester *******************************************************************
// *********************************************************************************

// a special node for the builtin functions file
public class CfgNodeTester
extends CfgNode {
    
    // Set of Integer's indicating the positions of the parameters that
    // are to be tested
    private Set paramNumbers;
    
    static public final int TEST_TAINT = 0;
    static public final int TEST_ARRAYLABEL = 1;
    
    // what to test: must be one of the above final's
    private int whatToTest;
    
// CONSTRUCTORS ********************************************************************    

    public CfgNodeTester(int whatToTest, Set paramNumbers) {
        super();
        this.whatToTest = whatToTest;
        this.paramNumbers = paramNumbers;
    }

// GET *****************************************************************************
    
    public List<Variable> getVariables() {
        return Collections.emptyList();
    }
    
    public int getWhatToTest() {
        return this.whatToTest;
    }
    
    public Set getParamNumbers() {
        return this.paramNumbers;
    }
    
//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
        // should not be necessary for this node
        throw new RuntimeException("SNH");
    }

}



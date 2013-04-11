package at.ac.tuwien.infosys.www.pixy.analysis.alias.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.*;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// transfer function for simple assignment nodes
public class AliasTfAssignRef
extends TransferFunction {

    private Variable left;
    private Variable right;

    private boolean supported;
    private AliasAnalysis aliasAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public AliasTfAssignRef(TacPlace left, TacPlace right, AliasAnalysis aliasAnalysis, CfgNode cfgNode) {

        // both arguments are variables if the PHP input is correct
        this.left = (Variable) left;
        this.right = (Variable) right;

        this.aliasAnalysis= aliasAnalysis;

        // check for unsupported features
        this.supported =
            AliasAnalysis.isSupported(this.left, this.right, true, cfgNode.getOrigLineno());

        /* moved to AliasAnalysis.isSupported()
        this.supported = true;

        // check for unsupported features;
        // - none of the variables must be an array or an array element
        // - none of the variables must be a variable variable
        // - none of the variables must be a member variable
        String description = this.left + " = & " + this.right;
        if (this.left.isArray()) {
            System.out.println("Warning: Rereferencing of arrays not supported: " +
                description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.right.isArray()) {
            System.out.println("Warning: Referencing to arrays not supported: " +
                    description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.left.isArrayElement()) {
            System.out.println("Warning: Rereferencing of array elements not supported: " +
                    description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.right.isArrayElement()) {
            System.out.println("Warning: Referencing to array elements not supported: " +
                    description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.left.isVariableVariable()) {
            System.out.println("Warning: Referencing of variable variables not supported: " +
                    description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.right.isVariableVariable()) {
            System.out.println("Warning: Referencing to variable variables not supported: " +
                    description);
            System.out.println("Line: " + cfgNode.getOrigLineno());
            this.supported = false;
        } else if (this.left.isMember()) {
            // stay silent
            this.supported = false;
        } else if (this.right.isMember()) {
            // stay silent
            this.supported = false;
        }
        */


    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // ignore unsupported operations
        if (!this.supported) {
            return inX;
        }

        // ignore useless statements like "$a =& $a"
        if (this.left == this.right) {
            return inX;
        }

        AliasLatticeElement in = (AliasLatticeElement) inX;
        AliasLatticeElement out = new AliasLatticeElement(in);

        // perform redirect operation on "out"
        out.redirect(this.left, this.right);

        // recycle
        out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

        return out;
    }
}
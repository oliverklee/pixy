package at.ac.tuwien.infosys.www.pixy.analysis.alias.tf;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
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
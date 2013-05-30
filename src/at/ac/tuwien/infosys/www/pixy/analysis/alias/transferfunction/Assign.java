package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * Transfer function for simple assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Assign extends AbstractTransferFunction {
    private Variable left;
    private Variable right;

    private boolean supported;
    private AliasAnalysis aliasAnalysis;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Assign(AbstractTacPlace left, AbstractTacPlace right, AliasAnalysis aliasAnalysis, AbstractCfgNode cfgNode) {

        // both arguments are variables if the PHP input is correct
        this.left = (Variable) left;
        this.right = (Variable) right;

        this.aliasAnalysis = aliasAnalysis;

        // check for unsupported features
        this.supported =
            AliasAnalysis.isSupported(this.left, this.right, true, cfgNode.getOriginalLineNumber());
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

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
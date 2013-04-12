package at.ac.tuwien.infosys.www.pixy.analysis.dep.tf;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.*;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// transfer function for unset nodes
public class DepTfUnset
extends TransferFunction {

    private Variable operand;
    private CfgNode cfgNode;
    private boolean supported;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfUnset(TacPlace operand, CfgNode cfgNode) {

        // only variables can be unset
        if (!operand.isVariable()) {
            throw new RuntimeException("Trying to unset a non-variable.");
        }

        this.operand = (Variable) operand;
        this.cfgNode = cfgNode;
        this.supported = true;

    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // if this statement is not supported by our alias analysis,
        // we simply ignore it
        if (!supported) {
            return inX;
        }

        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // unsetting a variable means setting it to NULL (untainted/clean)
        Set<Variable> mustAliases = new HashSet<Variable>();
        mustAliases.add(operand);
        Set mayAliases = Collections.EMPTY_SET;
        out.assign(operand, mustAliases, mayAliases, cfgNode);

        return out;
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dep.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Transfer function for "isset" tests,
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepTfIsset extends TransferFunction {
    private Variable setMe;
    private TacPlace testMe;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public DepTfIsset(TacPlace setMe, TacPlace testMe, AbstractCfgNode cfgNode) {
        this.setMe = (Variable) setMe;  // must be a variable
        this.testMe = testMe;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        // System.out.println("transfer method: " + setMe + " = " + setTo);
        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        if (!setMe.isTemp()) {
            throw new RuntimeException("SNH");
        }

        // always results in a boolean, which is always untainted/clean;
        // not so elegant, but working: simply use Literal.FALSE
        Set<Variable> mustAliases = new HashSet<>();
        mustAliases.add(setMe);
        Set<Variable> mayAliases = Collections.emptySet();
        out.assign(setMe, mustAliases, mayAliases, cfgNode);

        return out;
    }
}
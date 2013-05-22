package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
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
public class Isset extends AbstractTransferFunction {
    private Variable setMe;
    private AbstractTacPlace testMe;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public Isset(AbstractTacPlace setMe, AbstractTacPlace testMe, AbstractCfgNode cfgNode) {
        this.setMe = (Variable) setMe;  // must be a variable
        this.testMe = testMe;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

        // System.out.println("transfer method: " + setMe + " = " + setTo);
        DependencyLatticeElement in = (DependencyLatticeElement) inX;
        DependencyLatticeElement out = new DependencyLatticeElement(in);

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
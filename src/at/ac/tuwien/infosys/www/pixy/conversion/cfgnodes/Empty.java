package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Empty extends AbstractCfgNode {
// CONSTRUCTORS ********************************************************************

    public Empty() {
        super();
        // empty CFG nodes will be deleted from the CFG, so their ID's can be
        // recycled; TOO DANGEROUS TO DO IT HERE! better: additional pass over
        // all CFGs
    }

//  GET ****************************************************************************

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
    }
}
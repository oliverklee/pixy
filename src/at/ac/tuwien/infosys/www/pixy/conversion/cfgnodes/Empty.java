package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.util.Collections;
import java.util.List;

/**
 * This class represents an empty CFG node.
 *
 * Empty nodes will be removed from the CFG during optimization.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Empty extends AbstractCfgNode {
    public Empty() {
        super();
        // empty CFG nodes will be deleted from the CFG, so their ID's can be
        // recycled; TOO DANGEROUS TO DO IT HERE! better: additional pass over
        // all CFGs
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    public void replaceVariable(int index, Variable replacement) {
    }
}
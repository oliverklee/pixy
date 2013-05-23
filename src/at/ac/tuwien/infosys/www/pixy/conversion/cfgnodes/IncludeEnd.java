package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Indicates the end of an included section (inserted during include resolution).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncludeEnd extends AbstractCfgNode {
    private File file;  // file in which this node occurs
    private IncludeStart peer;

//  CONSTRUCTORS *******************************************************************

    // - instantiate IncludeStart
    // - instantiate IncludeEnd with the following constructor
    // this way, you don't have to call setPeer yourself
    public IncludeEnd(IncludeStart start) {
        super(start.getParseNode());
        start.setPeer(this);
        this.file = start.getContainingFile();
        this.peer = start;
    }

//  GET ****************************************************************************

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    public boolean isPeer(AbstractCfgNode node) {
        return node == this.peer;
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
    }
}
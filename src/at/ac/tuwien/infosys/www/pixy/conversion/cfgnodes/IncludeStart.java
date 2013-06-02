package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Indicates the start of an included section (inserted during include resolution).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class IncludeStart extends AbstractCfgNode {
    private File containingFile;  // file in which this node occurs
    private IncludeEnd peer;

    public IncludeStart(File file, ParseNode parseNode) {
        super(parseNode);
        this.containingFile = file;
        this.peer = null;
    }

    public File getContainingFile() {
        return this.containingFile;
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    public void replaceVariable(int index, Variable replacement) {
    }

    public void setPeer(IncludeEnd peer) {
        this.peer = peer;
    }
}
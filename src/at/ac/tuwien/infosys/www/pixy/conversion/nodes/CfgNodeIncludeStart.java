package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import java.io.File;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

// *********************************************************************************
// CfgNodeIncludeStart *************************************************************
// *********************************************************************************

// indicates the start of an included section (inserted during include resolution)
/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeIncludeStart
    extends CfgNode {

    private File containingFile;  // file in which this node occurs
    private CfgNodeIncludeEnd peer;

//  CONSTRUCTORS *******************************************************************

    public CfgNodeIncludeStart(File file, ParseNode parseNode) {
        super(parseNode);
        this.containingFile = file;
        this.peer = null;
    }

//  GET ****************************************************************************

    public File getContainingFile() {
        return this.containingFile;
    }

    public List<Variable> getVariables() {
        return Collections.emptyList();
    }

    public CfgNodeIncludeEnd getPeer() {
        return this.peer;
    }

//  SET ****************************************************************************

    public void replaceVariable(int index, Variable replacement) {
    }

    public void setPeer(CfgNodeIncludeEnd peer) {
        this.peer = peer;
    }
}
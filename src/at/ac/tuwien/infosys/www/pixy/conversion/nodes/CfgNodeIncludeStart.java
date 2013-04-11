package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.*;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.io.File;
import java.util.*;

// *********************************************************************************
// CfgNodeIncludeStart *************************************************************
// *********************************************************************************

// indicates the start of an included section (inserted during include resolution)
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
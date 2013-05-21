package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.BuiltinFunctions;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

/**
 * For builtin functions.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepGraphOpNode extends DepGraphNode {
    private CfgNode cfgNode;
    private String name;
    private boolean builtin;    // builtin function?

//  ********************************************************************************

    DepGraphOpNode(CfgNode cfgNode, String name, boolean builtin) {
        //this.place = place;
        this.cfgNode = cfgNode;
        this.name = name;
        this.builtin = builtin;
    }

//  ********************************************************************************

    // returns a name that can be used in dot file representation
    public String dotName() {
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

//  ********************************************************************************

    // returns a name that can be used in dot file representation
    public String comparableName() {
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

//  ********************************************************************************

    public String dotNameShort() {
        String fileName = this.cfgNode.getFileName();
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + fileName.substring(fileName.lastIndexOf('/') + 1);
    }

//  ********************************************************************************

    public String dotNameShortest() {
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOrigLineno() + ")";
    }

//  ********************************************************************************

    public String dotNameVerbose(boolean isModelled) {

        String retme = "";

        if (!MyOptions.optionW) {
            // don't print file name for web interface
            retme += this.cfgNode.getFileName() + " : " + this.cfgNode.getOrigLineno() + "\\n";
        }

        if (BuiltinFunctions.isBuiltinFunction(this.name) ||
            TacOperators.isOp(this.name)) {
            retme +=
                "builtin function:\\n" +
                    Dumper.escapeDot(this.name, 0) + "\\n";
        } else {
            retme +=
                "unresolved function:\\n" +
                    Dumper.escapeDot(this.name, 0) + "\\n";
        }

        if (!isModelled) {
            retme += "(unmodeled)\\n";
        }

        return retme;
    }

//  ********************************************************************************

    public String getName() {
        return this.name;
    }

//  ********************************************************************************

    public boolean isBuiltin() {
        return this.builtin;
    }

//  ********************************************************************************

    public CfgNode getCfgNode() {
        return this.cfgNode;
    }

//  ********************************************************************************

    public int getLine() {
        return this.cfgNode.getOrigLineno();
    }

//  ********************************************************************************

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        return hashCode;
    }

//  ********************************************************************************

    public boolean equals(Object compX) {

        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DepGraphOpNode)) {
            return false;
        }
        DepGraphOpNode comp = (DepGraphOpNode) compX;

        return this.cfgNode.equals(comp.cfgNode);
    }
}
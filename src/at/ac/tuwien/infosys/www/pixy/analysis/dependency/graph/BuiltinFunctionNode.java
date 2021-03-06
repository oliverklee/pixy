package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.BuiltinFunctions;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * For builtin functions.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class BuiltinFunctionNode extends AbstractNode {
    private AbstractCfgNode cfgNode;
    private String name;

    /** builtin function? */
    private boolean builtin;

    BuiltinFunctionNode(AbstractCfgNode cfgNode, String name, boolean builtin) {
        this.cfgNode = cfgNode;
        this.name = name;
        this.builtin = builtin;
    }

    /**
     * Returns a name that can be used in dot file representation.
     *
     * @return
     */
    public String dotName() {
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

    /**
     * Returns a name that can be used in dot file representation.
     *
     * @return
     */
    public String comparableName() {
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

    public String dotNameShort() {
        String fileName = this.cfgNode.getFileName();
        return "OP: " + Dumper.escapeDot(this.name, 0) + " (" + this.cfgNode.getOriginalLineNumber() + ")" +
            "\\n" + fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public String dotNameVerbose(boolean isModelled) {
        String retme = "";

        if (!MyOptions.optionW) {
            // don't print file name for web interface
            retme += this.cfgNode.getFileName() + " : " + this.cfgNode.getOriginalLineNumber() + "\\n";
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

    public String getName() {
        return this.name;
    }

    public boolean isBuiltin() {
        return this.builtin;
    }

    public AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

    public int getLine() {
        return this.cfgNode.getOriginalLineNumber();
    }

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        return hashCode;
    }

    public boolean equals(Object compX) {
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof BuiltinFunctionNode)) {
            return false;
        }
        BuiltinFunctionNode comp = (BuiltinFunctionNode) compX;

        return this.cfgNode.equals(comp.cfgNode);
    }
}
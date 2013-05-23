package at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class NormalNode extends AbstractNode {
    private AbstractTacPlace place;
    private AbstractCfgNode cfgNode;
    private boolean isTainted;

//  ********************************************************************************

    public NormalNode(AbstractTacPlace place, AbstractCfgNode cfgNode) {
        this.place = place;
        this.cfgNode = cfgNode;
        this.isTainted = false;
    }

//  ********************************************************************************

    // returns a name that can be used in dot file representation
    public String dotName() {
        return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

    public String comparableName() {
        return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + this.cfgNode.getFileName();
    }

    // no path
    public String dotNameShort() {
        String fileName = this.cfgNode.getFileName();
        return Dumper.escapeDot(this.place.toString(), 0) + " (" + this.cfgNode.getOrigLineno() + ")" +
            "\\n" + fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public String dotNameVerbose(boolean isModelled) {

        String retme = "";

        if (!MyOptions.optionW) {
            // don't print file name for web interface
            retme += this.cfgNode.getFileName() + " : " + this.cfgNode.getOrigLineno() + "\\n";
        } else {
            retme += "Line " + this.cfgNode.getOrigLineno() + "\\n";
        }

        if (this.place instanceof Variable) {
            Variable var = (Variable) this.place;
            retme += "Var: " + Dumper.escapeDot(var.getName(), 0) + "\\n";
            retme += "Func: " + Dumper.escapeDot(var.getSymbolTable().getName(), 0) + "\\n";
        } else if (this.place instanceof Constant) {
            retme += "Const: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
        } else if (this.place instanceof Literal) {
            retme += "Lit: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
        } else {
            throw new RuntimeException("SNH");
        }

        return retme;
    }

//  ********************************************************************************

    public boolean isString() {
        return this.place.isLiteral();
    }

//  ********************************************************************************

    public AbstractTacPlace getPlace() {
        return this.place;
    }

//  ********************************************************************************

    public AbstractCfgNode getCfgNode() {
        return this.cfgNode;
    }

//  ********************************************************************************

    public int getLine() {
        return this.cfgNode.getOrigLineno();
    }

//  ********************************************************************************

    public boolean equals(Object compX) {

        if (compX == this) {
            return true;
        }
        if (!(compX instanceof NormalNode)) {
            return false;
        }
        NormalNode comp = (NormalNode) compX;

        return this.place.equals(comp.place) && this.cfgNode.equals(comp.cfgNode);
    }

//  ********************************************************************************

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.place.hashCode();
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        return hashCode;
    }

//  ********************************************************************************

    public String toString() {
        return this.place.toString() + " (" + this.cfgNode.getOrigLineno() + ") " +
            this.cfgNode.getFileName();
    }
}
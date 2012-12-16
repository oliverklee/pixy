package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import at.ac.tuwien.infosys.www.pixy.Dumper;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

public class DepGraphNormalNode 
extends DepGraphNode {

    private TacPlace place;
    private CfgNode cfgNode;
    private boolean isTainted;
    
//  ********************************************************************************
    
    public DepGraphNormalNode(TacPlace place, CfgNode cfgNode) {
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
        "\\n" + fileName.substring(fileName.lastIndexOf('/')+1);
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
        } else if (this.place instanceof Constant){
            retme += "Const: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
        } else if (this.place instanceof Literal){
            retme += "Lit: " + Dumper.escapeDot(this.place.toString(), 0) + "\\n";
        } else {
            throw new RuntimeException("SNH");
        }
        
        return retme;
    }
    
    

//  ********************************************************************************
    
    public void setTainted(boolean isTainted) {
        this.isTainted = isTainted;
    }

//  ********************************************************************************
    
    public boolean isTainted() {
        return this.isTainted;
    }

//  ********************************************************************************
    
    public boolean isString() {
        if (this.place.isLiteral()) {
            return true;
        } else {
            return false;
        }
    }
    
//  ********************************************************************************
    
    public TacPlace getPlace() {
        return this.place;
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
    
    public boolean equals(Object compX) {
        
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DepGraphNormalNode)) {
            return false;
        }
        DepGraphNormalNode comp = (DepGraphNormalNode) compX;

        if (!this.place.equals(comp.place)) {
            return false;
        }
        if (!this.cfgNode.equals(comp.cfgNode)) {
            return false;
        }

        return true;
    }

//  ********************************************************************************
    
    public int hashCode() {
        int hashCode = 17;
        hashCode = 37*hashCode + this.place.hashCode();
        hashCode = 37*hashCode + this.cfgNode.hashCode();
        return hashCode;
    }
    
//  ********************************************************************************
    
    public String toString() {
        return this.place.toString() + " (" + this.cfgNode.getOrigLineno() + ") " +
            this.cfgNode.getFileName();
    }

}

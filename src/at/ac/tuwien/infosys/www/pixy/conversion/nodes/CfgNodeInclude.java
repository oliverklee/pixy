package at.ac.tuwien.infosys.www.pixy.conversion.nodes;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * "temp = include <place>" (can also be require or *_once).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CfgNodeInclude
    extends CfgNode
    implements Comparable<CfgNodeInclude> {

    private Variable temp;
    private TacPlace includeMe;
    private File file;  // file in which this node occurs
    private TacFunction includeFunction; // function in which this node occurs

//  CONSTRUCTORS *******************************************************************

    public CfgNodeInclude(TacPlace temp, TacPlace includeMe,
                          File file, TacFunction includeFunction, ParseNode parseNode) {
        super(parseNode);
        this.temp = (Variable) temp;
        this.includeMe = includeMe;
        this.file = file;
        this.includeFunction = includeFunction;
    }

//  GET ****************************************************************************

    public TacPlace getTemp() {
        return this.temp;
    }

    public TacPlace getIncludeMe() {
        return this.includeMe;
    }

    public File getFile() {
        return this.file;
    }

    public TacFunction getIncludeFunction() {
        return this.includeFunction;
    }

    // is the file to be included given by a simple literal?
    public boolean isLiteral() {
        return this.includeMe.isLiteral();
    }

    public List<Variable> getVariables() {
        List<Variable> retMe = new LinkedList<Variable>();
        retMe.add(this.temp);
        if (this.includeMe instanceof Variable) {
            retMe.add((Variable) this.includeMe);
        } else {
            retMe.add(null);
        }
        return retMe;
    }

//  SET ****************************************************************************

    // use this function if this include node is inside the main function and
    // is inlined into some other file and function
    public void setIncludeFunction(TacFunction function) {
        this.includeFunction = function;
    }

    public void replaceVariable(int index, Variable replacement) {
        switch (index) {
            case 0:
                this.temp = replacement;
                break;
            case 1:
                this.includeMe = replacement;
                break;
            default:
                throw new RuntimeException("SNH");
        }
    }

    public int compareTo(CfgNodeInclude o) {
        if (o == this) {
            return 0;
        }
        CfgNodeInclude comp = o;
        int fileComp = this.file.compareTo(comp.file);
        if (fileComp != 0) {
            return fileComp;
        } else {
            return new Integer(this.getOrigLineno()).compareTo(comp.getOrigLineno());
        }
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepos;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// corresponds to one dependency label, consisting of
// <source descriptor (e.g., variable or function name)>, <location (cfg node)>
public class Dep
    implements Recyclable {

    public static GenericRepos<Dep> repos =
        new GenericRepos<Dep>();

    // special, parameterized label
    public static final Dep UNINIT =
        new Dep(null);

    private CfgNode cfgNode;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ********************************************************************************

    private Dep(CfgNode cfgNode) {
        this.cfgNode = cfgNode;
    }

//  ********************************************************************************

    public static Dep create(CfgNode cfgNode) {
        Dep ret = new Dep(cfgNode);
        ret = repos.recycle(ret);
        return ret;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    public CfgNode getCfgNode() {
        return this.cfgNode;
    }

//  ********************************************************************************

    public String toString() {
        if (this == UNINIT) {
            return " <uninit> ";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(" (");
        if (this.cfgNode == null) {
            buf.append("none");
        } else {
            buf.append(this.cfgNode.getOrigLineno());
        }
        buf.append(", ");
        buf.append(this.cfgNode.toString());
        buf.append(") ");
        return buf.toString();
    }

//  ********************************************************************************

    // only needed by JUnit tests
    public boolean contains(int line) {
        if (this.cfgNode == null) {
            return false;
        }
        return (this.cfgNode.getOrigLineno() == line);
    }

//  ********************************************************************************

    public boolean structureEquals(Object compX) {
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof Dep)) {
            return false;
        }
        Dep comp = (Dep) compX;
        if (this.cfgNode == null) {
            return this == comp;
        }
        if (!this.cfgNode.equals(comp.cfgNode)) {
            return false;
        }
        return true;
    }

//  ********************************************************************************

    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        return hashCode;
    }
}
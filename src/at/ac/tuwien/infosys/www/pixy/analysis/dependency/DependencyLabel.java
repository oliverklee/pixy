package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

/**
 * Corresponds to one dependency label, consisting of
 * <source descriptor (e.g., variable or function name)>, <location (cfg node)>.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DependencyLabel implements Recyclable {
    public static GenericRepository<DependencyLabel> repos =
        new GenericRepository<>();

    // special, parameterized label
    public static final DependencyLabel UNINIT =
        new DependencyLabel(null);

    private AbstractCfgNode cfgNode;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

//  ********************************************************************************

    private DependencyLabel(AbstractCfgNode cfgNode) {
        this.cfgNode = cfgNode;
    }

//  ********************************************************************************

    public static DependencyLabel create(AbstractCfgNode cfgNode) {
        DependencyLabel ret = new DependencyLabel(cfgNode);
        ret = repos.recycle(ret);
        return ret;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  ********************************************************************************

    public AbstractCfgNode getCfgNode() {
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
        return this.cfgNode != null && (this.cfgNode.getOrigLineno() == line);
    }

//  ********************************************************************************

    public boolean structureEquals(Object compX) {
        if (compX == this) {
            return true;
        }
        if (!(compX instanceof DependencyLabel)) {
            return false;
        }
        DependencyLabel comp = (DependencyLabel) compX;
        if (this.cfgNode == null) {
            return this == comp;
        }
        return this.cfgNode.equals(comp.cfgNode);
    }

//  ********************************************************************************

    public int structureHashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + this.cfgNode.hashCode();
        return hashCode;
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class DependencyLabel implements Recyclable {

	public static GenericRepository<DependencyLabel> repos = new GenericRepository<DependencyLabel>();

	public static final DependencyLabel UNINIT = new DependencyLabel(null);

	private AbstractCfgNode cfgNode;

	private DependencyLabel(AbstractCfgNode cfgNode) {
		this.cfgNode = cfgNode;
	}

	public static DependencyLabel create(AbstractCfgNode cfgNode) {
		DependencyLabel ret = new DependencyLabel(cfgNode);
		ret = repos.recycle(ret);
		return ret;
	}

	public AbstractCfgNode getCfgNode() {
		return this.cfgNode;
	}

	public String toString() {
		if (this == UNINIT) {
			return " <uninit> ";
		}
		StringBuilder buf = new StringBuilder();
		buf.append(" (");
		if (this.cfgNode == null) {
			buf.append("none");
		} else {
			buf.append(this.cfgNode.getOriginalLineNumber());
		}
		buf.append(", ");
		buf.append(this.cfgNode.toString());
		buf.append(") ");
		return buf.toString();
	}

	public boolean contains(int line) {
		if (this.cfgNode == null) {
			return false;
		}
		return (this.cfgNode.getOriginalLineNumber() == line);
	}

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
		if (!this.cfgNode.equals(comp.cfgNode)) {
			return false;
		}
		return true;
	}

	public int structureHashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.cfgNode.hashCode();
		return hashCode;
	}

}

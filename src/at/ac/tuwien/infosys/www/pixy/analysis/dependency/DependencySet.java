package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.GenericRepository;
import at.ac.tuwien.infosys.www.pixy.analysis.Recyclable;

public class DependencySet implements Recyclable {

	public static GenericRepository<DependencySet> repos = new GenericRepository<DependencySet>();

	static public final DependencySet UNINIT = new DependencySet(DependencyLabel.UNINIT);
	static {
		repos.recycle(UNINIT);
	}

	private Set<DependencyLabel> depSet;

	private DependencySet() {
		this.depSet = new HashSet<DependencyLabel>();
	}

	private DependencySet(DependencyLabel dep) {
		this.depSet = new HashSet<DependencyLabel>();
		this.depSet.add(dep);
	}

	private DependencySet(Set<DependencyLabel> depSet) {
		this.depSet = depSet;
	}

	public static DependencySet create(Set<DependencyLabel> depSet) {
		DependencySet x = new DependencySet(depSet);
		return repos.recycle(x);
	}

	public static DependencySet create(DependencyLabel dep) {
		Set<DependencyLabel> taintSet = new HashSet<DependencyLabel>();
		taintSet.add(dep);
		return create(taintSet);
	}

	public static DependencySet lub(DependencySet a, DependencySet b) {
		Set<DependencyLabel> resultSet = new HashSet<DependencyLabel>();
		resultSet.addAll(a.depSet);
		resultSet.addAll(b.depSet);
		return DependencySet.create(resultSet);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (DependencyLabel element : this.depSet) {
			buf.append(element.toString());
		}
		return buf.toString();
	}

	public Set<DependencyLabel> getDepSet() {
		return new HashSet<DependencyLabel>(this.depSet);
	}

	public boolean structureEquals(Object compX) {

		if (compX == this) {
			return true;
		}
		if (!(compX instanceof DependencySet)) {
			return false;
		}
		DependencySet comp = (DependencySet) compX;

		if (!this.depSet.equals(comp.depSet)) {
			return false;
		}

		return true;
	}

	public int structureHashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.depSet.hashCode();
		return hashCode;
	}

}

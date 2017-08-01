package at.ac.tuwien.infosys.www.pixy.analysis;

public abstract class AbstractLatticeElement implements Recyclable {

	public abstract void lub(AbstractLatticeElement element);

	public abstract AbstractLatticeElement cloneMe();

	public abstract boolean structureEquals(Object compX);

	public abstract int structureHashCode();

	public abstract void dump();
}

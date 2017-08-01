package at.ac.tuwien.infosys.www.pixy.analysis;

public interface Recyclable {

	public abstract boolean structureEquals(Object compX);

	public abstract int structureHashCode();

}

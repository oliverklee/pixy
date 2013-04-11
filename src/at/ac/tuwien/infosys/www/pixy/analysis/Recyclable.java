package at.ac.tuwien.infosys.www.pixy.analysis;

// interface for recyclable objects;
// for an explanation, see GenericRepos
public interface Recyclable {

    // structural comparison rather than physical (==);
    // only to be used by GenericRepos
    public abstract boolean structureEquals(Object compX);
    public abstract int structureHashCode();
}
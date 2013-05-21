package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * Interface for recyclable objects.
 *
 * For an explanation, see GenericRepos.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public interface Recyclable {
    // structural comparison rather than physical (==);
    // only to be used by GenericRepos
    public abstract boolean structureEquals(Object compX);

    public abstract int structureHashCode();
}
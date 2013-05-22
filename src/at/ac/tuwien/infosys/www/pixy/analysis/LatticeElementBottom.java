package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LatticeElementBottom extends AbstractLatticeElement {
    public static final LatticeElementBottom INSTANCE = new LatticeElementBottom();

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // singleton class
    private LatticeElementBottom() {
    }

    public AbstractLatticeElement cloneMe() {
        throw new RuntimeException("SNH");
    }

    public void lub(AbstractLatticeElement element) {
        throw new RuntimeException("SNH");
    }

    public boolean structureEquals(Object compX) {
        throw new RuntimeException("SNH");
    }

    public int structureHashCode() {
        throw new RuntimeException("SNH");
    }

    public void dump() {
        System.out.println("<bottom>");
    }
}
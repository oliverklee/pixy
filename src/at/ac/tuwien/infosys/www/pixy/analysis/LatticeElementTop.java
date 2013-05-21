package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class LatticeElementTop extends LatticeElement {
    public static final LatticeElementTop INSTANCE = new LatticeElementTop();

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // singleton class
    private LatticeElementTop() {
    }

    public void lub(LatticeElement element) {
        throw new RuntimeException("SNH");
    }

    public LatticeElement cloneMe() {
        throw new RuntimeException("SNH");
    }

    public boolean structureEquals(Object compX) {
        throw new RuntimeException("SNH");
    }

    public int structureHashCode() {
        throw new RuntimeException("SNH");
    }

    public void dump() {
        System.out.println("<top>");
    }
}
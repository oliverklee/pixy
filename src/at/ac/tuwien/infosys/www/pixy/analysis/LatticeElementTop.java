package at.ac.tuwien.infosys.www.pixy.analysis;


public class LatticeElementTop
extends LatticeElement {

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


    /*
    public boolean equals(Object obj) {
        // since this is a singleton class, it can only be equal to itself
        return (obj == LatticeElementTop.INSTANCE ? true : false);
    }

    public int hashCode() {
        // use the object's "natural" hashcode:
        // this doesn't work
        return (((Object) this).hashCode());
    }
    */

}

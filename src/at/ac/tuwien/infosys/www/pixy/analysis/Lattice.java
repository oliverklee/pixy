package at.ac.tuwien.infosys.www.pixy.analysis;



// this is, in fact, a semi-lattice
public abstract class Lattice {
    
    // bottom element of this lattice
    protected final LatticeElement bottom = LatticeElementBottom.INSTANCE;

// *********************************************************************************    
// GET *****************************************************************************
// *********************************************************************************    
    
    public LatticeElement getBottom() {
        return this.bottom;
    }

// *********************************************************************************    
// OTHER ***************************************************************************
// *********************************************************************************    
    
    public abstract LatticeElement lub(
        LatticeElement incomingElement, 
        LatticeElement targetElement);
    
}



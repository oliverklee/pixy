package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * This is, in fact, a semi-lattice.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
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
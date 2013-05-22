package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * This is, in fact, a semi-lattice.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractLattice {
    // bottom element of this lattice
    protected final AbstractLatticeElement bottom = LatticeElementBottom.INSTANCE;

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public AbstractLatticeElement getBottom() {
        return this.bottom;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public abstract AbstractLatticeElement lub(
        AbstractLatticeElement incomingElement,
        AbstractLatticeElement targetElement);
}
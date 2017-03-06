package at.ac.tuwien.infosys.www.pixy.analysis;

public abstract class AbstractLattice {

	protected final AbstractLatticeElement bottom = LatticeElementBottom.INSTANCE;

	public AbstractLatticeElement getBottom() {
		return this.bottom;
	}

	public abstract AbstractLatticeElement lub(AbstractLatticeElement incomingElement,
			AbstractLatticeElement targetElement);

}

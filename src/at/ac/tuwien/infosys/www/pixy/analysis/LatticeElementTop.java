package at.ac.tuwien.infosys.www.pixy.analysis;

public class LatticeElementTop extends AbstractLatticeElement {

	public static final LatticeElementTop INSTANCE = new LatticeElementTop();

	private LatticeElementTop() {
	}

	public void lub(AbstractLatticeElement element) {
		throw new RuntimeException("SNH");
	}

	public AbstractLatticeElement cloneMe() {
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

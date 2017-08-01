package at.ac.tuwien.infosys.www.pixy.analysis;

public class LatticeElementBottom extends AbstractLatticeElement {

	public static final LatticeElementBottom INSTANCE = new LatticeElementBottom();

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

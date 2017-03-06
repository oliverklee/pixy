package at.ac.tuwien.infosys.www.pixy.conversion;

public class TacActualParameter {

	private AbstractTacPlace place;
	private boolean isReference;

	public TacActualParameter(AbstractTacPlace place, boolean isReference) {
		this.place = place;
		this.isReference = isReference;
	}

	public AbstractTacPlace getPlace() {
		return this.place;
	}

	public boolean isReference() {
		return this.isReference;
	}

	public void setPlace(AbstractTacPlace place) {
		this.place = place;
	}

}

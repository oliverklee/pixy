package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.List;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;

import java.util.LinkedList;

public class SinkProblem {

	AbstractTacPlace place;
	List<?> callNodes;

	public SinkProblem(AbstractTacPlace place) {
		this.place = place;
		this.callNodes = new LinkedList<Object>();
	}

	public void setCallList(List<?> callNodes) {
		this.callNodes = callNodes;
	}

	public AbstractTacPlace getPlace() {
		return this.place;
	}

	public List<?> getCallNodes() {
		return this.callNodes;
	}

}

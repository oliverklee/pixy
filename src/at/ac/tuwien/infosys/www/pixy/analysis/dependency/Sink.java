package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Sink implements Comparable<Sink> {

	private List<AbstractTacPlace> sensitivePlaces;
	private AbstractCfgNode cfgNode;
	private int lineNo;

	public TacFunction function;

	public Sink(AbstractCfgNode cfgNode, TacFunction function) {
		this.cfgNode = cfgNode;
		this.sensitivePlaces = new LinkedList<AbstractTacPlace>();
		this.lineNo = -1;
		this.function = function;
	}

	AbstractCfgNode getNode() {
		return this.cfgNode;
	}

	public int getLineNo() {
		if (this.lineNo == -1) {
			this.lineNo = this.cfgNode.getOriginalLineNumber();
		}
		return this.lineNo;
	}

	String getFileName() {
		return this.cfgNode.getFileName();
	}

	TacFunction getFunction() {
		return this.function;
	}

	public void addSensitivePlace(AbstractTacPlace place) {
		this.sensitivePlaces.add(place);
	}

	List<SinkProblem> getSinkProblems() {

		List<SinkProblem> problems = new LinkedList<SinkProblem>();

		for (Iterator<AbstractTacPlace> sensIter = this.sensitivePlaces.iterator(); sensIter.hasNext();) {
			AbstractTacPlace sensitivePlace = (AbstractTacPlace) sensIter.next();

			List<AbstractCfgNode> calledBy = new LinkedList<AbstractCfgNode>();
			SinkProblem problem = new SinkProblem(sensitivePlace);
			problem.setCallList(calledBy);
			problems.add(problem);
		}

		return problems;
	}

	public int compareTo(Sink comp) {
		int myLineNo = this.getLineNo();
		int compLineNo = comp.getLineNo();
		if (myLineNo < compLineNo) {
			return -1;
		} else if (myLineNo == compLineNo) {
			return 0;
		} else {
			return 1;
		}
	}

}

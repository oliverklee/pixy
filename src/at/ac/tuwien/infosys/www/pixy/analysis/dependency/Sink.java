package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a sink.
 *
 * Note: This class has a natural ordering that is inconsistent with equals().
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Sink implements Comparable<Sink> {
    /**
     * a list of sensitive places (i.e., places from which this sink uses data)
     */
    private List<AbstractTacPlace> sensitivePlaces;
    private AbstractCfgNode cfgNode;
    private int lineNumber = -1;

    /** function containing this sink */
    private TacFunction function;

    public Sink(AbstractCfgNode cfgNode, TacFunction function) {
        this.cfgNode = cfgNode;
        this.sensitivePlaces = new LinkedList<>();
        this.function = function;
    }

    AbstractCfgNode getNode() {
        return this.cfgNode;
    }

    public int getLineNumber() {
        if (this.lineNumber == -1) {
            this.lineNumber = this.cfgNode.getOriginalLineNumber();
        }
        return this.lineNumber;
    }

    public void addSensitivePlace(AbstractTacPlace place) {
        this.sensitivePlaces.add(place);
    }

    /**
     * Returns a list of SinkProblems for each sensitive place in this sink.
     *
     * @return
     */
    List<SinkProblem> getSinkProblems() {
        List<SinkProblem> problems = new LinkedList<>();

        for (AbstractTacPlace sensitivePlace : this.sensitivePlaces) {
            /** list of call nodes that call the function containing the sink */
            List<AbstractCfgNode> calledBy = new LinkedList<>();

            SinkProblem problem = new SinkProblem(sensitivePlace);
            problem.setCallList(calledBy);
            problems.add(problem);
        }

        return problems;
    }

    /**
     * Comparison regarding line number.
     *
     * @param otherSink
     *
     * @return
     */
    public int compareTo(Sink otherSink) {
        int myLineNo = this.getLineNumber();
        int compLineNo = otherSink.getLineNumber();
        if (myLineNo < compLineNo) {
            return -1;
        } else if (myLineNo == compLineNo) {
            return 0;
        } else {
            return 1;
        }
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Note: This class has a natural ordering that is inconsistent with equals().
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Sink implements Comparable<Sink> {
    /**
     * a list of sensitive places (i.e., places for which we want to get dependency graphs)
     */
    private List<AbstractTacPlace> sensitivePlaces;
    private AbstractCfgNode cfgNode;
    private int lineNo;

    /** function containing this sink */
    private TacFunction function;

    public Sink(AbstractCfgNode cfgNode, TacFunction function) {
        this.cfgNode = cfgNode;
        this.sensitivePlaces = new LinkedList<>();
        this.lineNo = -1;
        this.function = function;
    }

    AbstractCfgNode getNode() {
        return this.cfgNode;
    }

    public int getLineNo() {
        if (this.lineNo == -1) {
            this.lineNo = this.cfgNode.getOrigLineno();
        }
        return this.lineNo;
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
        int myLineNo = this.getLineNo();
        int compLineNo = otherSink.getLineNo();
        if (myLineNo < compLineNo) {
            return -1;
        } else if (myLineNo == compLineNo) {
            return 0;
        } else {
            return 1;
        }
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// note: this class has a natural ordering that is inconsistent with equals()
public class Sink
implements Comparable<Sink> {

    // a list of sensitive places
    // (i.e. places for which we want to get dependency graphs)
    private List<TacPlace> sensitivePlaces;
    private CfgNode cfgNode;
    private int lineNo;

    // function containing this sink
    private TacFunction function;

//  ********************************************************************************
//  CONSTRUCTORS *******************************************************************
//  ********************************************************************************

    public Sink(CfgNode cfgNode, TacFunction function) {
        this.cfgNode = cfgNode;
        this.sensitivePlaces = new LinkedList<TacPlace>();
        this.lineNo = -1;
        this.function = function;
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

    CfgNode getNode() {
        return this.cfgNode;
    }

    public int getLineNo() {
        if (this.lineNo == -1) {
            this.lineNo = this.cfgNode.getOrigLineno();
        }
        return this.lineNo;
    }

    String getFileName() {
        return this.cfgNode.getFileName();
    }

    TacFunction getFunction() {
        return this.function;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

    public void addSensitivePlace(TacPlace place) {
        this.sensitivePlaces.add(place);
    }

    // returns a list of SinkProblems for each sensitive place in this sink
    List<SinkProblem> getSinkProblems() {

        // to be returned
        List<SinkProblem> problems = new LinkedList<SinkProblem>();

        // for each sensitive place
        for (Iterator sensIter = this.sensitivePlaces.iterator(); sensIter.hasNext(); ) {
            TacPlace sensitivePlace = (TacPlace) sensIter.next();

            // list of CallNode's that call the function containing the sink
            List<CfgNode> calledBy = new LinkedList<CfgNode>();

            SinkProblem problem = new SinkProblem(sensitivePlace);
            problem.setCallList(calledBy);
            problems.add(problem);
        }

        return problems;
    }

    // comparison regarding line number
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
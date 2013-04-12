package at.ac.tuwien.infosys.www.pixy.analysis.dep;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;

public class SinkProblem {

    // a place for which we want to know something
    TacPlace place;

    // nodes from which the function containing the
    // sensitive sink is called
    List callNodes;

    public SinkProblem(TacPlace place) {
        this.place = place;
        this.callNodes = new LinkedList();
    }

    public void setCallList(List callNodes) {
        this.callNodes = callNodes;
    }

    public TacPlace getPlace() {
        return this.place;
    }

    public List getCallNodes() {
        return this.callNodes;
    }
}
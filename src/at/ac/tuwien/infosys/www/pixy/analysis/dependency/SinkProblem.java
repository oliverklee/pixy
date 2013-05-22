package at.ac.tuwien.infosys.www.pixy.analysis.dependency;

import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class SinkProblem {
    // a place for which we want to know something
    AbstractTacPlace place;

    // nodes from which the function containing the
    // sensitive sink is called
    List<AbstractCfgNode> callNodes;

    public SinkProblem(AbstractTacPlace place) {
        this.place = place;
        this.callNodes = new LinkedList<>();
    }

    public void setCallList(List<AbstractCfgNode> callNodes) {
        this.callNodes = callNodes;
    }

    public AbstractTacPlace getPlace() {
        return this.place;
    }

    public List<AbstractCfgNode> getCallNodes() {
        return this.callNodes;
    }
}
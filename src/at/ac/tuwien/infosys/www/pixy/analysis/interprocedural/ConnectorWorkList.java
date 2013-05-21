package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.LinkedList;

/**
 * Worklist for connector computation.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class ConnectorWorkList {
    private LinkedList<ConnectorWorkListElement> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    ConnectorWorkList() {
        this.workList = new LinkedList<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(ConnectorWorkListElement element) {
        this.workList.add(element);
    }

    // actually implemented as FIFO
    ConnectorWorkListElement removeNext() {
        return this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0);
    }
}
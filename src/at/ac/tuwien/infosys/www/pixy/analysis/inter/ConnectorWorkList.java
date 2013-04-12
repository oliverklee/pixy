package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.LinkedList;

// worklist for connector computation
public final class ConnectorWorkList {

    private LinkedList<ConnectorWorkListElement> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    ConnectorWorkList() {
        this.workList = new LinkedList<ConnectorWorkListElement>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(ConnectorWorkListElement element) {
        this.workList.add(element);
    }

    // actually implemented as FIFO
    ConnectorWorkListElement removeNext() {
        return (ConnectorWorkListElement) this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0 ? true : false);
    }
}
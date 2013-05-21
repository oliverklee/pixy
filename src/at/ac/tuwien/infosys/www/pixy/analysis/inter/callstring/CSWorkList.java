package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class CSWorkList {
    private LinkedList<CSWorkListElement> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    CSWorkList() {
        this.workList = new LinkedList<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(CSWorkListElement element) {
        this.workList.add(element);
    }

    // actually implemented as FIFO
    CSWorkListElement removeNext() {
        return this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0);
    }
}
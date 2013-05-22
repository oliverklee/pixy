package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class CallStringWorklist {
    private LinkedList<CallStringWorklistElement> workList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    CallStringWorklist() {
        this.workList = new LinkedList<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    void add(CallStringWorklistElement element) {
        this.workList.add(element);
    }

    // actually implemented as FIFO
    CallStringWorklistElement removeNext() {
        return this.workList.removeFirst();
    }

    boolean hasNext() {
        return (this.workList.size() > 0);
    }
}
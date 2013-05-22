package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This work list contains two lists: one for cfg nodes that have been
 * assigned a reverse pre-order, and one for nodes without such an order.
 *
 * If it contains nodes without order, these nodes are returned first
 * (queued, FIFO). If there are no nodes without order, the ordered
 * nodes are returned accordingly.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class InterproceduralWorklistPoor implements InterproceduralWorklist {
    private LinkedList<InterproceduralWorklistElement> unsortedWorkList;
    private SortedMap<Integer, InterproceduralWorklistElement> sortedWorkList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public InterproceduralWorklistPoor() {
        this.unsortedWorkList = new LinkedList<>();
        this.sortedWorkList = new TreeMap<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public void add(AbstractCfgNode cfgNode, AbstractContext context) {
        // null contexts are not allowed
        if (context == null) {
            throw new RuntimeException("SNH");
        }
        InterproceduralWorklistElement element = new InterproceduralWorklistElement(cfgNode, context);

        int rpo = cfgNode.getReversePostOrder();
        if (rpo == -1) {
            this.unsortedWorkList.add(element);
        } else {
            this.sortedWorkList.put(rpo, element);
        }
    }

    public InterproceduralWorklistElement removeNext() {
        if (!this.unsortedWorkList.isEmpty()) {
            return this.unsortedWorkList.removeFirst();
        } else {
            Integer key = this.sortedWorkList.firstKey();
            return this.sortedWorkList.remove(key);
        }
    }

    public boolean hasNext() {
        return !(this.unsortedWorkList.isEmpty() && this.sortedWorkList.isEmpty());
    }
}
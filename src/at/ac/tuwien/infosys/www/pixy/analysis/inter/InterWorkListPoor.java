package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

/*
 * this work list contains two lists: one for cfg nodes that have been
 * assigned a reverse preorder, and one for nodes without such an order;
 * if it contains nodes without order, these nodes are returned first
 * (queued, FIFO); if there are no nodes without order, the ordered
 * nodes are returned accordingly
 */
public final class InterWorkListPoor
    implements InterWorkList {

    private LinkedList<InterWorkListElement> unsortedWorkList;
    private SortedMap<Integer, InterWorkListElement> sortedWorkList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public InterWorkListPoor() {
        this.unsortedWorkList = new LinkedList<InterWorkListElement>();
        this.sortedWorkList = new TreeMap<Integer, InterWorkListElement>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public void add(CfgNode cfgNode, Context context) {
        // null contexts are not allowed
        if (context == null) {
            throw new RuntimeException("SNH");
        }
        InterWorkListElement element = new InterWorkListElement(cfgNode, context);

        int rpo = cfgNode.getReversePostOrder();
        if (rpo == -1) {
            this.unsortedWorkList.add(element);
        } else {
            this.sortedWorkList.put(rpo, element);
        }

    }

    public InterWorkListElement removeNext() {
        if (!this.unsortedWorkList.isEmpty()) {
            return this.unsortedWorkList.removeFirst();
        } else {
            Integer key = this.sortedWorkList.firstKey();
            return this.sortedWorkList.remove(key);
        }
    }

    public boolean hasNext() {
        if (this.unsortedWorkList.isEmpty() && this.sortedWorkList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNode;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This interprocedural worklist uses a better order (interprocedural reverse post-order).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class InterWorkListBetter implements InterWorkList {
    private InterWorkListOrder order;
    private SortedMap<Integer, InterWorkListElement> sortedWorkList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public InterWorkListBetter(InterWorkListOrder order) {
        this.order = order;
        this.sortedWorkList = new TreeMap<>();
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

        Integer rpo = this.order.getReversePostOrder(element);
        if (rpo == null) {
            throw new RuntimeException("SNH");
        }
        this.sortedWorkList.put(rpo, element);
    }

    public InterWorkListElement removeNext() {
        Integer key = this.sortedWorkList.firstKey();
        return this.sortedWorkList.remove(key);
    }

    public boolean hasNext() {
        return !this.sortedWorkList.isEmpty();
    }
}
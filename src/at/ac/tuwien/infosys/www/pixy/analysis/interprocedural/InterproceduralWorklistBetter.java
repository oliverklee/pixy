package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This interprocedural worklist uses a better order (interprocedural reverse post-order).
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class InterproceduralWorklistBetter implements InterproceduralWorklist {
    private InterproceduralWorklistOrder order;
    private SortedMap<Integer, InterproceduralWorklistElement> sortedWorkList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public InterproceduralWorklistBetter(InterproceduralWorklistOrder order) {
        this.order = order;
        this.sortedWorkList = new TreeMap<>();
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public void add(AbstractCfgNode cfgNode, Context context) {
        // null contexts are not allowed
        if (context == null) {
            throw new RuntimeException("SNH");
        }
        InterproceduralWorklistElement element = new InterproceduralWorklistElement(cfgNode, context);

        Integer rpo = this.order.getReversePostOrder(element);
        if (rpo == null) {
            throw new RuntimeException("SNH");
        }
        this.sortedWorkList.put(rpo, element);
    }

    public InterproceduralWorklistElement removeNext() {
        Integer key = this.sortedWorkList.firstKey();
        return this.sortedWorkList.remove(key);
    }

    public boolean hasNext() {
        return !this.sortedWorkList.isEmpty();
    }
}
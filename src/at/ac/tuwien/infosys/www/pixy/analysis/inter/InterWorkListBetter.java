package at.ac.tuwien.infosys.www.pixy.analysis.inter;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

// this interprocedural worklist uses a better order (interprocedural reverse postorder)
public final class InterWorkListBetter 
implements InterWorkList {

    private InterWorkListOrder order;
    private SortedMap<Integer, InterWorkListElement> sortedWorkList;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************
    
    public InterWorkListBetter(InterWorkListOrder order) {
        this.order = order;
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
        if (this.sortedWorkList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}

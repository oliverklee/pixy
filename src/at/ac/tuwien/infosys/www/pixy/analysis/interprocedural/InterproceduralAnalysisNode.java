package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.AnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An AnalysisNode holds analysis-specific information for a certain CFGNode.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class InterproceduralAnalysisNode extends AnalysisNode {
    // context map for interprocedural analysis
    // (Context -> input LatticeElement at current CFG node)
    Map<Context, LatticeElement> phi;

    // value resulting from lazy table folding; must only be modified
    // via setFoldedValue, since we want it to be recycled for some analyses!
    LatticeElement foldedValue;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    protected InterproceduralAnalysisNode(TransferFunction tf) {
        super(tf);
        this.phi = new HashMap<>();
        this.foldedValue = null;
    }

// *********************************************************************************
// GET *****************************************************************************
// *********************************************************************************

    public Map<Context, LatticeElement> getPhi() {
        return this.phi;
    }

    public Set<Context> getContexts() {
        return this.phi.keySet();
    }

    // returns the lattice element currently stored in the PHI map under the
    // given context; can be null
    public LatticeElement getPhiValue(Context context) {
        return this.phi.get(context);
    }

    // like getUnrecycledFoldedValue, but does not perform caching
    public LatticeElement computeFoldedValue() {
        if (this.hasFoldedValue()) {
            return this.foldedValue;
        }

        Iterator<? extends LatticeElement> iter = this.phi.values().iterator();
        if (!iter.hasNext()) {
            return null;
        }

        // initialize the folded value as a clone of the first value
        // in the phi map
        LatticeElement foldedValue = iter.next().cloneMe();

        // lub the rest of the values over the start value
        while (iter.hasNext()) {
            foldedValue.lub(iter.next());
        }

        return foldedValue;
    }

    public boolean hasFoldedValue() {
        return (this.foldedValue != null || this.phi == null);
    }

    public void setFoldedValue(LatticeElement foldedValue) {
        this.foldedValue = foldedValue;
    }

    // only do this after having set the folded value
    public void clearPhiMap() {
        this.phi = null;
    }

    // don't call this function without having checked whether
    // the folded value exists
    public LatticeElement getRecycledFoldedValue() {
        if (this.hasFoldedValue()) {
            return this.foldedValue;
        } else {
            throw new RuntimeException("SNH");
        }
    }

    // returns the least upper bound of all values in the phi map;
    // returns NULL if there is not a single value in the phi map
    // DOESN'T PERFORM RECYCLING OF THE FOLDED ELEMENT,
    // and performs caching (might become a memory-eater)
    public LatticeElement getUnrecycledFoldedValue() {
        // no need to recompute it if we already have it
        if (this.hasFoldedValue()) {
            return this.foldedValue;
        }

        Iterator<? extends LatticeElement> iter = this.phi.values().iterator();
        if (!iter.hasNext()) {
            return null;
        }

        // initialize the folded value as a clone of the first value
        // in the phi map
        this.foldedValue = iter.next().cloneMe();

        // lub the rest of the values over the start value
        while (iter.hasNext()) {
            this.foldedValue.lub(iter.next());
        }

        return this.foldedValue;
    }

// *********************************************************************************
// SET *****************************************************************************
// *********************************************************************************

    // sets the PHI value for the given context
    protected void setPhiValue(Context context, LatticeElement value) {
        this.phi.put(context, value);
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    LatticeElement transfer(LatticeElement value, Context context) {
        return tf.transfer(value, context);
    }
}
package at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator;

import at.ac.tuwien.infosys.www.pixy.analysis.Lattice;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class InclusionDominatorLattice extends Lattice {
    private InclusionDominatorAnalysis inclusionDominatorAnalysis;

    public InclusionDominatorLattice(InclusionDominatorAnalysis inclusionDominatorAnalysis) {
        this.inclusionDominatorAnalysis = inclusionDominatorAnalysis;
    }

    public LatticeElement lub(
        LatticeElement incomingElementX,
        LatticeElement targetElementX) {
        // if the incoming element is the bottom element: return the other element
        if (incomingElementX == this.bottom) {
            // no need for cloning, since we work with a repository of alias
            // lattice elements
            return targetElementX;
        }

        // if the target element is the bottom element: return the other element
        if (targetElementX == this.bottom) {
            // no need for cloning, since we work with a repository of alias
            // lattice elements
            return incomingElementX;
        }

        // if one of the elements is the top element: return the top element;
        // not necessary here: we will never encounter the top element

        // class cast
        InclusionDominatorLatticeElement incomingElement = (InclusionDominatorLatticeElement) incomingElementX;
        InclusionDominatorLatticeElement targetElement = (InclusionDominatorLatticeElement) targetElementX;

        // initialize the resulting lattice element as clone of the target
        // lattice element (we don't want to modify the target lattice element
        // given by the caller of this method);
        // EFF: note that we must not modify the incoming element either: while
        // most transfer functions generate a new lattice element object which
        // could be reused, the ID transfer function simply passes on the
        // reference; if you reuse here, the ID transfer function must return
        // a new element (not clear which method is more efficient)
        InclusionDominatorLatticeElement resultElement = new InclusionDominatorLatticeElement(targetElement);

        // lub the incoming element over the clone of the target element
        resultElement.lub(incomingElement);

        // check if the result element is already in the repository, and
        // recycle it in this case
        resultElement = (InclusionDominatorLatticeElement) this.inclusionDominatorAnalysis.recycle(resultElement);

        return resultElement;
    }
}
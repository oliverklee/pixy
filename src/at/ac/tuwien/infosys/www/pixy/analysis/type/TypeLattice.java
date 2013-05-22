package at.ac.tuwien.infosys.www.pixy.analysis.type;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLattice;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;

import java.util.Collection;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeLattice extends AbstractLattice {
    public TypeLattice(Collection<String> classNames) {
        Type.initTypes(classNames);
    }

    // input elements (incoming and target) are not modified,
    // output element is newly allocated
    public AbstractLatticeElement lub(
        AbstractLatticeElement incomingElementX,
        AbstractLatticeElement targetElementX) {

        // if the incoming element is the bottom element: return the other element
        if (incomingElementX == this.bottom) {
            if (targetElementX != this.bottom) {
                return targetElementX.cloneMe();
            } else {
                return this.bottom;
            }
        }

        // if the target element is the bottom element: return the other element
        if (targetElementX == this.bottom) {
            return incomingElementX.cloneMe();
        }

        // if one of the elements is the top element: return the top element;
        // not necessary here: we will never encounter the top element

        // class cast
        TypeLatticeElement incomingElement = (TypeLatticeElement) incomingElementX;
        TypeLatticeElement targetElement = (TypeLatticeElement) targetElementX;

        // initialize the resulting lattice element as clone of the target
        // lattice element (we don't want to modify the target lattice element
        // given by the caller of this method);
        // EFF: note that we must not modify the incoming element either: while
        // most transfer functions generate a new lattice element object which
        // could be reused, the ID transfer function simply passes on the
        // reference; if you reuse here, the ID transfer function must return
        // a new element (not clear which method is more efficient)
        TypeLatticeElement resultElement = new TypeLatticeElement(targetElement);

        // lub the incoming element over the clone of the target element
        resultElement.lub(incomingElement);

        return resultElement;
    }
}
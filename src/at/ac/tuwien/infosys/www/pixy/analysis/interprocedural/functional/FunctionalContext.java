package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractContext;

/**
 * Context for Sharir & Pnueli's functional approach.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class FunctionalContext extends AbstractContext {
    private AbstractLatticeElement latticeElement;

    public FunctionalContext(AbstractLatticeElement latticeElement) {
        this.latticeElement = latticeElement;
    }

    public AbstractLatticeElement getLatticeElement() {
        return this.latticeElement;
    }

    public int hashCode() {
        return this.latticeElement.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FunctionalContext)) {
            return false;
        }
        FunctionalContext comp = (FunctionalContext) obj;
        return this.latticeElement.equals(comp.getLatticeElement());
    }
}
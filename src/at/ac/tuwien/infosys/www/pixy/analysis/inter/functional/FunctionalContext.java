package at.ac.tuwien.infosys.www.pixy.analysis.inter.functional;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;

// context for Sharir & Pnueli's functional approach
public class FunctionalContext
extends Context {

    private LatticeElement latticeElement;

    public FunctionalContext(LatticeElement latticeElement) {
        this.latticeElement = latticeElement;
    }

    public LatticeElement getLatticeElement() {
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
package at.ac.tuwien.infosys.www.pixy.analysis.alias.tools;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class SccEdge {

    // this edge is undirected, so the order of the nodes
    // doesn't play a role
    private SccNode n1;
    private SccNode n2;

    public SccEdge(SccNode n1, SccNode n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public SccNode getN1() {
        return this.n1;
    }

    public SccNode getN2() {
        return this.n2;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SccEdge)) {
            return false;
        }
        SccEdge comp = (SccEdge) obj;

        if ((this.n1 == comp.getN1() && this.n2 == comp.getN2()) ||
            (this.n1 == comp.getN2() && this.n2 == comp.getN1())) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hashCode = 17;
        // order of the nodes must not matter
        hashCode = hashCode + this.n1.hashCode() + this.n2.hashCode();
        return hashCode;
    }
}
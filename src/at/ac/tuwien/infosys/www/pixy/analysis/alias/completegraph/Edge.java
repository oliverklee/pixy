package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Edge {
    // this edge is undirected, so the order of the nodes
    // doesn't play a role
    private Node n1;
    private Node n2;

    public Edge(Node n1, Node n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public Node getN1() {
        return this.n1;
    }

    public Node getN2() {
        return this.n2;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge comp = (Edge) obj;

        return (this.n1 == comp.getN1() && this.n2 == comp.getN2()) ||
            (this.n1 == comp.getN2() && this.n2 == comp.getN1());
    }

    public int hashCode() {
        int hashCode = 17;
        // order of the nodes must not matter
        hashCode = hashCode + this.n1.hashCode() + this.n2.hashCode();
        return hashCode;
    }
}
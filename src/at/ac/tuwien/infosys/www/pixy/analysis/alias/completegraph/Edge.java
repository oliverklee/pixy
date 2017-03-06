package at.ac.tuwien.infosys.www.pixy.analysis.alias.completegraph;

public class Edge {

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

		if ((this.n1 == comp.getN1() && this.n2 == comp.getN2())
				|| (this.n1 == comp.getN2() && this.n2 == comp.getN1())) {
			return true;
		} else {
			return false;
		}
	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = hashCode + this.n1.hashCode() + this.n2.hashCode();
		return hashCode;
	}
}

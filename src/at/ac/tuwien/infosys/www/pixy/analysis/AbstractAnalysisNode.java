package at.ac.tuwien.infosys.www.pixy.analysis;

public abstract class AbstractAnalysisNode {

	protected AbstractTransferFunction tf;

	protected AbstractAnalysisNode(AbstractTransferFunction tf) {
		this.tf = tf;
	}

	public AbstractTransferFunction getTransferFunction() {
		return this.tf;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement value) {
		return ((AbstractLatticeElement) tf.transfer(value));
	}

	public void setTransferFunction(AbstractTransferFunction tf) {
		this.tf = tf;
	}

}

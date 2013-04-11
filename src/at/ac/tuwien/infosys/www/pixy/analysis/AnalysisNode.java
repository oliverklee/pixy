package at.ac.tuwien.infosys.www.pixy.analysis;

public abstract class AnalysisNode {

    // the transfer function for this node
    protected TransferFunction tf;

    protected AnalysisNode(TransferFunction tf) {
        this.tf = tf;
    }

    public TransferFunction getTransferFunction() {
        return this.tf;
    }

    // applies the transfer function to the given input value
    public LatticeElement transfer(LatticeElement value) {
        return ((LatticeElement) tf.transfer(value));
    }

    public void setTransferFunction(TransferFunction tf) {
        this.tf = tf;
    }
}
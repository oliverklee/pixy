package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractAnalysisNode {
    // the transfer function for this node
    protected AbstractTransferFunction tf;

    protected AbstractAnalysisNode(AbstractTransferFunction tf) {
        this.tf = tf;
    }

    public AbstractTransferFunction getTransferFunction() {
        return this.tf;
    }

    // applies the transfer function to the given input value
    public AbstractLatticeElement transfer(AbstractLatticeElement value) {
        return tf.transfer(value);
    }

    public void setTransferFunction(AbstractTransferFunction tf) {
        this.tf = tf;
    }
}
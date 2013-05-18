package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CompositeTransferFunction
    extends TransferFunction {

    // a list of TransferFunctions to be applied in sequence
    private List<TransferFunction> tfs;

    public CompositeTransferFunction() {
        this.tfs = new LinkedList<>();
    }

    public void add(TransferFunction tf) {
        this.tfs.add(tf);
    }

    // returns an iterator over the contained transfer functions
    public Iterator<TransferFunction> iterator() {
        return this.tfs.iterator();
    }

    public LatticeElement transfer(LatticeElement in) {
        for (TransferFunction tf : this.tfs) {
            in = tf.transfer(in);
        }
        return in;
    }
}
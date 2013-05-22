package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CompositeTransferFunction extends AbstractTransferFunction {
    // a list of TransferFunctions to be applied in sequence
    private List<AbstractTransferFunction> tfs;

    public CompositeTransferFunction() {
        this.tfs = new LinkedList<>();
    }

    public void add(AbstractTransferFunction tf) {
        this.tfs.add(tf);
    }

    // returns an iterator over the contained transfer functions
    public Iterator<AbstractTransferFunction> iterator() {
        return this.tfs.iterator();
    }

    public AbstractLatticeElement transfer(AbstractLatticeElement in) {
        for (AbstractTransferFunction tf : this.tfs) {
            in = tf.transfer(in);
        }
        return in;
    }
}
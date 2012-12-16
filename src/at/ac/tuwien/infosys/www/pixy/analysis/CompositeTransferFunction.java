package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.*;

// EFF: special treatment for ID transfer function:
// don't add it to the list, handle case where the list is
// empty (because it consists only of implicit ID transfer functions)
public class CompositeTransferFunction 
extends TransferFunction {
    
    // a list of TransferFunctions to be applied in sequence
    private List<TransferFunction> tfs;

    public CompositeTransferFunction() {
        this.tfs = new LinkedList<TransferFunction>();
    }
    
    public void add(TransferFunction tf) {
        this.tfs.add(tf);
    }
    
    // returns an iterator over the contained transfer functions
    public Iterator iterator() {
        return this.tfs.iterator();
    }
    
    public LatticeElement transfer(LatticeElement in) {
        for (Iterator iter = this.tfs.iterator(); iter.hasNext();) {
            TransferFunction tf = (TransferFunction) iter.next();
            in = tf.transfer(in);
        }
        return in;
    }

}

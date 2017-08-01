package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.*;

public class CompositeTransferFunction extends AbstractTransferFunction {

	private List<AbstractTransferFunction> tfs;

	public CompositeTransferFunction() {
		this.tfs = new LinkedList<AbstractTransferFunction>();
	}

	public void add(AbstractTransferFunction tf) {
		this.tfs.add(tf);
	}

	public Iterator<AbstractTransferFunction> iterator() {
		return this.tfs.iterator();
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement in) {
		for (Iterator<AbstractTransferFunction> iter = this.tfs.iterator(); iter.hasNext();) {
			AbstractTransferFunction tf = (AbstractTransferFunction) iter.next();
			in = tf.transfer(in);
		}
		return in;
	}

}

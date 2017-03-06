package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;

public abstract class AbstractInterproceduralAnalysisNode extends AbstractAnalysisNode {

	Map<AbstractContext, AbstractLatticeElement> phi;
	AbstractLatticeElement foldedValue;

	protected AbstractInterproceduralAnalysisNode(AbstractTransferFunction tf) {
		super(tf);
		this.phi = new HashMap<AbstractContext, AbstractLatticeElement>();
		this.foldedValue = null;
	}

	public Map<AbstractContext, AbstractLatticeElement> getPhi() {
		return this.phi;
	}

	public Set<AbstractContext> getContexts() {
		return this.phi.keySet();
	}

	public AbstractLatticeElement getPhiValue(AbstractContext context) {
		return ((AbstractLatticeElement) this.phi.get(context));
	}

	public AbstractLatticeElement computeFoldedValue() {

		if (this.hasFoldedValue()) {
			return this.foldedValue;
		}

		Iterator<AbstractLatticeElement> iter = this.phi.values().iterator();
		if (!iter.hasNext()) {
			return null;
		}

		AbstractLatticeElement foldedValue = ((AbstractLatticeElement) iter.next()).cloneMe();

		while (iter.hasNext()) {
			foldedValue.lub((AbstractLatticeElement) iter.next());
		}
		return foldedValue;
	}

	public boolean hasFoldedValue() {
		return (this.foldedValue != null || this.phi == null);
	}

	public void setFoldedValue(AbstractLatticeElement foldedValue) {
		this.foldedValue = foldedValue;
	}

	public void clearPhiMap() {
		this.phi = null;
	}

	public AbstractLatticeElement getRecycledFoldedValue() {
		if (this.hasFoldedValue()) {
			return this.foldedValue;
		} else {
			throw new RuntimeException("SNH");
		}
	}

	public AbstractLatticeElement getUnrecycledFoldedValue() {
		if (this.hasFoldedValue()) {
			return this.foldedValue;
		}

		Iterator<AbstractLatticeElement> iter = this.phi.values().iterator();
		if (!iter.hasNext()) {
			return null;
		}

		this.foldedValue = ((AbstractLatticeElement) iter.next()).cloneMe();
		while (iter.hasNext()) {
			this.foldedValue.lub((AbstractLatticeElement) iter.next());
		}

		return this.foldedValue;
	}

	protected void setPhiValue(AbstractContext context, AbstractLatticeElement value) {
		this.phi.put(context, value);
	}

	AbstractLatticeElement transfer(AbstractLatticeElement value, AbstractContext context) {
		return ((AbstractLatticeElement) tf.transfer(value, context));
	}

}

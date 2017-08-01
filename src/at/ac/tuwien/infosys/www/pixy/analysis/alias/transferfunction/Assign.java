package at.ac.tuwien.infosys.www.pixy.analysis.alias.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Assign extends AbstractTransferFunction {

	private Variable left;
	private Variable right;

	private boolean supported;
	private AliasAnalysis aliasAnalysis;

	public Assign(AbstractTacPlace left, AbstractTacPlace right, AliasAnalysis aliasAnalysis, AbstractCfgNode cfgNode) {

		this.left = (Variable) left;
		this.right = (Variable) right;

		this.aliasAnalysis = aliasAnalysis;

		this.supported = AliasAnalysis.isSupported(this.left, this.right, true, cfgNode.getOriginalLineNumber());

	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		if (!this.supported) {
			return inX;
		}

		if (this.left == this.right) {
			return inX;
		}

		AliasLatticeElement in = (AliasLatticeElement) inX;
		AliasLatticeElement out = new AliasLatticeElement(in);

		out.redirect(this.left, this.right);

		out = (AliasLatticeElement) this.aliasAnalysis.recycle(out);

		return out;
	}
}

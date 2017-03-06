package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Define extends AbstractTransferFunction {

	private AbstractTacPlace setMe;
	private AbstractTacPlace caseInsensitive;

	private ConstantsTable constantsTable;
	private LiteralAnalysis literalAnalysis;
	private AbstractCfgNode cfgNode;

	public Define(ConstantsTable table, LiteralAnalysis literalAnalysis,
			at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define cfgNode) {

		this.setMe = cfgNode.getSetMe();
		this.caseInsensitive = cfgNode.getCaseInsensitive();
		this.constantsTable = table;
		this.literalAnalysis = literalAnalysis;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		Literal constantLit;
		if (this.setMe instanceof Literal) {
			constantLit = (Literal) setMe;
		} else {
			constantLit = literalAnalysis.getLiteral(setMe, cfgNode);
		}

		if (constantLit == Literal.TOP) {
			return out;
		}

		if (this.caseInsensitive == Constant.TRUE) {
			List<?> insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
			if (insensGroup != null) {
				for (Iterator<?> iter = insensGroup.iterator(); iter.hasNext();) {
					Constant constant = (Constant) iter.next();
					out.defineConstant(constant, this.cfgNode);
				}
			} else {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			}

		} else if (this.caseInsensitive == Constant.FALSE) {
			Constant constant = this.constantsTable.getConstant(constantLit.toString());
			if (constant == null) {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			} else {
				out.defineConstant(constant, this.cfgNode);
			}

		} else {
			Constant constant = this.constantsTable.getConstant(constantLit.toString());
			if (constant == null) {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			} else {
				out.defineConstant(constant, this.cfgNode);
			}
			List<?> insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
			if (insensGroup != null) {
				for (Iterator<?> iter = insensGroup.iterator(); iter.hasNext();) {
					Constant weakConstant = (Constant) iter.next();
					if (!weakConstant.equals(constant)) {
						out.defineConstantWeak(weakConstant, this.cfgNode);
					}
				}
			} else {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			}
		}
		return out;
	}
}

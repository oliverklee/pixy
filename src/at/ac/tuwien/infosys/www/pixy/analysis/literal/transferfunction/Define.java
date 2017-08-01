package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class Define extends AbstractTransferFunction {

	private AbstractTacPlace setMe;
	private AbstractTacPlace setTo;
	private AbstractTacPlace caseInsensitive;
	private ConstantsTable constantsTable;
	private AbstractCfgNode cfgNode;

	public Define(ConstantsTable table, at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define cfgNode) {
		this.setMe = cfgNode.getSetMe();
		this.setTo = cfgNode.getSetTo();
		this.caseInsensitive = cfgNode.getCaseInsensitive();
		this.constantsTable = table;
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);
		Literal constantLit = in.getLiteral(this.setMe);
		if (constantLit == Literal.TOP) {
			System.out.println("Warning: can't resolve constant to be defined");
			System.out.println("- " + cfgNode.getFileName() + ":" + cfgNode.getOriginalLineNumber());
			return out;
		}
		Literal valueLit = in.getLiteral(this.setTo);
		Literal caseLit = in.getLiteral(this.caseInsensitive).getBoolValueLiteral();
		if (caseLit == Literal.TRUE) {
			List<?> insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
			if (insensGroup != null) {
				for (Iterator<?> iter = insensGroup.iterator(); iter.hasNext();) {
					Constant constant = (Constant) iter.next();
					out.defineConstant(constant, valueLit);
				}
			} else {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());

			}

		} else if (caseLit == Literal.FALSE) {
			Constant constant = this.constantsTable.getConstant(constantLit.toString());
			if (constant == null) {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			} else {
				out.defineConstant(constant, valueLit);
			}

		} else if (caseLit == Literal.TOP) {
			Constant constant = this.constantsTable.getConstant(constantLit.toString());
			if (constant == null) {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			} else {
				out.defineConstant(constant, valueLit);
			}
			List<?> insensGroup = this.constantsTable.getInsensitiveGroup(constantLit);
			if (insensGroup != null) {
				for (Iterator<?> iter = insensGroup.iterator(); iter.hasNext();) {
					Constant weakConstant = (Constant) iter.next();
					if (!weakConstant.equals(constant)) {
						out.defineConstantWeak(weakConstant, valueLit);
					}
				}
			} else {
				System.out.println("Warning: a constant is defined, but never used");
				System.out.println("- name:    " + constantLit.toString());
				System.out.println("- defined: " + this.cfgNode.getLoc());
			}

		} else {
			throw new RuntimeException("SNH");
		}

		return out;
	}
}
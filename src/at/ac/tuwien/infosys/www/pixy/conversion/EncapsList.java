package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Empty;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;

public class EncapsList {

	private List<Object> encapsList;

	EncapsList() {
		this.encapsList = new LinkedList<Object>();
	}

	void dump() {
		System.out.println("here comes an encaps list:");
		for (Object o : this.encapsList) {
			System.out.println(o);
		}
	}

	void add(AbstractTacPlace place, ControlFlowGraph cfg) {
		this.encapsList.add(place);
		this.encapsList.add(cfg);
	}

	void add(Literal string) {
		this.encapsList.add(string);
	}

	int Lenght() {
		return this.encapsList.size();
	}

	String ToString() {
		String i = "";
		Iterator<Object> iter = this.encapsList.iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();

			if (obj instanceof Literal) {
				i += ((Literal) obj).toString() + "\\";

			}
		}
		i = i.substring(0, i.length() - 1);
		return i;
	}

	TacAttributes makeAtts(Variable temp, ParseNode node) {
		TacAttributes myAtts = new TacAttributes();

		AbstractCfgNode head = new Empty();
		AbstractCfgNode contd = head;

		boolean tempEmpty = true;

		Iterator<Object> iter = this.encapsList.iterator();
		Literal lastLiteral = null;
		while (iter.hasNext()) {
			Object obj = iter.next();

			if (obj instanceof Literal) {
				Literal lit = (Literal) obj;

				if (lastLiteral != null) {
					lastLiteral = new Literal(lastLiteral.toString() + lit.toString());
				} else {
					lastLiteral = lit;
				}

			} else if (obj instanceof AbstractTacPlace) {

				if (lastLiteral != null) {
					AbstractCfgNode cfgNode;
					if (tempEmpty) {
						cfgNode = new AssignSimple(temp, lastLiteral, node);
						tempEmpty = false;
					} else {
						cfgNode = new AssignBinary(temp, temp, lastLiteral, TacOperators.CONCAT, node);
					}
					TacConverter.connect(contd, cfgNode);
					contd = cfgNode;
					lastLiteral = null;
				}
				ControlFlowGraph nextCfg = (ControlFlowGraph) iter.next();

				AbstractCfgNode cfgNode;
				if (tempEmpty) {
					cfgNode = new AssignSimple(temp, (AbstractTacPlace) obj, node);
					tempEmpty = false;
				} else {
					cfgNode = new AssignBinary(temp, temp, (AbstractTacPlace) obj, TacOperators.CONCAT, node);
				}
				TacConverter.connect(contd, nextCfg);
				TacConverter.connect(nextCfg, cfgNode);
				contd = cfgNode;

			} else {
			}
		}

		if (lastLiteral != null) {
			AbstractCfgNode cfgNode;
			if (tempEmpty) {
				cfgNode = new AssignSimple(temp, lastLiteral, node);
				tempEmpty = false;
			} else {
				cfgNode = new AssignBinary(temp, temp, lastLiteral, TacOperators.CONCAT, node);
			}
			TacConverter.connect(contd, cfgNode);
			contd = cfgNode;
			lastLiteral = null;
		}

		myAtts.setCfg(new ControlFlowGraph(head, contd));
		myAtts.setPlace(temp);
		return myAtts;
	}
}

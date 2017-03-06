package at.ac.tuwien.infosys.www.pixy.sanitation;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

public class FSAUtils {

	private static String mohri = MyOptions.fsa_home + "/Examples/MohriSproat96/ops.pl";

	public static FSAAutomaton reg_replace(FSAAutomaton phpPatternAuto, FSAAutomaton replaceAuto,
			FSAAutomaton subjectAuto, boolean preg, AbstractCfgNode cfgNode) {

		List<String> finitePattern = phpPatternAuto.getFiniteString();
		if (finitePattern == null) {
			return FSAAutomaton.makeAnyString();
		}
		if (finitePattern.isEmpty()) {
			return FSAAutomaton.makeAnyString();
		}

		boolean approximate = false;
		FSAAutomaton prologPatternAuto = null;
		try {
			prologPatternAuto = FSAAutomaton.convertPhpRegex(finitePattern, preg);
		} catch (UnsupportedRegexException e) {
			System.err.println("unsupported regex:");
			System.err.println("- " + cfgNode.getLoc());
			approximate = true;
		} catch (Exception e) {
			System.err.println("Exception during regex conversion");
			System.err.println("- " + cfgNode.getLoc());
			System.err.println(e.getMessage());
			e.printStackTrace();
			approximate = true;
		}

		FSAAutomaton retMe;
		if (approximate) {
			retMe = FSAAutomaton.makeAnyString();
		} else {
			String patternFile = prologPatternAuto.toFile("temp1.auto");
			String replaceFile = replaceAuto.toFile("temp2.auto");
			String subjectFile = subjectAuto.toFile("temp3.auto");

			String c = MyOptions.fsa_home + "/" + "fsa -aux " + mohri + " -r compose(file('" + subjectFile
					+ "'),replace(file('" + patternFile + "'),file('" + replaceFile + "'))) ";
			String autoString = Utils.exec(c);
			retMe = new FSAAutomaton(autoString);
			retMe = retMe.projectOut();
		}
		return retMe;
	}

	public static FSAAutomaton str_replace(FSAAutomaton searchAuto, FSAAutomaton replaceAuto, FSAAutomaton subjectAuto,
			AbstractCfgNode cfgNode) {

		if (searchAuto.getFiniteString() == null) {
			System.out.println("Warning: search automaton is not finite!");
			System.out.println("- " + cfgNode.getLoc());
			replaceAuto = FSAAutomaton.makeAnyString();
		}

		String searchFile = searchAuto.toFile("temp1.auto");
		String replaceFile = replaceAuto.toFile("temp2.auto");
		String subjectFile = subjectAuto.toFile("temp3.auto");

		String c = MyOptions.fsa_home + "/" + "fsa -aux " + mohri + " -r compose(file('" + subjectFile
				+ "'),replace(file('" + searchFile + "'),file('" + replaceFile + "'))) ";

		String autoString = Utils.exec(c);

		FSAAutomaton retMe = new FSAAutomaton(autoString);

		retMe = retMe.projectOut();

		return retMe;
	}

	public static FSAAutomaton addslashes(FSAAutomaton subjectAuto, AbstractCfgNode cfgNode) {

		FSAAutomaton searchAuto = FSAAutomaton.makeString("\\");
		FSAAutomaton replaceAuto = FSAAutomaton.makeString("\\\\");
		subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
		searchAuto = FSAAutomaton.makeString("'");
		replaceAuto = FSAAutomaton.makeString("\\'");
		subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);
		searchAuto = FSAAutomaton.makeString("\"");
		replaceAuto = FSAAutomaton.makeString("\\\"");
		subjectAuto = str_replace(searchAuto, replaceAuto, subjectAuto, cfgNode);

		return subjectAuto;
	}
}
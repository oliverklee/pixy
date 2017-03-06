package at.ac.tuwien.infosys.www.pixy.conversion;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.phpParser.PhpSymbols;

public class ParseNodeHeuristics {

	private static LiteralAnalysis literalAnalysis;
	private static Include includeNode;

	public static List<String> getPossibleIncludeTargets(Include includeNode, LiteralAnalysis literalAnalysis,
			Map<Include, String> include2String, String workingDirectory) {

		ParseNodeHeuristics.literalAnalysis = literalAnalysis;
		ParseNodeHeuristics.includeNode = includeNode;

		ParseNode parseNode = includeNode.getParseNode();

		if (parseNode.getSymbol() != PhpSymbols.internal_functions_in_yacc) {
			throw new RuntimeException("SNH");
		}
		ParseNode firstChild = parseNode.getChild(0);
		switch (firstChild.getSymbol()) {
		case PhpSymbols.T_INCLUDE:
		case PhpSymbols.T_INCLUDE_ONCE:
		case PhpSymbols.T_REQUIRE:
		case PhpSymbols.T_REQUIRE_ONCE:
			break;
		default:
			throw new RuntimeException("SNH");
		}
		ParseNode secondChild = parseNode.getChild(1);
		if (secondChild.getSymbol() != PhpSymbols.expr) {
			throw new RuntimeException("SNH");
		}
		LinkedList<String> stringList = expr(secondChild);
		boolean somethingLiteral = false;
		boolean precedingDotStar = false;
		StringBuilder pattern = new StringBuilder();
		if (stringList != null) {
			for (String s : stringList) {

				if (s == null) {
					if (precedingDotStar) {
					} else {
						pattern.append(".*");
						precedingDotStar = true;
					}
				} else {
					pattern.append(Pattern.quote(s));
					somethingLiteral = true;
					precedingDotStar = false;
				}
			}
		}

		pattern.append("$");
		normalizePath(pattern);
		if (!somethingLiteral) {
			return null;
		}
		Pattern patternObj = Pattern.compile(pattern.toString());
		List<File> candidates1 = Utils.fileListFromDir(workingDirectory);
		System.out.println("inclusion matching against " + candidates1.size() + " candidates");
		List<String> winners = matchCandidates(patternObj, candidates1);
		if (winners == null) {
			return null;
		}
		if (winners.size() == 1) {
			return winners;
		}

		List<File> candidates2 = Utils.fileListFromFile(parseNode.getFileName());
		candidates2.removeAll(candidates1);
		winners = matchCandidates(patternObj, candidates2);
		if (winners == null) {
			return null;
		}

		if (winners.isEmpty()) {
			include2String.put(includeNode, patternObj.pattern());
		}

		return winners;
	}

	private static List<String> matchCandidates(Pattern patternObj, Collection<File> candidates) {
		List<String> winners = new LinkedList<String>();
		for (File candidate : candidates) {

			if (winners.size() > 1) {
				return null;
			}
			String candidatePath = candidate.getPath();
			Matcher matcher = patternObj.matcher(candidatePath);
			if (matcher.find()) {
				winners.add(candidatePath);
			}
		}
		return winners;
	}

	private static void normalizePath(StringBuilder pattern) {
	}

	private static LinkedList<String> expr(ParseNode node) {

		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {
		}

		return myList;
	}

	@SuppressWarnings("unused")
	private static LinkedList<String> expr_without_variable(ParseNode node) {

		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.expr: {
			switch (node.getChild(1).getSymbol()) {

			case PhpSymbols.T_POINT: {
				LinkedList<String> list0 = expr(node.getChild(0));
				List<String> list2 = expr(node.getChild(2));
				myList = list0;
				myList.addAll(list2);
				break;
			}

			default: {
				myList = new LinkedList<String>();
				myList.add(null);
			}

			}
			break;

		}

		case PhpSymbols.T_OPEN_BRACES: {
			myList = expr(node.getChild(1));
			break;
		}

		case PhpSymbols.scalar: {
			myList = scalar(firstChild);
			break;
		}

		default: {
			myList = new LinkedList<String>();
			myList.add(null);
		}

		}

		return myList;
	}

	private static LinkedList<String> scalar(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.common_scalar: {
			myList = common_scalar(firstChild);
			break;
		}

		case PhpSymbols.T_DOUBLE_QUOTE: {
			myList = encaps_list(node.getChild(1));
			break;
		}

		default: {
			myList = new LinkedList<String>();
			myList.add(null);
		}

		}

		return myList;

	}

	private static LinkedList<String> common_scalar(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_CONSTANT_ENCAPSED_STRING: {
			myList = new LinkedList<String>();

			myList.add(new Literal(firstChild.getLexeme()).toString());
			break;
		}

		default: {
			myList = new LinkedList<String>();
			myList.add(null);
		}

		}

		return myList;
	}

	@SuppressWarnings("unused")
	private static LinkedList<String> r_cvar(ParseNode node) {
		return cvar(node.getChild(0));
	}

	private static LinkedList<String> cvar(ParseNode node) {
		LinkedList<String> myList = null;

		if (node.getNumChildren() == 1) {
			myList = cvar_without_objects(node.getChild(0));
		} else {
			myList = new LinkedList<String>();
			myList.add(null);
		}

		return myList;
	}

	private static LinkedList<String> cvar_without_objects(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.reference_variable: {
			myList = reference_variable(firstChild);
			break;
		}

		case PhpSymbols.simple_indirect_reference: {
			myList = new LinkedList<String>();
			myList.add(null);
			break;
		}

		default: {
			throw new RuntimeException("SNH");
		}
		}

		return myList;
	}

	private static LinkedList<String> reference_variable(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.reference_variable: {
			myList = new LinkedList<String>();
			myList.add(null);
			break;
		}

		case PhpSymbols.compound_variable: {
			myList = compound_variable(firstChild);
			break;
		}
		default: {
			throw new RuntimeException("SNH");
		}
		}
		return myList;
	}

	private static LinkedList<String> compound_variable(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		switch (firstChild.getSymbol()) {

		case PhpSymbols.T_VARIABLE: {
			myList = new LinkedList<String>();

			Literal lit = literalAnalysis.getLiteral(firstChild.getLexeme(), includeNode);
			if (lit == Literal.TOP) {
				myList.add(null);
			} else {
				myList.add(lit.toString());
			}

			break;
		}

		case PhpSymbols.T_DOLLAR: {
			myList = new LinkedList<String>();
			myList.add(null);
			break;
		}

		default: {
			throw new RuntimeException("SNH");
		}

		}

		return myList;
	}

	private static LinkedList<String> encaps_list(ParseNode node) {
		LinkedList<String> myList = null;

		ParseNode firstChild = node.getChild(0);
		if (firstChild.getSymbol() == PhpSymbols.T_EMPTY) {
			myList = new LinkedList<String>();
			return myList;
		}

		ParseNode secondChild = node.getChild(1);
		switch (secondChild.getSymbol()) {

		case PhpSymbols.encaps_var: {
			LinkedList<String> list0 = encaps_list(firstChild);
			LinkedList<String> list1 = encaps_var(secondChild);
			myList = list0;
			myList.addAll(list1);
			break;
		}

		case PhpSymbols.T_STRING: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_NUM_STRING: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_ENCAPSED_AND_WHITESPACE: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_OPEN_RECT_BRACES: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_CLOSE_RECT_BRACES: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_OPEN_CURLY_BRACES: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_CLOSE_CURLY_BRACES: {
			myList = encapsListHelper(node);
			break;
		}

		case PhpSymbols.T_OBJECT_OPERATOR: {
			myList = new LinkedList<String>();
			myList.add(null);
			break;
		}

		default: {
			throw new RuntimeException("SNH");
		}
		}

		return myList;

	}

	private static LinkedList<String> encapsListHelper(ParseNode node) {
		LinkedList<String> myList = encaps_list(node.getChild(0));
		myList.add(node.getChild(1).getLexeme());
		return myList;
	}

	private static LinkedList<String> encaps_var(ParseNode node) {
		LinkedList<String> myList = null;

		if (node.getNumChildren() == 1) {
			myList = new LinkedList<String>();
			Literal lit = literalAnalysis.getLiteral(node.getChild(0).getLexeme(), includeNode);
			if (lit == Literal.TOP) {
				myList.add(null);
			} else {
				myList.add(lit.toString());
			}
			return myList;
		}

		myList = new LinkedList<String>();
		myList.add(null);

		return myList;
	}
}
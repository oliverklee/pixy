package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElementBottom;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.DummyAliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MayAliasPair;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MayAliases;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MustAliasGroup;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.MustAliases;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural.IntraproceduralAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural.IntraproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.DummyLiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.BuiltinFunctions;
import at.ac.tuwien.infosys.www.pixy.conversion.CfgEdge;
import at.ac.tuwien.infosys.www.pixy.conversion.Constant;
import at.ac.tuwien.infosys.www.pixy.conversion.ConstantsTable;
import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.SymbolTable;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgEntry;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgExit;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Empty;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.EmptyTest;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Eval;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Global;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.If;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Static;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseTree;

import java.util.*;
import java.io.*;

public final class Dumper {

	private static HashMap<AbstractCfgNode, Integer> node2Int;
	private static int idCounter;
	static final String linesep = System.getProperty("line.separator");

	private Dumper() {
	}

	static void dumpDot(ParseTree parseTree, String path, String filename) {

		(new File(path)).mkdir();

		try {
			Writer outWriter = new FileWriter(path + '/' + filename);
			dumpDot(parseTree, outWriter);
			outWriter.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	static void dumpDot(ParseTree parseTree, Writer outWriter) {
		try {
			outWriter.write("digraph parse_tree {\n");
			dumpDot(parseTree.getRoot(), outWriter);
			outWriter.write("}\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	static void dumpDot(ParseNode parseNode, Writer outWriter) throws java.io.IOException {

		outWriter.write("  n" + parseNode.getId() + " [label=\"");

		String symbolName = parseNode.getName();
		outWriter.write(escapeDot(symbolName, 0));

		if (parseNode.isToken()) {
			String lexeme = parseNode.getLexeme();
			outWriter.write("\\n");
			outWriter.write(escapeDot(lexeme, 10));
		}
		outWriter.write("\"];\n");

		ParseNode parent = parseNode.getParent();
		if (parent != null) {
			outWriter.write("  n" + parent.getId() + " -> n" + parseNode.getId() + ";\n");
		}
		for (int i = 0; i < parseNode.getChildren().size(); i++) {
			dumpDot(parseNode.getChild(i), outWriter);
		}

	}

	public static void dumpDot(TacFunction function, String graphPath, boolean dumpParams) {

		dumpDot(function.getCfg(), function.getName(), graphPath);

		if (dumpParams) {
			List<TacFormalParameter> params = function.getParams();
			for (int i = 0; i < params.size(); i++) {
				TacFormalParameter param = (TacFormalParameter) params.get(i);
				String paramString = param.getVariable().getName();
				paramString = paramString.substring(1); 
				if (param.hasDefault()) {
					dumpDot(param.getDefaultCfg(), function.getName() + "_" + paramString, graphPath);
				}
			}
		}
	}

	static void dumpDot(ControlFlowGraph cfg, String graphName, String graphPath) {
		dumpDot(cfg, graphName, graphPath, graphName + ".dot");
	}

	public static void dumpDot(ControlFlowGraph cfg, String graphName, String path, String filename) {

		(new File(path)).mkdir();

		try {
			Writer outWriter = new FileWriter(path + "/" + filename);
			dumpDot(cfg, graphName, outWriter);
			outWriter.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	static void dumpDot(ControlFlowGraph cfg, String graphName, Writer outWriter) {

		try {
			Dumper.node2Int = new HashMap<AbstractCfgNode, Integer>();
			Dumper.idCounter = 0;
			outWriter.write("digraph cfg {\n  label=\"");
			outWriter.write(escapeDot(graphName, 0));
			outWriter.write("\";\n");
			outWriter.write("  labelloc=t;\n");
			dumpDot(cfg.getHead(), outWriter);
			outWriter.write("}\n");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	static int dumpDot(AbstractCfgNode cfgNode, Writer outWriter) throws java.io.IOException {

		int nodeId = Dumper.idCounter;
		Dumper.node2Int.put(cfgNode, new Integer(Dumper.idCounter++));

		String name = makeCfgNodeName(cfgNode);

		outWriter.write("  n" + nodeId + " [label=\"" + name + "\"];\n");

		int succId;
		for (int i = 0; i < 2; i++) {

			CfgEdge outEdge = cfgNode.getOutEdge(i);

			if (outEdge != null) {

				AbstractCfgNode succNode = outEdge.getDest();

				Integer succIdInt = ((Integer) Dumper.node2Int.get(succNode));
				if (succIdInt == null) {
					succId = dumpDot(succNode, outWriter);
				} else {
					succId = succIdInt.intValue();
				}

				outWriter.write("  n" + nodeId + " -> n" + succId);
				if (outEdge.getType() != CfgEdge.NORMAL_EDGE) {
					outWriter.write(" [label=\"" + outEdge.getName() + "\"]");
				}
				outWriter.write(";\n");

			}
		}

		return nodeId;
	}

	public static void dump(TacFunction function) {
		System.out.println("***************************************");
		System.out.println("Function " + function.getName());
		System.out.println("***************************************");
		System.out.println();
		if (function.isReference()) {
			System.out.println("isReference");
		}
		List<TacFormalParameter> params = function.getParams();
		for (int i = 0; i < params.size(); i++) {
			TacFormalParameter param = (TacFormalParameter) params.get(i);
			String paramString = param.getVariable().getName();
			System.out.print("Param: " + paramString);
			if (param.isReference()) {
				System.out.print(" (isReference)");
			}
			if (param.hasDefault()) {
				System.out.print(" (hasDefault)");
			}
			System.out.println();
		}
	}

	public static String makeCfgNodeName(AbstractCfgNode cfgNodeX) {

		if (cfgNodeX instanceof BasicBlock) {

			BasicBlock cfgNode = (BasicBlock) cfgNodeX;
			StringBuilder label = new StringBuilder("basic block\\n");
			for (Iterator<AbstractCfgNode> iter = cfgNode.getContainedNodes().iterator(); iter.hasNext();) {
				AbstractCfgNode containedNode = (AbstractCfgNode) iter.next();
				label.append(makeCfgNodeName(containedNode));
				label.append("\\n");
			}
			return label.toString();

		} else if (cfgNodeX instanceof AssignSimple) {

			AssignSimple cfgNode = (AssignSimple) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getLeft());
			String rightString = getPlaceString(cfgNode.getRight());
			return (leftString + " = " + rightString);

		} else if (cfgNodeX instanceof AssignBinary) {

			AssignBinary cfgNode = (AssignBinary) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getLeft());
			String leftOperandString = getPlaceString(cfgNode.getLeftOperand());
			String rightOperandString = getPlaceString(cfgNode.getRightOperand());
			int op = cfgNode.getOperator();

			return (leftString + " = " + leftOperandString + " " + TacOperators.opToName(op) + " "
					+ rightOperandString);

		} else if (cfgNodeX instanceof AssignUnary) {

			AssignUnary cfgNode = (AssignUnary) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getLeft());
			String rightString = getPlaceString(cfgNode.getRight());
			int op = cfgNode.getOperator();

			return (leftString + " = " + " " + TacOperators.opToName(op) + " " + rightString);

		} else if (cfgNodeX instanceof AssignReference) {

			AssignReference cfgNode = (AssignReference) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getLeft());
			String rightString = getPlaceString(cfgNode.getRight());
			return (leftString + " =& " + rightString);

		} else if (cfgNodeX instanceof If) {

			If cfgNode = (If) cfgNodeX;
			String leftOperandString = getPlaceString(cfgNode.getLeftOperand());
			String rightOperandString = getPlaceString(cfgNode.getRightOperand());
			int op = cfgNode.getOperator();

			return ("if " + leftOperandString + " " + TacOperators.opToName(op) + " " + rightOperandString);

		} else if (cfgNodeX instanceof Empty) {
			return ";";
		} else if (cfgNodeX instanceof CfgEntry) {
			return "entry";
		} else if (cfgNodeX instanceof CfgExit) {
			CfgExit cfgNode = (CfgExit) cfgNodeX;
			return "exit " + cfgNode.getEnclosingFunction().getName();
		} else if (cfgNodeX instanceof Call) {
			Call cfgNode = (Call) cfgNodeX;
			String objectString = "";
			Variable object = cfgNode.getObject();
			if (object != null) {
				objectString = object + "->";
			}
			return (objectString + cfgNode.getFunctionNamePlace().toString() + "(...)");
		} else if (cfgNodeX instanceof CallPreparation) {

			CallPreparation cfgNode = (CallPreparation) cfgNodeX;

			List<TacActualParameter> paramList = cfgNode.getParamList();
			StringBuilder paramListStringBuf = new StringBuilder();
			for (Iterator<TacActualParameter> iter = paramList.iterator(); iter.hasNext();) {
				TacActualParameter param = (TacActualParameter) iter.next();
				if (param.isReference()) {
					paramListStringBuf.append("&");
				}
				paramListStringBuf.append(getPlaceString(param.getPlace()));
				if (iter.hasNext()) {
					paramListStringBuf.append(", ");
				}
			}

			return ("prepare " + cfgNode.getFunctionNamePlace().toString() + "(" + paramListStringBuf.toString() + ")");

		} else if (cfgNodeX instanceof CallReturn) {

			CallReturn cfgNode = (CallReturn) cfgNodeX;
			return ("call-return (" + cfgNode.getTempVar() + ")");

		} else if (cfgNodeX instanceof CallBuiltinFunction) {

			CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;

			List<TacActualParameter> paramList = cfgNode.getParamList();
			StringBuilder paramListStringBuf = new StringBuilder();
			for (Iterator<TacActualParameter> iter = paramList.iterator(); iter.hasNext();) {
				TacActualParameter param = (TacActualParameter) iter.next();
				if (param.isReference()) {
					paramListStringBuf.append("&");
				}
				paramListStringBuf.append(getPlaceString(param.getPlace()));
				if (iter.hasNext()) {
					paramListStringBuf.append(", ");
				}
			}

			return (cfgNode.getFunctionName() + "(" + paramListStringBuf.toString() + ") " + "<"
					+ getPlaceString(cfgNode.getTempVar()) + ">");

		} else if (cfgNodeX instanceof CallUnknownFunction) {

			CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;

			List<TacActualParameter> paramList = cfgNode.getParamList();
			StringBuilder paramListStringBuf = new StringBuilder();
			for (Iterator<TacActualParameter> iter = paramList.iterator(); iter.hasNext();) {
				TacActualParameter param = (TacActualParameter) iter.next();
				if (param.isReference()) {
					paramListStringBuf.append("&");
				}
				paramListStringBuf.append(getPlaceString(param.getPlace()));
				if (iter.hasNext()) {
					paramListStringBuf.append(", ");
				}
			}

			return ("UNKNOWN: " + cfgNode.getFunctionName() + "(" + paramListStringBuf.toString() + ") " + "<"
					+ getPlaceString(cfgNode.getTempVar()) + ">");

		} else if (cfgNodeX instanceof AssignArray) {

			AssignArray cfgNode = (AssignArray) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getLeft());
			return (leftString + " = array()");

		} else if (cfgNodeX instanceof Unset) {

			Unset cfgNode = (Unset) cfgNodeX;
			String unsetMe = cfgNode.getOperand().getVariable().toString();
			return ("unset(" + unsetMe + ")");

		} else if (cfgNodeX instanceof Echo) {

			Echo cfgNode = (Echo) cfgNodeX;
			String echoMe = getPlaceString(cfgNode.getPlace());
			return ("echo(" + echoMe + ")");

		} else if (cfgNodeX instanceof Global) {

			Global cfgNode = (Global) cfgNodeX;
			String globMe = cfgNode.getOperand().toString();
			return ("global " + globMe);

		} else if (cfgNodeX instanceof Static) {

			Static cfgNode = (Static) cfgNodeX;
			String statMe = cfgNode.getOperand().getVariable().toString();
			String initial;
			if (cfgNode.hasInitialPlace()) {
				initial = " = " + getPlaceString(cfgNode.getInitialPlace());
			} else {
				initial = "";
			}
			return ("static " + statMe + initial);

		} else if (cfgNodeX instanceof Isset) {

			Isset cfgNode = (Isset) cfgNodeX;
			String checkMe = cfgNode.getRight().getVariable().toString();
			String leftString = cfgNode.getLeft().getVariable().toString();
			return (leftString + " = " + "isset(" + checkMe + ")");

		} else if (cfgNodeX instanceof EmptyTest) {

			EmptyTest cfgNode = (EmptyTest) cfgNodeX;
			String checkMe = cfgNode.getRight().getVariable().toString();
			String leftString = cfgNode.getLeft().getVariable().toString();
			return (leftString + " = " + "empty(" + checkMe + ")");

		} else if (cfgNodeX instanceof Eval) {

			Eval cfgNode = (Eval) cfgNodeX;
			String evalMe = "";
			String leftString = "";
			if (cfgNode.getRight().isVariable()) {
				evalMe = cfgNode.getRight().getVariable().toString();
			}
			if (cfgNode.getLeft().isVariable()) {
				leftString = cfgNode.getLeft().getVariable().toString();
			}
			return (leftString + " = " + "eval(" + evalMe + ")");

		} else if (cfgNodeX instanceof Define) {

			Define cfgNode = (Define) cfgNodeX;
			return ("define(" + cfgNode.getSetMe() + ", " + cfgNode.getSetTo() + ", " + cfgNode.getCaseInsensitive()
					+ ")");

		} else if (cfgNodeX instanceof Include) {

			Include cfgNode = (Include) cfgNodeX;
			String leftString = getPlaceString(cfgNode.getTemp());
			String rightString = getPlaceString(cfgNode.getIncludeMe());
			return (leftString + " = include " + rightString);

		} else if (cfgNodeX instanceof IncludeStart) {
			return ("incStart");

		} else if (cfgNodeX instanceof IncludeEnd) {
			return ("incEnd");

		} else {
			return "to-do: " + cfgNodeX.getClass();
		}
	}

	static String getPlaceString(AbstractTacPlace place) {
		if (place.isVariable()) {
			return place.toString();
		} else if (place.isConstant()) {
			return place.toString();
		} else {
			return escapeDot(place.toString(), 20);
		}
	}

	static public String escapeDot(String escapeMe, int limit) {
		if (limit > 0 && escapeMe.length() > limit) {
			return "...";
		}
		StringBuilder escaped = new StringBuilder(escapeMe);
		for (int i = 0; i < escaped.length(); i++) {
			char inspectMe = escaped.charAt(i);
			if (inspectMe == '\n' || inspectMe == '\r') {
				escaped.deleteCharAt(i);
				i--;
			} else if (inspectMe == '"' || inspectMe == '\\') {
				escaped.insert(i, '\\');
				i++;
			}
		}
		return escaped.toString();
	}

	static void dump(ParseTree parseTree) {
		recursiveDump(parseTree.getRoot(), 0);
	}

	static public void dump(ParseNode parseNode, int level) {
		StringBuilder buf = new StringBuilder(level);
		for (int i = 0; i < level; i++) {
			buf.append(" ");
		}
		String spaces = buf.toString();
		System.out.print(spaces + "Sym: " + parseNode.getSymbol() + ", Name: " + parseNode.getName());
		if (parseNode.getLexeme() != null) {
			System.out.print(", Lex: " + parseNode.getLexeme() + ", lineno: " + parseNode.getLineno());
		}
		System.out.println();
	}

	static public void recursiveDump(ParseNode parseNode, int level) {
		dump(parseNode, level);
		for (Iterator<?> iter = parseNode.getChildren().iterator(); iter.hasNext();) {
			recursiveDump((ParseNode) iter.next(), level + 1);
		}
	}

	static public void dump(SymbolTable symbolTable, String name) {
		System.out.println("***************************************");
		System.out.println("Symbol Table: " + name);
		System.out.println("***************************************");
		System.out.println();
		Map<?, ?> variables = symbolTable.getVariables();
		for (Iterator<?> iter = variables.values().iterator(); iter.hasNext();) {
			dump((Variable) iter.next());
			System.out.println();
		}
	}

	static public void dump(Variable variable) {

		System.out.println(variable);

		if (variable.isArray()) {
			System.out.println("isArray:            true");

			List<?> elements = variable.getElements();
			if (!elements.isEmpty()) {
				System.out.print("elements:           ");
				for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
					Variable element = (Variable) iter.next();
					System.out.print(element.getName() + " ");
				}
				System.out.println();
			}
		}

		if (variable.isArrayElement()) {
			System.out.println("isArrayElement:     true");
			System.out.println("enclosingArray:     " + variable.getEnclosingArray().getName());
			System.out.println("topEnclosingArray:  " + variable.getTopEnclosingArray().getName());
			AbstractTacPlace indexPlace = variable.getIndex();
			System.out.print("index type:         ");
			if (indexPlace.isLiteral()) {
				System.out.println("literal");
			} else if (indexPlace.isVariable()) {
				System.out.println("variable");
			} else if (indexPlace.isConstant()) {
				System.out.println("constant");
			} else {
				System.out.println("UNKNOWN!");
			}
			System.out.print("indices:            ");
			for (Iterator<?> iter = variable.getIndices().iterator(); iter.hasNext();) {
				AbstractTacPlace index = (AbstractTacPlace) iter.next();
				System.out.print(index + " ");
			}
			System.out.println();
		}

		AbstractTacPlace depPlace = variable.getDependsOn();
		if (depPlace != null) {
			System.out.println("dependsOn:          " + depPlace.toString());
		}

		List<?> indexFor = variable.getIndexFor();
		if (!indexFor.isEmpty()) {
			System.out.print("indexFor:           ");
			for (Iterator<?> iter = indexFor.iterator(); iter.hasNext();) {
				Variable indexed = (Variable) iter.next();
				System.out.print(indexed + " ");
			}
			System.out.println();
		}
	}

	static public void dump(ConstantsTable constantsTable) {
		System.out.println("***************************************");
		System.out.println("Constants Table ");
		System.out.println("***************************************");
		System.out.println();
		Map<?, ?> constants = constantsTable.getConstants();
		for (Iterator<?> iter = constants.values().iterator(); iter.hasNext();) {
			System.out.println(((Constant) iter.next()).getLabel());
		}
		System.out.println();
		System.out.println("Insensitive Groups:");
		System.out.println();
		Map<?, ?> insensitiveGroups = constantsTable.getInsensitiveGroups();
		for (Iterator<?> iter = insensitiveGroups.values().iterator(); iter.hasNext();) {
			List<?> insensitiveGroup = (List<?>) iter.next();
			System.out.print("* ");
			for (Iterator<?> iter2 = insensitiveGroup.iterator(); iter2.hasNext();) {
				System.out.print(((Constant) iter2.next()).getLabel() + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	@SuppressWarnings("rawtypes")
	static public void dump(InclusionDominatorAnalysis analysis) {
		IntraproceduralAnalysisInformation analysisInfo = analysis.getAnalysisInfo();
		for (Iterator<?> iter = analysisInfo.getMap().entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			AbstractCfgNode cfgNode = (AbstractCfgNode) entry.getKey();
			IntraproceduralAnalysisNode analysisNode = (IntraproceduralAnalysisNode) entry.getValue();
			System.out
					.println("dominators for cfg node " + cfgNode.toString() + ", " + cfgNode.getOriginalLineNumber());
			Dumper.dump((InclusionDominatorLatticeElement) analysisNode.getInValue());
		}
	}

	static public void dump(AbstractInterproceduralAnalysis analysis, String path, String filename) {

		(new File(path)).mkdir();

		try {
			Writer writer = new FileWriter(path + '/' + filename);
			if (analysis instanceof DummyLiteralAnalysis || analysis instanceof DummyAliasAnalysis) {
				writer.write("Dummy Analysis" + linesep);
				writer.close();
				return;
			}

			List<?> functions = analysis.getFunctions();
			InterproceduralAnalysisInformation analysisInfoNew = analysis.getInterAnalysisInfo();

			if (analysis instanceof LiteralAnalysis) {
				writer.write(linesep + "Default Lattice Element:" + linesep + linesep);
				dump(LiteralLatticeElement.DEFAULT, writer);
			}

			for (Iterator<?> iter = functions.iterator(); iter.hasNext();) {
				TacFunction function = (TacFunction) iter.next();
				ControlFlowGraph cfg = function.getCfg();
				writer.write(linesep + "****************************************************" + linesep);
				writer.write(function.getName() + linesep);
				writer.write("****************************************************" + linesep + linesep);
				for (Iterator<?> bft = cfg.bfIterator(); bft.hasNext();) {
					AbstractCfgNode cfgNode = (AbstractCfgNode) bft.next();
					writer.write("----------------------------------------" + linesep);
					writer.write(cfgNode.getFileName() + ", " + cfgNode.getOriginalLineNumber() + ", "
							+ makeCfgNodeName(cfgNode) + linesep);
					dump(analysisInfoNew.getAnalysisNode(cfgNode).getRecycledFoldedValue(), writer);
				}
				writer.write("----------------------------------------" + linesep);
			}

			writer.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}

	}

	static public void dump(AbstractInterproceduralAnalysisNode node) {
		System.out.print("Transfer Function: ");
		try {
			System.out.println(node.getTransferFunction().getClass().getName());
		} catch (NullPointerException e) {
			System.out.println("<<null>>");
		}
		Map<?, ?> phi = node.getPhi();
		for (Iterator<?> iter = phi.values().iterator(); iter.hasNext();) {
			System.out.println("~~~~~~~~~~~~~~~");
			AbstractLatticeElement element = (AbstractLatticeElement) iter.next();
			dump(element);
		}
	}

	static public void dump(AbstractLatticeElement elementX) {

		try {
			Writer writer = new OutputStreamWriter(System.out);
			dump(elementX, writer);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException("SNH:" + e.getStackTrace());
		}

	}

	@SuppressWarnings("rawtypes")
	static public void dump(AbstractLatticeElement elementX, Writer writer) throws IOException {

		if (elementX instanceof AliasLatticeElement) {

			AliasLatticeElement element = (AliasLatticeElement) elementX;
			dump(element.getMustAliases(), writer);
			dump(element.getMayAliases(), writer);

		} else if (elementX instanceof LiteralLatticeElement) {

			LiteralLatticeElement element = (LiteralLatticeElement) elementX;

			Map<?, ?> placeToLit = element.getPlaceToLit();
			for (Iterator<?> iterator = placeToLit.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				AbstractTacPlace place = (AbstractTacPlace) entry.getKey();
				Literal lit = (Literal) entry.getValue();
				writer.write(place + ":      " + lit + linesep);
			}

		} else if (elementX instanceof DependencyLatticeElement) {

			dumpComplete((DependencyLatticeElement) elementX, writer);

		} else if (elementX instanceof InclusionDominatorLatticeElement) {

			InclusionDominatorLatticeElement element = (InclusionDominatorLatticeElement) elementX;
			List<?> dominators = element.getDominators();
			if (dominators.isEmpty()) {
				System.out.println("<<empty>>");
			} else {
				for (Iterator<?> iter = dominators.iterator(); iter.hasNext();) {
					AbstractCfgNode dominator = (AbstractCfgNode) iter.next();
					System.out.println(dominator.toString() + ", " + dominator.getOriginalLineNumber());
				}
			}

		} else if (elementX instanceof LatticeElementBottom) {

			writer.write(linesep + "Bottom Element" + linesep + linesep);

		} else if (elementX == null) {

			writer.write(linesep + "<<null>>" + linesep + linesep);

		} else {
			throw new RuntimeException("SNH: " + elementX.getClass());
		}

		writer.flush();
	}

	private static boolean doNotDump(Variable var) {
		if (var.isTemp() || var.getName().endsWith(InternalStrings.gShadowSuffix)
				|| var.getName().endsWith(InternalStrings.gShadowSuffix)
				|| BuiltinFunctions.isBuiltinFunction(var.getSymbolTable().getName())) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	static public void dumpComplete(DependencyLatticeElement element, Writer writer) throws IOException {

		writer.write(linesep + "DEP MAPPINGS" + linesep + linesep);
		Map<?, ?> placeToDep = element.getPlaceToDep();
		for (Iterator<?> iterator = placeToDep.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();
			DependencySet depSet = (DependencySet) entry.getValue();
			writer.write(place + ":      " + depSet + linesep);
		}

		writer.write(linesep + "ARRAY LABELS" + linesep + linesep);
		Map<?, ?> arrayLabels = element.getArrayLabels();
		for (Iterator<?> iterator = arrayLabels.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Variable var = (Variable) entry.getKey();
			DependencySet arrayLabel = (DependencySet) entry.getValue();
			writer.write(var + ":      " + arrayLabel + linesep);
		}
	}

	static public void dump(DependencyLatticeElement element) {
		try {
			Writer writer = new OutputStreamWriter(System.out);
			dump(element, writer);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException("SNH:" + e.getStackTrace());
		}
	}

	@SuppressWarnings("rawtypes")
	static public void dump(DependencyLatticeElement element, Writer writer) throws IOException {
		writer.write(linesep + "TAINT MAPPINGS" + linesep + linesep);
		Map<?, ?> placeToDep = element.getPlaceToDep();
		for (Iterator<?> iterator = placeToDep.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			AbstractTacPlace place = (AbstractTacPlace) entry.getKey();
			if (place.isVariable()) {
				Variable var = place.getVariable();
				if (doNotDump(var)) {
					continue;
				}
			}
			DependencySet depSet = (DependencySet) entry.getValue();
			writer.write(place + ":      " + depSet + linesep);
		}
		writer.write(linesep + "ARRAY LABELS" + linesep + linesep);
		Map<?, ?> arrayLabels = element.getArrayLabels();
		for (Iterator<?> iterator = arrayLabels.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Variable var = (Variable) entry.getKey();
			if (doNotDump(var)) {
				continue;
			}
			DependencySet arrayLabel = (DependencySet) entry.getValue();
			writer.write(var + ":      " + arrayLabel + linesep);
		}
	}

	static public void dump(MustAliases mustAliases, Writer writer) throws IOException {
		Set<?> mustAliasGroups = mustAliases.getGroups();
		writer.write("u{ ");
		for (Iterator<?> iter = mustAliasGroups.iterator(); iter.hasNext();) {
			MustAliasGroup group = (MustAliasGroup) iter.next();
			dump(group, writer);
			writer.write(" ");
		}
		writer.write("}" + linesep);
	}

	static public void dump(MustAliasGroup mustAliasGroup, Writer writer) throws IOException {
		Set<?> group = mustAliasGroup.getVariables();
		writer.write("( ");
		for (Iterator<?> iter = group.iterator(); iter.hasNext();) {
			Variable var = (Variable) iter.next();
			writer.write(var + " ");
		}
		writer.write(")");
	}

	static public void dump(MayAliases mayAliases, Writer writer) throws IOException {
		Set<?> mayAliasPairs = mayAliases.getPairs();
		writer.write("a{ ");
		for (Iterator<?> iter = mayAliasPairs.iterator(); iter.hasNext();) {
			MayAliasPair pair = (MayAliasPair) iter.next();
			dump(pair, writer);
			writer.write(" ");
		}
		writer.write("}" + linesep);
	}

	static public void dump(MayAliasPair mayAliasPair, Writer writer) throws IOException {
		Set<?> pair = mayAliasPair.getPair();
		Object[] pairArray = pair.toArray();
		writer.write("(" + (Variable) pairArray[0] + " " + (Variable) pairArray[1] + ")" + linesep);
	}

	@SuppressWarnings("rawtypes")
	static public void dumpFunction2ECS(Map<?, ?> function2ECS) {
		for (Iterator<?> iter = function2ECS.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			TacFunction function = (TacFunction) entry.getKey();
			EncodedCallStrings ecs = (EncodedCallStrings) entry.getValue();
			System.out.println("ECS for Function " + function.getName() + ": ");
			System.out.println(ecs);
			System.out.println();
		}
	}

	@SuppressWarnings("rawtypes")
	static public void dumpECSStats(Map<?, ?> function2ECS) {

		List<List<Object>> output = new LinkedList<List<Object>>();
		for (Iterator<?> iter = function2ECS.entrySet().iterator(); iter.hasNext();) {

			Map.Entry entry = (Map.Entry) iter.next();
			TacFunction function = (TacFunction) entry.getKey();
			EncodedCallStrings ecs = (EncodedCallStrings) entry.getValue();
			int ecsLength = ecs.getCallStrings().size();
			long cfgSize = function.getCfg().size();
			long product = ecsLength * cfgSize;

			int insertAtIndex = 0;
			for (Iterator<List<Object>> iterator = output.iterator(); iterator.hasNext();) {
				List<?> nextList = (List<?>) iterator.next();
				int compProduct = ((Integer) nextList.get(3)).intValue();
				if (product > compProduct) {
					break;
				}
				insertAtIndex++;
			}

			List<Object> insertMe = new LinkedList<Object>();
			insertMe.add(function);
			insertMe.add(new Integer(ecsLength));
			insertMe.add(new Long(cfgSize));
			insertMe.add(new Long(product));
			output.add(insertAtIndex, insertMe);
		}

		int total = 0;
		for (Iterator<List<Object>> iter = output.iterator(); iter.hasNext();) {
			List<?> outputList = (List<?>) iter.next();
			TacFunction function = (TacFunction) outputList.get(0);
			int ecsLength = ((Integer) outputList.get(1)).intValue();
			int cfgSize = ((Integer) outputList.get(2)).intValue();
			int product = ((Integer) outputList.get(3)).intValue();
			System.out.println(function.getName() + ": " + ecsLength + ", " + cfgSize + ": " + product);
			total += product;
		}

		System.out.println();
		System.out.println("total product: " + total);
		System.out.println();
	}
}

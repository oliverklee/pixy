package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElementBottom;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractInterproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralAnalysisInformation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.EncodedCallStrings;
import at.ac.tuwien.infosys.www.pixy.analysis.intraprocedural.IntraproceduralAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.DummyLiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.*;

import java.io.*;
import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class Dumper {
    // auxiliary HashMap: CfgNode -> Integer
    private static HashMap<AbstractCfgNode, Integer> node2Int;
    private static int idCounter;
    static final String linesep = System.getProperty("line.separator");

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // since this class is stateless, there is no need to create an instance of it
    private Dumper() {
    }

// *********************************************************************************
// DOT: ParseTree ******************************************************************
// *********************************************************************************

// dumpDot(ParseTree, String, String) **********************************************

    // dumps the parse tree in dot syntax to the directory specified
    // by "path" and the file specified by "filename"
    static void dumpDot(ParseTree parseTree, String path, String filename) {

        // create directory
        (new File(path)).mkdir();

        try {
            Writer outWriter = new FileWriter(path + '/' + filename);
            dumpDot(parseTree, outWriter);
            outWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

// dumpDot(ParseTree, String, Writer) **********************************************

    // dumps the parse tree in dot syntax using the specified Writer
    static void dumpDot(ParseTree parseTree, Writer outWriter) {
        try {
            outWriter.write("digraph parse_tree {\n");
            dumpDot(parseTree.getRoot(), outWriter);
            outWriter.write("}\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

// dumpDot(ParseNode, Writer) ******************************************************

    // dumps the subtree starting at the given parse node in dot syntax
    static void dumpDot(ParseNode parseNode, Writer outWriter)
        throws java.io.IOException {

        outWriter.write("  n" + parseNode.getId() + " [label=\"");

        // print symbol
        String symbolName = parseNode.getName();
        outWriter.write(escapeDot(symbolName, 0));

        // print lexeme for token nodes
        if (parseNode.isToken()) {
            String lexeme = parseNode.getLexeme();
            outWriter.write("\\n");
            outWriter.write(escapeDot(lexeme, 10));
        }
        outWriter.write("\"];\n");

        // print edge to parent
        ParseNode parent = parseNode.getParent();
        if (parent != null) {
            outWriter.write("  n" + parent.getId() + " -> n" +
                parseNode.getId() + ";\n");
        }
        // recursion
        for (int i = 0; i < parseNode.getChildren().size(); i++) {
            dumpDot(parseNode.getChild(i), outWriter);
        }
    }

// *********************************************************************************
// DOT: TacFunction ****************************************************************
// *********************************************************************************

// dumpDot(TacFunction, String, boolean) *******************************************

    // dumps the function's ControlFlowGraph in dot syntax
    public static void dumpDot(TacFunction function, String graphPath, boolean dumpParams) {
        dumpDot(function.getControlFlowGraph(), function.getName(), graphPath);

        if (dumpParams) {
            for (TacFormalParameter parameter : function.getParams()) {
                String paramString = parameter.getVariable().getName();
                paramString = paramString.substring(1); // remove "$"
                if (parameter.hasDefault()) {
                    dumpDot(
                        parameter.getDefaultControlFlowGraph(),
                        function.getName() + "_" + paramString,
                        graphPath);
                }
            }
        }
    }

// dumpDot(ControlFlowGraph, String) ************************************************************

    static void dumpDot(ControlFlowGraph controlFlowGraph, String graphName, String graphPath) {
        dumpDot(controlFlowGraph, graphName, graphPath, graphName + ".dot");
    }

// dumpDot(ControlFlowGraph, String, String, String) ********************************************

    // dumps the ControlFlowGraph in dot syntax to the directory specified by "path" and the
    // file specified by "filename"
    public static void dumpDot(ControlFlowGraph controlFlowGraph, String graphName, String path, String filename) {

        // create directory
        (new File(path)).mkdir();

        try {
            Writer outWriter = new FileWriter(path + "/" + filename);
            dumpDot(controlFlowGraph, graphName, outWriter);
            outWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

// dumpDot(ControlFlowGraph, String, Writer) ****************************************************

    // dumps the ControlFlowGraph in dot syntax using the specified Writer
    static void dumpDot(ControlFlowGraph controlFlowGraph, String graphName, Writer outWriter) {

        try {
            Dumper.node2Int = new HashMap<>();
            Dumper.idCounter = 0;
            outWriter.write("digraph controlFlowGraph {\n  label=\"");
            outWriter.write(escapeDot(graphName, 0));
            outWriter.write("\";\n");
            outWriter.write("  labelloc=t;\n");
            dumpDot(controlFlowGraph.getHead(), outWriter);
            outWriter.write("}\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

// dumpDot(CfgNode) ****************************************************************

    // recursively dumps the CfgNode in dot syntax
    // and returns the ID that is assigned to this node
    static int dumpDot(AbstractCfgNode cfgNode, Writer outWriter)
        throws java.io.IOException {

        // mark node as visited and store ID
        int nodeId = Dumper.idCounter;
        Dumper.node2Int.put(cfgNode, Dumper.idCounter++);

        // print node
        String name = makeCfgNodeName(cfgNode);
        outWriter.write("  n" + nodeId + " [label=\"" + name + "\"];\n");

        // handle successors
        int succId;
        for (int i = 0; i < 2; i++) {

            CfgEdge outEdge = cfgNode.getOutEdge(i);

            if (outEdge != null) {

                AbstractCfgNode succNode = outEdge.getDestination();

                // print successor
                Integer succIdInt = Dumper.node2Int.get(succNode);
                if (succIdInt == null) {
                    succId = dumpDot(succNode, outWriter);
                } else {
                    succId = succIdInt;
                }

                // print edge to successor
                outWriter.write("  n" + nodeId + " -> n" + succId);
                if (outEdge.getType() != CfgEdge.NORMAL_EDGE) {
                    outWriter.write(" [label=\"" + outEdge.getName() + "\"]");
                }
                outWriter.write(";\n");
            }
        }

        return nodeId;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

// dump(TacFunction) ***************************************************************

    // dumps function information
    public static void dump(TacFunction function) {
        System.out.println("***************************************");
        System.out.println("Function " + function.getName());
        System.out.println("***************************************");
        System.out.println();
        if (function.isReference()) {
            System.out.println("isReference");
        }
        for (TacFormalParameter param : function.getParams()) {
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

// makeCfgNodeName(CfgNode) ********************************************************

    // creates a string representation for the given cfg node
    public static String makeCfgNodeName(AbstractCfgNode cfgNodeX) {
        if (cfgNodeX instanceof BasicBlock) {
            BasicBlock cfgNode = (BasicBlock) cfgNodeX;
            StringBuilder label = new StringBuilder("basic block\\n");
            for (AbstractCfgNode containedNode : cfgNode.getContainedNodes()) {
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

            return (
                leftString +
                    " = " +
                    leftOperandString +
                    " " + TacOperators.opToName(op) + " " +
                    rightOperandString);
        } else if (cfgNodeX instanceof AssignUnary) {
            AssignUnary cfgNode = (AssignUnary) cfgNodeX;
            String leftString = getPlaceString(cfgNode.getLeft());
            String rightString = getPlaceString(cfgNode.getRight());
            int op = cfgNode.getOperator();

            return (
                leftString +
                    " = " +
                    " " + TacOperators.opToName(op) + " " +
                    rightString);
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

            return (
                "if " +
                    leftOperandString +
                    " " + TacOperators.opToName(op) + " " +
                    rightOperandString);
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

            // construct parameter list
            StringBuilder paramListStringBuf = new StringBuilder();
            for (Iterator<TacActualParameter> iter = cfgNode.getParamList().iterator(); iter.hasNext(); ) {
                TacActualParameter param = iter.next();
                if (param.isReference()) {
                    paramListStringBuf.append("&");
                }
                paramListStringBuf.append(getPlaceString(param.getPlace()));
                if (iter.hasNext()) {
                    paramListStringBuf.append(", ");
                }
            }

            return (
                "prepare " +
                    cfgNode.getFunctionNamePlace().toString() + "(" +
                    paramListStringBuf.toString() + ")");
        } else if (cfgNodeX instanceof CallReturn) {
            CallReturn cfgNode = (CallReturn) cfgNodeX;
            return ("call-return (" + cfgNode.getTempVar() + ")");
        } else if (cfgNodeX instanceof CallBuiltinFunction) {
            CallBuiltinFunction cfgNode = (CallBuiltinFunction) cfgNodeX;

            // construct parameter list
            StringBuilder paramListStringBuf = new StringBuilder();
            for (Iterator<TacActualParameter> iter = cfgNode.getParamList().iterator(); iter.hasNext(); ) {
                TacActualParameter param = iter.next();
                if (param.isReference()) {
                    paramListStringBuf.append("&");
                }
                paramListStringBuf.append(getPlaceString(param.getPlace()));
                if (iter.hasNext()) {
                    paramListStringBuf.append(", ");
                }
            }

            return (
                cfgNode.getFunctionName() + "(" +
                    paramListStringBuf.toString() + ") " + "<" +
                    getPlaceString(cfgNode.getTempVar()) + ">");
        } else if (cfgNodeX instanceof CallUnknownFunction) {
            CallUnknownFunction cfgNode = (CallUnknownFunction) cfgNodeX;

            // construct parameter list
            StringBuilder paramListStringBuf = new StringBuilder();
            for (Iterator<TacActualParameter> iter = cfgNode.getParamList().iterator(); iter.hasNext(); ) {
                TacActualParameter param = iter.next();
                if (param.isReference()) {
                    paramListStringBuf.append("&");
                }
                paramListStringBuf.append(getPlaceString(param.getPlace()));
                if (iter.hasNext()) {
                    paramListStringBuf.append(", ");
                }
            }

            return ("UNKNOWN: " +
                cfgNode.getFunctionName() + "(" +
                paramListStringBuf.toString() + ") " + "<" +
                getPlaceString(cfgNode.getTempVar()) + ">");
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
            String evalMe = cfgNode.getRight().getVariable().toString();
            String leftString = cfgNode.getLeft().getVariable().toString();
            return (leftString + " = " + "eval(" + evalMe + ")");
        } else if (cfgNodeX instanceof Define) {
            Define cfgNode = (Define) cfgNodeX;
            return ("define(" +
                cfgNode.getSetMe() + ", " +
                cfgNode.getSetTo() + ", " +
                cfgNode.getCaseInsensitive() + ")");
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

// getPlaceString ******************************************************************

    static String getPlaceString(AbstractTacPlace place) {
        if (place.isVariable()) {
            return place.toString();
        } else if (place.isConstant()) {
            return place.toString();
        } else {
            return escapeDot(place.toString(), 20);
        }
    }

// escapeDot ***********************************************************************

    // escapes special characters in the given string, making it suitable for
    // dot output; if the string's length exceeds the given limit, "..." is
    // returned
    static public String escapeDot(String escapeMe, int limit) {
        if (limit > 0 && escapeMe.length() > limit) {
            return "...";
        }
        StringBuilder escaped = new StringBuilder(escapeMe);
        for (int i = 0; i < escaped.length(); i++) {
            char inspectMe = escaped.charAt(i);
            if (inspectMe == '\n' || inspectMe == '\r') {
                // delete these control characters
                escaped.deleteCharAt(i);
                i--;
            } else if (inspectMe == '"' || inspectMe == '\\') {
                // escape this character by prefixing it with a backslash
                escaped.insert(i, '\\');
                i++;
            }
        }
        return escaped.toString();
    }

// dump(ParseTree) *****************************************************************

    // dumps the parse tree
    static void dump(ParseTree parseTree) {
        recursiveDump(parseTree.getRoot(), 0);
    }

// dump(ParseNode) *****************************************************************

    // dumps only the current parse node
    static public void dump(ParseNode parseNode, int level) {
        StringBuilder buf = new StringBuilder(level);
        for (int i = 0; i < level; i++) {
            buf.append(" ");
        }
        String spaces = buf.toString();

        System.out.print(spaces + "Sym: " + parseNode.getSymbol() + ", Name: " +
            parseNode.getName());
        if (parseNode.getLexeme() != null) {
            System.out.print(", Lex: " + parseNode.getLexeme() + ", lineno: " +
                parseNode.getLineno());
        }
        System.out.println();
    }

// recursiveDump *******************************************************************

    // dumps the subtree starting at the current parse node
    static public void recursiveDump(ParseNode parseNode, int level) {
        dump(parseNode, level);
        for (ParseNode child : parseNode.getChildren()) {
            recursiveDump(child, level + 1);
        }
    }

// dump(SymbolTable) ***************************************************************

    static public void dump(SymbolTable symbolTable, String name) {
        System.out.println("***************************************");
        System.out.println("Symbol Table: " + name);
        System.out.println("***************************************");
        System.out.println();
        for (Variable variable : symbolTable.getVariables().values()) {
            dump(variable);
            System.out.println();
        }
    }

// dump(Variable) ******************************************************************

    static public void dump(Variable variable) {
        System.out.println(variable);

        // if it is an array
        if (variable.isArray()) {
            System.out.println("isArray:            true");

            List<Variable> elements = variable.getElements();
            if (!elements.isEmpty()) {
                System.out.print("elements:           ");
                for (Variable element : elements) {
                    System.out.print(element.getName() + " ");
                }
                System.out.println();
            }
        }

        // if it is an array element
        if (variable.isArrayElement()) {
            System.out.println("isArrayElement:     true");
            System.out.println("enclosingArray:     " +
                variable.getEnclosingArray().getName());
            System.out.println("topEnclosingArray:  " +
                variable.getTopEnclosingArray().getName());
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
            for (AbstractTacPlace index : variable.getIndices()) {
                System.out.print(index + " ");
            }
            System.out.println();
        }

        // if it is a variable variable
        AbstractTacPlace depPlace = variable.getDependsOn();
        if (depPlace != null) {
            System.out.println("dependsOn:          " + depPlace.toString());
        }

        // print array elements indexed by this variable
        List<Variable> indexFor = variable.getIndexFor();
        if (!indexFor.isEmpty()) {
            System.out.print("indexFor:           ");
            for (Variable indexed : indexFor) {
                System.out.print(indexed + " ");
            }
            System.out.println();
        }
    }

// dump(ConstantsTable) ************************************************************

    static public void dump(ConstantsTable constantsTable) {
        System.out.println("***************************************");
        System.out.println("Constants Table ");
        System.out.println("***************************************");
        System.out.println();
        for (Constant constant : constantsTable.getConstants().values()) {
            System.out.println(constant.getLabel());
        }
        System.out.println();
        System.out.println("Insensitive Groups:");
        System.out.println();
        for (List<Constant> insensitiveGroup : constantsTable.getInsensitiveGroups().values()) {
            System.out.print("* ");
            for (Constant anInsensitiveGroup : insensitiveGroup) {
                System.out.print(anInsensitiveGroup.getLabel() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    static public void dump(InclusionDominatorAnalysis analysis) {
        for (Map.Entry<AbstractCfgNode, AbstractAnalysisNode> entry : analysis.getAnalysisInfo().getMap().entrySet()) {
            AbstractCfgNode cfgNode = entry.getKey();
            IntraproceduralAnalysisNode analysisNode = (IntraproceduralAnalysisNode) entry.getValue();
            System.out.println("dominators for cfg node " + cfgNode.toString() + ", " + cfgNode.getOriginalLineNumber());
            Dumper.dump(analysisNode.getInValue());
        }
    }

    static public void dump(AbstractInterproceduralAnalysis analysis, String path, String filename) {

        // create directory
        (new File(path)).mkdir();

        try {
            Writer writer = new FileWriter(path + '/' + filename);

            // nothing to do for dummy analysis
            if (analysis instanceof DummyLiteralAnalysis || analysis instanceof DummyAliasAnalysis) {
                writer.write("Dummy Analysis" + linesep);
                writer.close();
                return;
            }

            List<TacFunction> functions = analysis.getFunctions();
            InterproceduralAnalysisInformation analysisInfoNew = analysis.getInterproceduralAnalysisInformation();

            if (analysis instanceof LiteralAnalysis) {
                writer.write(linesep + "Default Lattice Element:" + linesep + linesep);
                dump(LiteralLatticeElement.DEFAULT, writer);
            }

            // for each function...
            for (TacFunction function : functions) {
                ControlFlowGraph controlFlowGraph = function.getControlFlowGraph();
                writer.write(linesep + "****************************************************" + linesep);
                writer.write(function.getName() + linesep);
                writer.write("****************************************************" + linesep + linesep);
                // for each ControlFlowGraph node...
                for (Iterator<AbstractCfgNode> bft = controlFlowGraph.bfIterator(); bft.hasNext(); ) {
                    AbstractCfgNode cfgNode = bft.next();
                    writer.write("----------------------------------------" + linesep);
                    writer.write(cfgNode.getFileName() + ", " + cfgNode.getOriginalLineNumber() +
                        ", " + makeCfgNodeName(cfgNode) + linesep);
                    dump(analysisInfoNew.getAnalysisNode(cfgNode).getRecycledFoldedValue(), writer);
                }
                writer.write("----------------------------------------" + linesep);
            }

            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

// dump(AnalysisNode) **************************************************************

    static public void dump(AbstractInterproceduralAnalysisNode node) {
        System.out.print("Transfer Function: ");
        try {
            System.out.println(node.getTransferFunction().getClass().getName());
        } catch (NullPointerException e) {
            System.out.println("<<null>>");
        }
        // dump the lattice element for each context
        for (AbstractLatticeElement element : node.getPhi().values()) {
            System.out.println("~~~~~~~~~~~~~~~");
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

//  dump(LatticeElement) ************************************************************

    static public void dump(AbstractLatticeElement elementX, Writer writer) throws IOException {
        if (elementX instanceof AliasLatticeElement) {
            AliasLatticeElement element = (AliasLatticeElement) elementX;
            dump(element.getMustAliases(), writer);
            dump(element.getMayAliases(), writer);
        } else if (elementX instanceof LiteralLatticeElement) {
            LiteralLatticeElement element = (LiteralLatticeElement) elementX;

            // dump non-default literal mappings
            for (Map.Entry<AbstractTacPlace, Literal> entry : element.getPlaceToLit().entrySet()) {
                AbstractTacPlace place = entry.getKey();
                Literal lit = entry.getValue();
                writer.write(place + ":      " + lit + linesep);
            }
        } else if (elementX instanceof DependencyLatticeElement) {
            dumpComplete((DependencyLatticeElement) elementX, writer);
        } else if (elementX instanceof InclusionDominatorLatticeElement) {
            InclusionDominatorLatticeElement element = (InclusionDominatorLatticeElement) elementX;
            List<AbstractCfgNode> dominators = element.getDominators();
            if (dominators.isEmpty()) {
                System.out.println("<<empty>>");
            } else {
                for (AbstractCfgNode dominator : dominators) {
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

    // returns true if this variable should not be dumped, because it is a
    // - temporary
    // - shadow
    // - variable of a builtin function
    private static boolean doNotDump(Variable var) {
        // EFF: "endsWith" technique not too elegant; might also lead
        // to "rootkit effects"...; alternative would be: save additional
        // field for variables
        return var.isTemp() ||
            var.getName().endsWith(InternalStrings.gShadowSuffix) ||
            var.getName().endsWith(InternalStrings.gShadowSuffix) ||
            BuiltinFunctions.isBuiltinFunction(var.getSymbolTable().getName());
    }

//  ********************************************************************************

    static public void dumpComplete(DependencyLatticeElement element, Writer writer)
        throws IOException {

        // dump non-default dependency mappings
        writer.write(linesep + "DEP MAPPINGS" + linesep + linesep);
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : element.getPlaceToDep().entrySet()) {
            AbstractTacPlace place = entry.getKey();
            DependencySet dependencySet = entry.getValue();
            writer.write(place + ":      " + dependencySet + linesep);
        }

        // dump non-default array labels
        writer.write(linesep + "ARRAY LABELS" + linesep + linesep);
        for (Map.Entry<Variable, DependencySet> entry : element.getArrayLabels().entrySet()) {
            Variable var = entry.getKey();
            DependencySet arrayLabel = entry.getValue();
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

    // like dumpComplete, but only prints
    // - non-temporaries
    // - non-shadows
    // - variables of non-builtin functions
    static public void dump(DependencyLatticeElement element, Writer writer) throws IOException {
        // dump non-default taint mappings
        writer.write(linesep + "TAINT MAPPINGS" + linesep + linesep);
        for (Map.Entry<AbstractTacPlace, DependencySet> entry : element.getPlaceToDep().entrySet()) {
            AbstractTacPlace place = entry.getKey();
            if (place.isVariable()) {
                Variable var = place.getVariable();
                if (doNotDump(var)) {
                    continue;
                }
            }
            writer.write(place + ":      " + entry.getValue() + linesep);
        }

        // dump non-default array labels
        writer.write(linesep + "ARRAY LABELS" + linesep + linesep);
        for (Map.Entry<Variable, DependencySet> entry : element.getArrayLabels().entrySet()) {
            Variable variable = entry.getKey();
            if (doNotDump(variable)) {
                continue;
            }
            writer.write(variable + ":      " + entry.getValue() + linesep);
        }
    }

    static public void dump(MustAliases mustAliases, Writer writer) throws IOException {
        writer.write("u{ ");
        for (MustAliasGroup group : mustAliases.getGroups()) {
            dump(group, writer);
            writer.write(" ");
        }
        writer.write("}" + linesep);
    }

    static public void dump(MustAliasGroup mustAliasGroup, Writer writer) throws IOException {
        writer.write("( ");
        for (Variable variable : mustAliasGroup.getVariables()) {
            writer.write(variable + " ");
        }
        writer.write(")");
    }

    static public void dump(MayAliases mayAliases, Writer writer) throws IOException {
        writer.write("a{ ");
        for (MayAliasPair pair : mayAliases.getPairs()) {
            dump(pair, writer);
            writer.write(" ");
        }
        writer.write("}" + linesep);
    }

    static public void dump(MayAliasPair mayAliasPair, Writer writer) throws IOException {
        Set<Variable> pair = mayAliasPair.getPair();
        Object[] pairArray = pair.toArray();
        writer.write("(" + pairArray[0] + " " + pairArray[1] + ")" + linesep);
    }

    static public void dumpFunction2ECS(Map<TacFunction, EncodedCallStrings> function2ECS) {
        for (Map.Entry<TacFunction, EncodedCallStrings> entry : function2ECS.entrySet()) {
            TacFunction function = entry.getKey();
            EncodedCallStrings encodedCallStrings = entry.getValue();
            System.out.println("EncodedCallStrings for Function " + function.getName() + ": ");
            System.out.println(encodedCallStrings);
            System.out.println();
        }
    }
}
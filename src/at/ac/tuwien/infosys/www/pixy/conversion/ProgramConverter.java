package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.phpparser.PhpLexer;
import at.ac.tuwien.infosys.www.phpparser.PhpParser;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.DummyAliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.*;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.type.TypeAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.conversion.includes.IncludeGraph;

import java.io.*;
import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ProgramConverter {
    // = the directory in which the given entry file is located
    private File workingDirectoryFile;

    // include graph used for detecting recursive include relationships
    private IncludeGraph includeGraph;

    // the converter for the base file, will also be the resulting converter
    // when we've finished our work
    private TacConverter baseTac;

    private int numberOfConvertedFiles = 0;

    // the analysis responsible for resolving and including includes
    private LiteralAnalysis literalAnalysis;

    // recognize hotspot nodes for JUnit tests?
    private boolean specialNodes;

    // is a real alias analysis used?
    private boolean useAliasAnalysis;

    // line counter (builtin functions file is not subtracted automatically)
    private int numberOfLines = 0;
    private boolean countLines = false;

    // File objects of all included files as well as the entry file
    private Set<File> allFiles = new HashSet<>();

    // set of include nodes that should be skipped (don't try to include them)
    private Set<Include> skipUs = new HashSet<>();

    private SymbolTable superSymbolTable;

    private enum InclusionStatus {NOT_FOUND, INCLUDED, CYCLIC}

    // type analysis (for resolving ambiguous method calls)
    private TypeAnalysis typeAnalysis;

//  ********************************************************************************
//  CONSTRUCTOR ********************************************************************
//  ********************************************************************************

    public ProgramConverter(boolean specialNodes, boolean useAliasAnalysis) {
        // determine working directory (= directory of the entry file)
        this.workingDirectoryFile = MyOptions.entryFile.getParentFile();

        this.includeGraph = new IncludeGraph(MyOptions.entryFile);

        this.specialNodes = specialNodes;
        this.useAliasAnalysis = useAliasAnalysis;

        this.allFiles.add(MyOptions.entryFile);

        initializeSuperglobalsSymbolTable();
    }

    private void initializeSuperglobalsSymbolTable() {
        this.superSymbolTable = new SymbolTable("_superglobals", true);

        this.addSuperGlobal("$GLOBALS");
        this.addSuperGlobal("$_SERVER");
        this.addSuperGlobal("$HTTP_SERVER_VARS");
        this.addSuperGlobal("$_GET");
        this.addSuperGlobal("$HTTP_GET_VARS");
        this.addSuperGlobal("$_POST");
        this.addSuperGlobal("$HTTP_POST_VARS");
        this.addSuperGlobal("$_COOKIE");
        this.addSuperGlobal("$HTTP_COOKIE_VARS");
        this.addSuperGlobal("$_FILES");
        this.addSuperGlobal("$HTTP_POST_FILES");
        this.addSuperGlobal("$_ENV");
        this.addSuperGlobal("$HTTP_ENV_VARS");
        this.addSuperGlobal("$_REQUEST");
        this.addSuperGlobal("$_SESSION");
        this.addSuperGlobal("$HTTP_SESSION_VARS");
    }

//  ********************************************************************************
//  GET ****************************************************************************
//  ********************************************************************************

//  getTac *************************************************************************

    public TacConverter getTac() {
        return this.baseTac;
    }

// getSuperSymbolTable *************************************************************

    public SymbolTable getSuperSymbolTable() {
        return this.superSymbolTable;
    }

//  ********************************************************************************
//  OTHER **************************************************************************
//  ********************************************************************************

//  convert ************************************************************************

    public void convert() {
        convertEntryFile();

        List<Include> processUs = baseTac.getIncludeNodes();
        boolean goOn = true;  // start a new iteration?

        // STATS
        //
        // include nodes that could not be resolved
        Set<Include> topIncludes = new HashSet<>();
        //
        // literal includes for which the target file could not be found
        SortedMap<Include, String> notFoundLiteralIncludes = new TreeMap<>();
        //
        // dynamic includes for which the target file could not be found
        SortedMap<Include, String> notFoundDynamicIncludes = new TreeMap<>();
        //
        // number of included literal includes
        int resolvedLit = 0;
        //
        // number of included non-literal includes;
        int resolvedNonLit = 0;
        //
        // number of cyclic includes;
        int cyclic = 0;
        //
        // number of "not found" includes
        int notFound = 0;
        //
        // number of iterations
        int iteration = 0;

        while (goOn && !MyOptions.optionW) {
            iteration++;

            goOn = false;
            boolean nonLiteralIncludes = false; // are there non-literal includes?
            List<Include> weComeAfterwards;

            // resolution of literal includes *****************************

            System.out.println();
            System.out.println("*** resolving literal includes ***");
            System.out.println();

            while (!processUs.isEmpty()) {
                // auxiliary list: will be filled with the include nodes
                // contained in included files
                weComeAfterwards = new LinkedList<>();

                // process all literal include nodes in "processUs"
                for (Include includeNode : processUs) {
                    if (this.skipUs.contains(includeNode)) {
                        continue;
                    }
                    if (!includeNode.isLiteral()) {
                        nonLiteralIncludes = true;
                        continue;
                    }
                    InclusionStatus status = this.include(includeNode.getIncludeMe().toString(), includeNode,
                        includeNode.getIncludeFunction(), weComeAfterwards);

                    switch (status) {
                        case INCLUDED:
                            // fine!
                            resolvedLit++;
                            break;
                        case NOT_FOUND:
                            // a literal include that was not found;
                            // there is no need to retry
                            this.skipUs.add(includeNode);
                            notFound++;
                            notFoundLiteralIncludes.put(includeNode, includeNode.getIncludeMe().toString());
                            break;
                        case CYCLIC:
                            this.skipUs.add(includeNode);
                            cyclic++;
                            break;
                        default:
                            throw new RuntimeException("SNH");
                    }
                }

                processUs = weComeAfterwards;
            }
            System.out.println();

            // assign functions to cfg nodes
            this.baseTac.assignFunctionsToControlFlowGraphNodes();

            // resolution of non-literal includes **********************************

            // if there are no non-literal includes, we don't have anything
            // to resolve
            if (!nonLiteralIncludes) {
                break;
            }

            System.out.println();
            System.out.println("*** resolving non-literal includes ***");
            System.out.println();

            // requires literal analysis and hence, preliminary backpatching
            this.baseTac.backpatch();

            int kSize = 1;
            ConnectorComputation connectorComp = new ConnectorComputation(
                baseTac.getAllFunctions(), baseTac.getMainFunction(), kSize);
            connectorComp.compute();
            InterproceduralWorklist workList = new InterproceduralWorklistBetter(new InterproceduralWorklistOrder(baseTac, connectorComp));
            connectorComp.stats(false);

            AliasAnalysis aliasAnalysis = new DummyAliasAnalysis();

            literalAnalysis = new LiteralAnalysis(
                baseTac, aliasAnalysis, new CallStringAnalysis(connectorComp), workList);
            literalAnalysis.analyze();

            processUs = literalAnalysis.getIncludeNodes();
            weComeAfterwards = new LinkedList<>();
            notFoundDynamicIncludes = new TreeMap<>();
            topIncludes = new HashSet<>();

            for (Include includeNode : processUs) {
                if (this.skipUs.contains(includeNode)) {
                    continue;
                }

                Literal includedLit = literalAnalysis.getLiteral(includeNode.getIncludeMe(), includeNode);
                String includedString = null;

                if (includedLit == Literal.TOP) {

                    // try heuristics
                    List<String> includeTargets = ParseNodeHeuristics.getPossibleIncludeTargets(
                        includeNode, literalAnalysis, notFoundDynamicIncludes,
                        this.workingDirectoryFile.getPath());

                    if (includeTargets == null) {
                        // more than one possibility
                        topIncludes.add(includeNode);
                        continue;
                    } else if (includeTargets.isEmpty()) {
                        continue;
                    } else if (includeTargets.size() == 1) {
                        // heuristics were successful!
                        includedString = includeTargets.get(0);
                    } else {
                        throw new RuntimeException("SNH");
                    }
                } else {
                    includedString = includedLit.toString();
                }

                // include!
                InclusionStatus status = this.include(includedString, includeNode, includeNode.getIncludeFunction(),
                    weComeAfterwards);

                switch (status) {
                    case INCLUDED:
                        // we have included something, so continue with the next iteration
                        goOn = true;
                        resolvedNonLit++;
                        break;
                    case CYCLIC:
                        this.skipUs.add(includeNode);
                        cyclic++;
                        break;
                    case NOT_FOUND:
                        // a non-literal include that was not found:
                        // perhaps we will succeed in a later iteration...
                        notFoundDynamicIncludes.put(includeNode, includedString);
                        break;
                    default:
                        throw new RuntimeException("SNH");
                }
            }

            System.out.println();

            // assign functions to cfg nodes
            this.baseTac.assignFunctionsToControlFlowGraphNodes();

            processUs = weComeAfterwards;
            processUs.addAll(topIncludes);  // maybe they will become resolvable in the next iteration
            processUs.addAll(notFoundDynamicIncludes.keySet()); // _,,_
        }

        // don't generate warnings for unreachables
        this.removeUnreachables(topIncludes, notFoundDynamicIncludes);

        // not needed any more ( => save memory)
        this.literalAnalysis = null;

        if (this.useAliasAnalysis) {
            // final, verbose backpatching
            this.baseTac.backpatch(true, true, null, null);

            // shadow generation
            this.baseTac.generateShadows();
        } else {
            // approximate effects of "global" statements
            this.baseTac.replaceGlobals();

            // backpatch with methods so that type analysis can do its work
            this.baseTac.backpatch(true, false, null, null);

            // if there were ambiguous method calls:
            // try to resolve them by means of a type analysis;
            // EFF: perform type analysis conditionally; currently, type analysis
            // is outside of the condition (for testing purposes)

            System.out.println();
            System.out.println("*** performing type analysis ***");
            System.out.println();

            ConnectorComputation connectorComp = new ConnectorComputation(
                baseTac.getAllFunctions(), baseTac.getMainFunction(), 0);
            connectorComp.compute();
            InterproceduralWorklist workList = new InterproceduralWorklistBetter(new InterproceduralWorklistOrder(baseTac, connectorComp));
            this.typeAnalysis = new TypeAnalysis(
                this.baseTac, new CallStringAnalysis(connectorComp), workList);
            typeAnalysis.analyze();

            // final, verbose backpatching
            this.baseTac.backpatch(true, true, typeAnalysis, connectorComp.getCallGraph());
        }

        // summarize into basic blocks;
        // leads to a reduced amount of used memory
        if (MyOptions.optionV) {
            System.out.println("creating basic blocks");
        }
        this.baseTac.createBasicBlocks();

        // assign functions to cfg nodes
        this.baseTac.assignFunctionsToControlFlowGraphNodes();

        if (this.countLines) {
            System.out.println("Lines: " + this.numberOfLines);
        }

        // statistics output
        if (!MyOptions.optionW) {
            System.out.println();
            System.out.println("inclusion iterations:            " + iteration);
            System.out.println("resolved literal includes:       " + resolvedLit);
            System.out.println("resolved non-literal includes:   " + resolvedNonLit);
            System.out.println("cyclic includes:                 " + cyclic);
            System.out.println("not found includes:              " +
                (notFoundLiteralIncludes.size() + notFoundDynamicIncludes.size()));
            for (Map.Entry<Include, String> entry : notFoundLiteralIncludes.entrySet()) {
                System.out.println("- " + entry.getKey().getLoc());
                System.out.println("   [" + entry.getValue() + "]");
            }
            for (Map.Entry<Include, String> entry : notFoundDynamicIncludes.entrySet()) {
                System.out.println("- " + entry.getKey().getLoc());
                System.out.println("   [" + entry.getValue() + "]");
            }
            System.out.println("unresolved non-literal includes: " + topIncludes.size());
            List<Include> topIncludesList = new LinkedList<>(topIncludes);
            Collections.sort(topIncludesList);
            for (Include includeNode : topIncludesList) {
                System.out.println("- " + includeNode.getLoc());
            }
            System.out.println();

            // dump include relationships
            Utils.writeToFile(this.includeGraph.dump(),
                MyOptions.graphPath + "/includes_" + MyOptions.entryFile.getName() + ".txt");
        }

        // we don't need these any more
        this.literalAnalysis = null;
        this.includeGraph = null;
        this.skipUs = null;

        this.baseTac.addSuperGlobalElements();

        if (!MyOptions.optionB && !MyOptions.optionW) {
            this.baseTac.stats();
        }
        System.out.println();

        // final node order
        // EFF: only needed for functional analyses
        baseTac.assignReversePostOrder();
    }

    /**
     * Converts the entry file and sets baseTac.
     */
    private void convertEntryFile() {
        ParseTree parseTree = this.parse(MyOptions.entryFile.getPath());
        baseTac = new TacConverter(
            parseTree, this.specialNodes, this.numberOfConvertedFiles++, MyOptions.entryFile, this
        );
        baseTac.convert();
    }

//  ********************************************************************************

    // - input: a set of unresolved or not found includes
    // - output: the same set, but without those that are inside functions
    //   that are never called
    private void removeUnreachables(Set<Include> includeSet, Map<Include, String> includeMap) {
        for (Iterator<Include> iter = includeSet.iterator(); iter.hasNext(); ) {
            Include includeNode = iter.next();
            AbstractInterproceduralAnalysisNode incAnNode = literalAnalysis.getAnalysisNode(includeNode);
            if (incAnNode.getPhi().isEmpty()) {
                iter.remove();
            }
        }

        for (Iterator<Map.Entry<Include, String>> iter = includeMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Include, String> entry = iter.next();
            Include includeNode = entry.getKey();
            AbstractInterproceduralAnalysisNode incAnNode = literalAnalysis.getAnalysisNode(includeNode);
            if (incAnNode.getPhi().isEmpty()) {
                iter.remove();
            }
        }
    }

    public ParseTree parse(String fileName) {
        String canonicalFileName = createUniqueFileName(fileName);

        ParseTree parseTree = null;
        try {
            PhpLexer lexer = new PhpLexer(new FileReader(canonicalFileName));
            lexer.setFileName(canonicalFileName);
            PhpParser parser = new PhpParser(lexer);
            ParseNode rootNode = (ParseNode) parser.parse().value;
            parseTree = new ParseTree(rootNode);
        } catch (FileNotFoundException e) {
            Utils.bail("File not found: " + canonicalFileName);
        } catch (Exception e) {
            if (!MyOptions.optionW) {
                Utils.bail("Error parsing " + canonicalFileName);
            } else {
                Utils.bail();
            }
        }

        if (this.countLines) {
            this.numberOfLines += this.countLines(canonicalFileName);
        }

        return parseTree;
    }

    private String createUniqueFileName(String fileName) {
        try {
            fileName = (new File(fileName)).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return fileName;
    }

    // turns the name of a file to be included into a File object;
    // returns null if there is no such file
    private File makeFile(String fileName, File includingFile) {
        // first check whether the given file name is absolute
        File findMe = new File(fileName);
        if (findMe.isAbsolute()) {
            if (findMe.isFile()) {
                // found it!
                return findMe;
            }
        }

        // try to include the file relative to working directory first:
        // for each specified include path, append it to the working directory
        // and search there for the file that is to be included
        for (File includePath : MyOptions.includePaths) {
            File searchIn;  // directory to search in
            if (includePath.isAbsolute()) {
                searchIn = includePath;
            } else {
                searchIn = new File(this.workingDirectoryFile, includePath.getPath());
            }

            findMe = new File(searchIn, fileName);
            if (findMe.isFile()) {
                // found it!
                return findMe;
            }
        }

        // next: inclusion relative to the directory of the script file
        // that contains the include node (includingFile);
        // but only if the given file name doesn't start with "." or ".."

        if (!(fileName.startsWith("./") || fileName.startsWith("../"))) {
            for (File includePath : MyOptions.includePaths) {
                // we've already dealt with absolute include paths in the previous loop
                if (includePath.isAbsolute()) {
                    continue;
                }

                try {
                    File searchIn = new File(
                        includingFile.getCanonicalFile().getParentFile(),
                        includePath.getPath());
                    findMe = new File(searchIn, fileName);
                    if (findMe.isFile()) {
                        // found it!
                        return findMe;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        // file not found
        return null;
    }

//  include ************************************************************************

    // function: the one that contains the given include node;
    public InclusionStatus include(String includedFileName, Include includeNode, TacFunction function,
                             List<Include> includeNodes) {
        File includingFile = includeNode.getFile();
        File includedFile = this.makeFile(includedFileName, includingFile);
        if (includedFile == null) {
            return InclusionStatus.NOT_FOUND;
        }
        try {
            this.allFiles.add(includedFile.getCanonicalFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // approximation for handling recursions:
        // if this is a recursive include (include graph would get cyclic), ignore it
        boolean acyclic = this.includeGraph.addAcyclicEdge(includeNode.getFile(), includedFile);

        // get canonical path of included file
        String includedFilePath = null;
        try {
            includedFilePath = includedFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (acyclic) {
            if (!MyOptions.optionB) {
                System.out.print(".");
            }
            ParseTree parseTree = this.parse(includedFilePath);
            TacConverter tac = new TacConverter(
                parseTree, this.specialNodes, this.numberOfConvertedFiles++, includedFile, this);
            tac.convert();
            this.baseTac.include(tac, includeNode, function);
            includeNodes.addAll(tac.getIncludeNodes());
            return InclusionStatus.INCLUDED;
        } else {
            return InclusionStatus.CYCLIC;
        }
    }

//  countLines *********************************************************************

    private int countLines(String fileName) {
        int lines = 0;
        try {
            File countMe = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(countMe));
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return lines;
    }

// addSuperGlobal ******************************************************************

    private void addSuperGlobal(String varName) {
        // lookup variable in superglobals symbol table
        Variable var = this.superSymbolTable.getVariable(varName);

        // if it isn't there: add it
        if (var == null) {
            var = new Variable(varName, this.superSymbolTable);
            this.superSymbolTable.add(var);
        }
        var.setIsSuperGlobal(true);
    }
}
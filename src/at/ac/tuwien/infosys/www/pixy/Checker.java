package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.DummyAliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.*;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional.FunctionalAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.DummyLiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.ProgramConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class Checker {
    // enable this switch to make the TacConverter recognize hotspots
    // and other special nodes
    private boolean specialNodes = true;

    // required by call-string analyses
    private ConnectorComputation connectorComp = null;
    //private InterproceduralWorklistOrder order;
    private InterproceduralWorklist workList;

    // k-size for call-string analyses
    private int kSize = 1;

    // Analyses
    AliasAnalysis aliasAnalysis;
    LiteralAnalysis literalAnalysis;
    public GenericTaintAnalysis gta;

    InclusionDominatorAnalysis inclusionDominatorAnalysis;

//  ********************************************************************************
//  MAIN ***************************************************************************
//  ********************************************************************************

    private static void help(Options cliOptions) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("check [options] file", cliOptions);
    }

    public static void main(String[] args) {

        // **********************
        // COMMAND LINE PARSING
        // **********************

        Options cliOptions = new Options();
        cliOptions.addOption("a", "call-string", false, "call-string analysis (else: functional)");
        cliOptions.addOption("A", "alias", false, "use alias analysis");
        cliOptions.addOption("b", "brief", false, "be brief (for regression tests)");
        cliOptions.addOption("c", "cfg", false, "dump the function CFGs in dot syntax");
        cliOptions.addOption("d", "detailcfg", false, "dump the function control flow graphs and the CFGs of their paramters in dot syntax");
        cliOptions.addOption("f", "functions", false, "print function information");
        cliOptions.addOption("g", "registerGlobals", false, "DISABLE register_globals for analysis");
        cliOptions.addOption("h", "help", false, "print help");
        cliOptions.addOption("i", "getisuntaintedsql", false, "make the GET array untainted for SQL analysis");
        cliOptions.addOption("l", "libdetect", false, "detect libraries (i.e. scripts with empty main function)");
        cliOptions.addOption("L", "literal", false, "use literal analysis (usually not necessary)");
        cliOptions.addOption("m", "max", false, "print maximum number of temporaries");
        cliOptions.addOption("o", "outputdir", true, "output directory (for graphs etc.)");
        cliOptions.addOption("p", "parsetree", false, "print the parse tree in dot syntax");
        cliOptions.addOption("P", "prefixes", false, "print prefixes and suffixes");
        cliOptions.addOption("q", "query", false, "enable interactive queries");
        cliOptions.addOption("r", "notrim", false, "do NOT trim untained stuff (during sanitation analysis)");
        cliOptions.addOption("s", "sinks", true, "provide config files for custom sinks");
        cliOptions.addOption("t", "table", false, "print symbol tables");
        cliOptions.addOption("w", "web", false, "web interface mode");
        cliOptions.addOption("v", "verbose", false, "enable verbose output");
        cliOptions.addOption("V", "verbosegraphs", false, "disable verbose depgraphs");
        cliOptions.addOption("y", "analysistype", true,
            "type of taint analysis (" + MyOptions.getAnalysisNames() + ")");

        CommandLineParser cliParser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = cliParser.parse(cliOptions, args);
        } catch (MissingOptionException e) {
            help(cliOptions);
            Utils.bail("Please specify option: " + e.getMessage());
        } catch (ParseException e) {
            help(cliOptions);
            Utils.bail(e.getMessage());
        }

        if (cmd.hasOption("h")) {
            help(cliOptions);
            System.exit(0);
        }

        String[] restArgs = cmd.getArgs();
        if (restArgs.length != 1) {
            help(cliOptions);
            Utils.bail("Please specify exactly one target file.");
        }
        String fileName = restArgs[0];

        // **********************
        // CHECKING
        // **********************

        Checker checker = new Checker(fileName);

        // set boolean options according to command line
        MyOptions.optionA = cmd.hasOption("a");
        MyOptions.option_A = cmd.hasOption("A");
        MyOptions.optionB = cmd.hasOption("b");
        MyOptions.optionC = cmd.hasOption("c");
        MyOptions.optionD = cmd.hasOption("d");
        MyOptions.optionF = cmd.hasOption("f");
        MyOptions.optionG = !cmd.hasOption("g");
        MyOptions.optionI = cmd.hasOption("i");
        MyOptions.optionL = cmd.hasOption("l");
        MyOptions.option_L = cmd.hasOption("L");
        MyOptions.optionM = cmd.hasOption("m");
        MyOptions.optionP = cmd.hasOption("p");
        MyOptions.option_P = cmd.hasOption("P");
        MyOptions.optionQ = cmd.hasOption("q");
        MyOptions.optionR = cmd.hasOption("r");
        MyOptions.optionS = cmd.getOptionValue("s");
        MyOptions.optionT = cmd.hasOption("t");
        MyOptions.optionW = cmd.hasOption("w");
        MyOptions.optionV = cmd.hasOption("v");
        MyOptions.option_V = !cmd.hasOption("V");

        // inform MyOptions about the analyses that are to be performed
        if (!MyOptions.setAnalyses(cmd.getOptionValue("y"))) {
            Utils.bail("Invalid 'y' argument");
        }

        // set output directory
        if (cmd.hasOption("o")) {
            MyOptions.graphPath = cmd.getOptionValue("o");
            File graphPathFile = new File(MyOptions.graphPath);
            if (!graphPathFile.isDirectory()) {
                Utils.bail("Given output directory does not exist");
            }
        } else {
            // no output directory given: use default

            MyOptions.graphPath = MyOptions.pixy_home + "/graphs";

            // create / empty the graphs directory
            File graphPathFile = new File(MyOptions.graphPath);
            graphPathFile.mkdir();
            for (File file : graphPathFile.listFiles()) {
                file.delete();
            }
        }

        if (!MyOptions.optionW) {
            if (MyOptions.optionB) {
                System.out.println("File: " + Utils.basename(fileName));
            } else {
                System.out.println("File: " + fileName);
            }
        }

        long startTime = System.currentTimeMillis();

        // convert the whole program (with file inclusions)
        ProgramConverter pcv = checker.initialize();
        TacConverter tac = pcv.getTac();

        // params: tac, functional?, desired analyses
        checker.analyzeTaint(tac, !MyOptions.optionA);

        if (!MyOptions.optionB) {
            long analysisEndTime = System.currentTimeMillis();
            long analysisDiffTime = (analysisEndTime - startTime) / 1000;
            System.out.println();
            System.out.println("Time: " + analysisDiffTime + " seconds");
        }

        // we don't need these any more:
        checker.literalAnalysis = null;
        checker.aliasAnalysis = null;

        // detect vulnerabilities
        System.out.println("\n*** detecting vulnerabilities ***\n");
        checker.gta.detectVulns();

        if (!MyOptions.optionB) {
            long endTime = System.currentTimeMillis();
            long diffTime = (endTime - startTime) / 1000;
            System.out.println("Total Time: " + diffTime + " seconds");
            System.out.println();
            System.out.println();
        }
    }

//  ********************************************************************************
//  CONSTRUCTOR ********************************************************************
//  ********************************************************************************

    // after calling this constructor and before initializing / analyzing,
    // you can set options by modifying the appropriate member variables
    public Checker(String fileName) {

        // get entry file
        try {
            MyOptions.entryFile = (new File(fileName)).getCanonicalFile();
        } catch (IOException e) {
            Utils.bail("File not found: " + fileName);
        }
    }

//  ********************************************************************************
//  SET ****************************************************************************
//  ********************************************************************************

    // adjust the kSize for call-string analyses
    public void setKSize(int kSize) {
        this.kSize = kSize;
    }

//  ********************************************************************************
//  OTHERS *************************************************************************
//  ********************************************************************************

    private void readConfig() {

        // read config file into props
        String configPath = MyOptions.pixy_home + "/" + MyOptions.configDir + "/config.txt";
        File configFile = new File(configPath);
        Properties props = new Properties();
        try {
            configPath = configFile.getCanonicalPath();
            FileInputStream in = new FileInputStream(configPath);
            props.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            Utils.bail("Can't find configuration file: " + configPath);
        } catch (IOException e) {
            Utils.bail("I/O exception while reading configuration file:" + configPath,
                e.getMessage());
        }

        // read PHP include path from the config file
        MyOptions.includePaths = new LinkedList<>();
        MyOptions.includePaths.add(new File("."));
        String includePath = props.getProperty(InternalStrings.includePath);
        if (includePath != null) {
            StringTokenizer tokenizer = new StringTokenizer(includePath, ":");
            while (tokenizer.hasMoreTokens()) {
                String pathElement = tokenizer.nextToken();
                File pathElementFile = new File(pathElement);
                if (pathElementFile.isDirectory()) {
                    MyOptions.includePaths.add(pathElementFile);
                } else {
                    System.out.println("Warning: Invalid PHP path directory in config file: " + pathElement);
                }
            }
        }

        // location of php binary
        String phpBin = props.getProperty(InternalStrings.phpBin);
        if (phpBin == null) {
            MyOptions.phpBin = null;
        } else {
            if (!(new File(phpBin)).canExecute()) {
                System.out.println("Warning: Invalid path to PHP binary in config file: " + phpBin);
                MyOptions.phpBin = null;
            } else {
                MyOptions.phpBin = phpBin;
            }
        }

        // location of FSA-Utils
        String fsaHome = props.getProperty(InternalStrings.fsaHome);
        if (fsaHome != null) {
            if (!(new File(fsaHome)).exists()) {
                fsaHome = null;
            }
        }
        MyOptions.fsa_home = fsaHome;

        // read harmless server variables
        String hsvPath = MyOptions.pixy_home + "/" + MyOptions.configDir + "/harmless_server_vars.txt";
        File hsvFile = new File(hsvPath);
        Properties hsvProps = new Properties();
        try {
            hsvPath = hsvFile.getCanonicalPath();
            FileInputStream in = new FileInputStream(hsvPath);
            hsvProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            Utils.bail("Can't find configuration file: " + hsvPath);
        } catch (IOException e) {
            Utils.bail("I/O exception while reading configuration file:" + hsvPath,
                e.getMessage());
        }
        Enumeration<Object> hsvKeys = hsvProps.keys();
        while (hsvKeys.hasMoreElements()) {
            String hsvElement = (String) hsvKeys.nextElement();
            hsvElement = hsvElement.trim();
            MyOptions.addHarmlessServerIndex(hsvElement);
        }
    }

//  initialize *********************************************************************

    // taintString: "-y" option, type of taint analysis
    ProgramConverter initialize() {

        // *****************
        // PREPARATIONS
        // *****************

        // read config file
        readConfig();

        // *****************
        // PARSE & CONVERT
        // *****************

        // initialize builtin sinks
        MyOptions.initSinks();

        // read user-defined custom sinks
        MyOptions.readCustomSinkFiles();

        // read builtin function models
        MyOptions.readModelFiles();

        // convert the program
        ProgramConverter pcv = new ProgramConverter(
            this.specialNodes, MyOptions.option_A/*, props*/);

        // print parse tree in dot syntax
        if (MyOptions.optionP) {
            ParseTree parseTree = pcv.parse(MyOptions.entryFile.getPath());
            Dumper.dumpDot(parseTree, MyOptions.graphPath, "parseTree");
            System.exit(0);
        }

        pcv.convert();
        TacConverter tac = pcv.getTac();

        if (MyOptions.optionL) {
            if (tac.hasEmptyMain()) {
                System.out.println(MyOptions.entryFile.getPath() + ": library!");
            } else {
                System.out.println(MyOptions.entryFile.getPath() + ": entry point!");
            }
            System.exit(0);
        }

        // print maximum number of temporaries
        if (MyOptions.optionM) {
            System.out.println("Maximum number of temporaries: " + tac.getMaxTempId());
        }

        // print symbol tables
        if (MyOptions.optionT) {
            Dumper.dump(tac.getSuperSymbolTable(), "Superglobals");
            for (TacFunction function : tac.getUserFunctions().values()) {
                Dumper.dump(function.getSymbolTable(), function.getName());
            }
            Dumper.dump(tac.getConstantsTable());
        }

        // print function information
        if (MyOptions.optionF) {
            for (TacFunction function : tac.getUserFunctions().values()) {
                Dumper.dump(function);
            }
        }

        // print control flow graphs
        if (MyOptions.optionC || MyOptions.optionD) {
            for (TacFunction function : tac.getUserFunctions().values()) {
                Dumper.dumpDot(function, MyOptions.graphPath, MyOptions.optionD);
            }
            System.exit(0);
        }

        return pcv;
    }

//  analyzeAliases *****************************************************************

    // "cleanup" should only be disabled for JUnit tests
    AliasAnalysis analyzeAliases(TacConverter tac, boolean cleanup) {

        // ***********************
        // PERFORM ALIAS ANALYSIS
        // ***********************

        if (!MyOptions.option_A) {
            this.aliasAnalysis = new DummyAliasAnalysis();
            return this.aliasAnalysis;
        }

        System.out.println("\n*** initializing alias analysis ***\n");
        this.aliasAnalysis = new AliasAnalysis(tac, new FunctionalAnalysis());
        System.out.println("\n*** performing alias analysis ***\n");
        this.aliasAnalysis.analyze();
        if (cleanup) {
            System.out.println("\n*** cleaning up ***\n");
            this.aliasAnalysis.clean();
        }
        System.out.println("\nFinished.");

        return this.aliasAnalysis;
    }

//  analyzeLiterals ****************************************************************

    LiteralAnalysis analyzeLiterals(TacConverter tac) {

        // *************************
        // PERFORM LITERAL ANALYSIS
        // *************************

        this.analyzeAliases(tac, true);

        if (!MyOptions.option_L) {
            this.literalAnalysis = new DummyLiteralAnalysis();
            return this.literalAnalysis;
        }

        // this is a call-string analysis and therefore requires previously
        // computed connectors; if this computation hasn't been done yet,
        // do it now
        if (this.connectorComp == null) {
            this.connectorComp = new ConnectorComputation(
                tac.getAllFunctions(), tac.getMainFunction(), this.kSize);
            connectorComp.compute();
            this.workList = new InterproceduralWorklistBetter(new InterproceduralWorklistOrder(tac, this.connectorComp));
        }

        System.out.println("\n*** initializing literal analysis ***\n");
        this.literalAnalysis =
            new LiteralAnalysis(tac, this.aliasAnalysis,
                new CSAnalysis(this.connectorComp), this.workList);
        System.out.println("\n*** performing literal analysis ***\n");
        this.literalAnalysis.analyze();
        System.out.println("\n*** cleaning up ***\n");
        this.literalAnalysis.clean();
        System.out.println("\nFinished.");

        return this.literalAnalysis;
    }

//  ********************************************************************************

    // - "functional": functional or CS analysis?
    // - "useLiteralAnalysis": use real literal analysis? or rather a dummy?
    //  a dummy literal analysis is MUCH faster (in fact, it doesn't analyze anything),
    //  but can lead to less precise results in if-evaluation and the resolution of
    //  defined constants; can solve easy cases, however (see DummyLiteralAnalysis.java)
    public void analyzeTaint(TacConverter tac, boolean functional) {

        // perform literal analysis if necessary; also takes care of alias analysis
        this.analyzeLiterals(tac);

        // ***********************
        // PERFORM TAINT ANALYSIS
        // ***********************

        AnalysisType enclosingAnalysis;
        CallGraph callGraph = null;
        GlobalsModificationAnalysis globalsModificationAnalysis = null;
        if (functional) {
            System.out.println("functional analysis!");
            enclosingAnalysis = new FunctionalAnalysis();
            this.workList = new InterproceduralWorklistPoor();
        } else {
            if (this.connectorComp == null) {
                this.connectorComp = new ConnectorComputation(
                    tac.getAllFunctions(), tac.getMainFunction(), this.kSize);
                connectorComp.compute();
                this.workList = new InterproceduralWorklistBetter(new InterproceduralWorklistOrder(tac, this.connectorComp));
                connectorComp.stats(false);
            }
            if (MyOptions.optionV) {
                System.out.println("call-string analysis!");
            }
            enclosingAnalysis = new CSAnalysis(this.connectorComp);

            // write called-by relations to file; can be quite useful
            Utils.writeToFile(this.connectorComp.dump(),
                MyOptions.graphPath + "/" + "/calledby_" + MyOptions.entryFile.getName() + ".txt");

            callGraph = this.connectorComp.getCallGraph();
            if (this.aliasAnalysis instanceof DummyAliasAnalysis) {
                globalsModificationAnalysis = new GlobalsModificationAnalysis(tac.getAllFunctions(), callGraph);
            }
        }

        this.gta = GenericTaintAnalysis.createAnalysis(tac, enclosingAnalysis,
            this, this.workList, globalsModificationAnalysis);
        if (this.gta == null) {
            Utils.bail("Please specify a valid type of taint analysis.");
        }
        System.out.println("\n*** performing taint analysis ***\n");
        gta.analyze();

        System.out.println("\nFinished.");
    }

//  analyzeIncDom ******************************************************************

    InclusionDominatorAnalysis analyzeIncDom(TacFunction function) {

        // ***********************************
        // PERFORM INCLUDE DOMINATOR ANALYSIS
        // ***********************************

        System.out.println("\n*** initializing inclusiondominator analysis ***\n");
        this.inclusionDominatorAnalysis = new InclusionDominatorAnalysis(function);
        System.out.println("\n*** performing inclusiondominator analysis ***\n");
        this.inclusionDominatorAnalysis.analyze();

        return this.inclusionDominatorAnalysis;
    }

//  report *************************************************************************

    public static void report() {

        System.gc();
        System.gc();
        System.gc();

        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long freeMem = rt.freeMemory();
        long usedMem = totalMem - freeMem;

        System.out.println("Memory used: " + usedMem);
    }
}
package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.DummyAliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.*;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional.FunctionalAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.DummyLiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
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

        Options commandLineOptions = createCommandLineOptions();

        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(commandLineOptions, args);
        } catch (MissingOptionException e) {
            help(commandLineOptions);
            Utils.bail("Please specify option: " + e.getMessage());
        } catch (ParseException e) {
            help(commandLineOptions);
            Utils.bail(e.getMessage());
        }

        if (commandLine.hasOption("h")) {
            help(commandLineOptions);
            System.exit(0);
        }

        String[] trailingArguments = commandLine.getArgs();
        if (trailingArguments.length != 1) {
            help(commandLineOptions);
            Utils.bail("Please specify exactly one target file.");
        }
        String fileName = trailingArguments[0];

        // **********************
        // CHECKING
        // **********************

        Checker checker = new Checker(fileName);

        // set boolean options according to command line
        MyOptions.optionA = commandLine.hasOption("a");
        MyOptions.option_A = commandLine.hasOption("A");
        MyOptions.optionB = commandLine.hasOption("b");
        MyOptions.optionC = commandLine.hasOption("c");
        MyOptions.optionD = commandLine.hasOption("d");
        MyOptions.optionF = commandLine.hasOption("f");
        MyOptions.optionG = !commandLine.hasOption("g");
        MyOptions.optionI = commandLine.hasOption("i");
        MyOptions.optionL = commandLine.hasOption("l");
        MyOptions.option_L = commandLine.hasOption("L");
        MyOptions.optionM = commandLine.hasOption("m");
        MyOptions.optionP = commandLine.hasOption("p");
        MyOptions.option_P = commandLine.hasOption("P");
        MyOptions.optionQ = commandLine.hasOption("q");
        MyOptions.optionR = commandLine.hasOption("r");
        MyOptions.optionS = commandLine.getOptionValue("s");
        MyOptions.optionT = commandLine.hasOption("t");
        MyOptions.optionW = commandLine.hasOption("w");
        MyOptions.optionV = commandLine.hasOption("v");
        MyOptions.option_V = !commandLine.hasOption("V");

        // inform MyOptions about the analyses that are to be performed
        if (!MyOptions.setAnalyses(commandLine.getOptionValue("y"))) {
            Utils.bail("Invalid 'y' argument");
        }

        // set output directory
        if (commandLine.hasOption("o")) {
            MyOptions.graphPath = commandLine.getOptionValue("o");
            File graphPathFile = new File(MyOptions.graphPath);
            if (!graphPathFile.isDirectory()) {
                Utils.bail("Given output directory does not exist");
            }
        } else {
            // no output directory given: use default
            MyOptions.graphPath = MyOptions.pixyHome + "/graphs";

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

    private static Options createCommandLineOptions() {
        Options commandLineOptions = new Options();

        commandLineOptions.addOption("a", "call-string", false, "call-string analysis (else: functional)");
        commandLineOptions.addOption("A", "alias", false, "use alias analysis");
        commandLineOptions.addOption("b", "brief", false, "be brief (for regression tests)");
        commandLineOptions.addOption("c", "cfg", false, "dump the function CFGs in dot syntax");
        commandLineOptions.addOption("d", "detailcfg", false, "dump the function control flow graphs and the CFGs of their paramters in dot syntax");
        commandLineOptions.addOption("f", "functions", false, "print function information");
        commandLineOptions.addOption("g", "registerGlobals", false, "DISABLE register_globals for analysis");
        commandLineOptions.addOption("h", "help", false, "print help");
        commandLineOptions.addOption("i", "getisuntaintedsql", false, "make the GET array untainted for SQL analysis");
        commandLineOptions.addOption("l", "libdetect", false, "detect libraries (i.e. scripts with empty main function)");
        commandLineOptions.addOption("L", "literal", false, "use literal analysis (usually not necessary)");
        commandLineOptions.addOption("m", "max", false, "print maximum number of temporaries");
        commandLineOptions.addOption("o", "outputdir", true, "output directory (for graphs etc.)");
        commandLineOptions.addOption("p", "parsetree", false, "print the parse tree in dot syntax");
        commandLineOptions.addOption("P", "prefixes", false, "print prefixes and suffixes");
        commandLineOptions.addOption("q", "query", false, "enable interactive queries");
        commandLineOptions.addOption("r", "notrim", false, "do NOT trim untained stuff (during sanitation analysis)");
        commandLineOptions.addOption("s", "sinks", true, "provide config files for custom sinks");
        commandLineOptions.addOption("t", "table", false, "print symbol tables");
        commandLineOptions.addOption("w", "web", false, "web interface mode");
        commandLineOptions.addOption("v", "verbose", false, "enable verbose output");
        commandLineOptions.addOption("V", "verbosegraphs", false, "disable verbose depgraphs");
        commandLineOptions.addOption("y", "analysistype", true, "type of taint analysis (" + MyOptions.getAnalysisNames() + ")");

        return commandLineOptions;
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
//  OTHERS *************************************************************************
//  ********************************************************************************

    private void readConfiguration() {
        Properties properties = readConfigurationFileIntoProperties();

        readPhpIncludePathFromConfigurationFile(properties);
        findPhpBinary(properties);
        findFsaUtilities(properties);
        readHarmlessServerVariables();
    }

    private Properties readConfigurationFileIntoProperties() {
        String configPath = MyOptions.pixyHome + "/" + MyOptions.configurationDirectory + "/config.txt";
        File configFile = new File(configPath);
        Properties properties = new Properties();
        try {
            configPath = configFile.getCanonicalPath();
            FileInputStream in = new FileInputStream(configPath);
            properties.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            Utils.bail("Can't find configuration file: " + configPath);
        } catch (IOException e) {
            Utils.bail("I/O exception while reading configuration file:" + configPath, e.getMessage());
        }

        return properties;
    }

    private void readPhpIncludePathFromConfigurationFile(Properties properties) {
        MyOptions.includePaths = new LinkedList<>();
        MyOptions.includePaths.add(new File("."));
        String includePath = properties.getProperty(InternalStrings.INCLUDE_PATH);
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
    }

    private void findPhpBinary(Properties properties) {
        String phpBin = properties.getProperty(InternalStrings.PHP_BIN);
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
    }

    private void findFsaUtilities(Properties properties) {
        String fsaUtilitiesHome = properties.getProperty(InternalStrings.FSA_HOME);
        if (fsaUtilitiesHome != null) {
            if (!(new File(fsaUtilitiesHome)).exists()) {
                fsaUtilitiesHome = null;
            }
        }
        MyOptions.fsaHome = fsaUtilitiesHome;
    }

    private void readHarmlessServerVariables() {
        String harmlessServerVariablesPath = MyOptions.pixyHome + "/" + MyOptions.configurationDirectory
            + "/harmless_server_vars.txt";
        File harmlessServerVariablesFile = new File(harmlessServerVariablesPath);
        Properties harmlessServerVariablesProperties = new Properties();
        try {
            harmlessServerVariablesPath = harmlessServerVariablesFile.getCanonicalPath();
            FileInputStream in = new FileInputStream(harmlessServerVariablesPath);
            harmlessServerVariablesProperties.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            Utils.bail("Can't find configuration file: " + harmlessServerVariablesPath);
        } catch (IOException e) {
            Utils.bail("I/O exception while reading configuration file:" + harmlessServerVariablesPath,
                e.getMessage());
        }

        Enumeration<Object> harmlessServerVariablesKeys = harmlessServerVariablesProperties.keys();
        while (harmlessServerVariablesKeys.hasMoreElements()) {
            String hsvElement = (String) harmlessServerVariablesKeys.nextElement();
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
        readConfiguration();

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
                new CallStringAnalysis(this.connectorComp), this.workList);
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

        AbstractAnalysisType enclosingAnalysis;
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
            enclosingAnalysis = new CallStringAnalysis(this.connectorComp);

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
}
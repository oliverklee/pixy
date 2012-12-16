package at.ac.tuwien.infosys.www.pixy;

import org.apache.commons.cli.*;

import at.ac.tuwien.infosys.www.phpparser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.analysis.incdom.IncDomAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkListBetter;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkListOrder;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkListPoor;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring.CSAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.functional.FunctionalAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.*;
import at.ac.tuwien.infosys.www.pixy.analysis.mod.ModAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.*;

import java.io.*;
import java.util.*;


public final class Checker {

    // enable this switch to make the TacConverter recognize hotspots
    // and other special nodes
    private boolean specialNodes = true;
    
    // required by call-string analyses
    private ConnectorComputation connectorComp = null;
    //private InterWorkListOrder order;
    private InterWorkList workList;
    
    // k-size for call-string analyses
    private int kSize = 1;
    
    // Analyses
    AliasAnalysis aliasAnalysis;
    LiteralAnalysis literalAnalysis;
    public GenericTaintAnalysis gta;
    
    IncDomAnalysis incDomAnalysis;
    
//  ********************************************************************************
//  MAIN ***************************************************************************
//  ********************************************************************************

    private static void help(Options cliOptions) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp( "check [options] file", cliOptions);
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
        cliOptions.addOption("r", "notrim", false, "do NOT trim untained stuff (during sanit analysis)");
        cliOptions.addOption("s", "sinks", true, "provide config files for custom sinks");
        cliOptions.addOption("t", "table", false, "print symbol tables");
        cliOptions.addOption("w", "web", false, "web interface mode");
        cliOptions.addOption("v", "verbose", false, "enable verbose output");
        cliOptions.addOption("V", "verbosegraphs", false, "disable verbose depgraphs");
        cliOptions.addOption("y", "analysistype", true, 
                "type of taint analysis (" + MyOptions.getAnalysisNames() + ")");
        /*
        Option o = new Option("y", "analysistype", true, 
                "type of taint analysis (" + InternalStrings.getAnalyses() + ")"); 
        o.setRequired(true);
        cliOptions.addOption(o);
        */
        
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
        if (MyOptions.setAnalyses(cmd.getOptionValue("y")) == false) {
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
        
        
        //tac.stats();
        
        // System.out.println("Cfg nodes: " + tac.getSize());
        //Checker.report();
        
        // params: tac, functional?, desired analyses
        checker.analyzeTaint(tac, !MyOptions.optionA);

        if (!MyOptions.optionB) {
            long analysisEndTime = System.currentTimeMillis();
            long analysisDiffTime = (analysisEndTime - startTime) / 1000;
            System.out.println();
            System.out.println("Time: " + analysisDiffTime + " seconds");
        }
        
        //checker.taintAnalysis.stats();
        // System.out.println("Taint Analysis Size: " + checker.taintAnalysis.size());

        // we don't need these any more:
        checker.literalAnalysis = null;
        checker.aliasAnalysis = null;

        // we don't need these either;
        // EFF: perhaps using weak references would be better;
        // don't do the following, or you will get a NullPointerException in ExTaint.create()
        /*
        ExTaintSet.repos = null;
        ExTaint.repos = null;
        */
        
        //checker.xssTaintAnalysis.info();
        //provokeOutOfMemory();
        
        // detect vulnerabilities
        System.out.println("\n*** detecting vulnerabilities ***\n");
        checker.gta.detectVulns();
        
        //System.out.println("done!");
        if (!MyOptions.optionB) {
            long endTime = System.currentTimeMillis();
            long diffTime = (endTime - startTime) / 1000;
            System.out.println("Total Time: " + diffTime + " seconds");
            System.out.println();
            System.out.println();
        }
        
        // QUERY
        /*
        if (checker.optionQ) {
            QueryClient queryClient = new QueryClient(checker.taintAnalysis, pcv.getAllFiles(), tac);
            queryClient.start();
        }
        */

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
        MyOptions.includePaths = new LinkedList<File>();
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
            //Utils.bail("Please set " + InternalStrings.phpBin + " in config");
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
        
        /*
        MyOptions.graphPath = MyOptions.pixy_home + "/graphs";
        
        // create / empty the graphs directory
        System.out.println("Cleaning up graph directory...");
        File graphPathFile = new File(MyOptions.graphPath);
        graphPathFile.mkdir();
        for (File file : graphPathFile.listFiles()) {
            file.delete();
        }
        */
        
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
            for (Iterator iter = tac.getUserFunctions().values().iterator(); iter.hasNext(); ) {
                TacFunction function = (TacFunction) iter.next();
                Dumper.dump(function.getSymbolTable(), function.getName());
            }
            Dumper.dump(tac.getConstantsTable());
        }

        // print function information
        if (MyOptions.optionF) {
            for (Iterator iter = tac.getUserFunctions().values().iterator(); iter.hasNext(); ) {
                TacFunction function = (TacFunction) iter.next();
                Dumper.dump(function);
            }
        }

        // print control flow graphs
        if (MyOptions.optionC || MyOptions.optionD) {
            for (Iterator iter = tac.getUserFunctions().values().iterator(); iter.hasNext(); ) {
                TacFunction function = (TacFunction) iter.next();
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

        ////Checker.report();
        System.out.println("\n*** initializing alias analysis ***\n");
        this.aliasAnalysis = new AliasAnalysis(tac, new FunctionalAnalysis());
        //Checker.report();
        System.out.println("\n*** performing alias analysis ***\n");
        this.aliasAnalysis.analyze();
        //Checker.report();
        if (cleanup) {
            System.out.println("\n*** cleaning up ***\n");
            this.aliasAnalysis.clean();
        }
        //Checker.report();
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
            this.workList = new InterWorkListBetter(new InterWorkListOrder(tac, this.connectorComp));
            // Dumper.dumpFunction2ECS(connectorComp.getFunction2ECS());
            // Dumper.dumpCall2ConFunc(connectorComp.getCall2ConnectorFunction());
        }

        //Checker.report();
        System.out.println("\n*** initializing literal analysis ***\n");
        this.literalAnalysis = 
            new LiteralAnalysis(tac, this.aliasAnalysis, 
                    new CSAnalysis(this.connectorComp), this.workList);
        //Checker.report();
        System.out.println("\n*** performing literal analysis ***\n");
        this.literalAnalysis.analyze();
        //Checker.report();
        System.out.println("\n*** cleaning up ***\n");
        this.literalAnalysis.clean();
        //Checker.report();
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
        ModAnalysis modAnalysis = null;
        if (functional) {
            System.out.println("functional analysis!");
            enclosingAnalysis = new FunctionalAnalysis();
            this.workList = new InterWorkListPoor();
        } else {
            if (this.connectorComp == null) {
                this.connectorComp = new ConnectorComputation(
                        tac.getAllFunctions(), tac.getMainFunction(), this.kSize);
                connectorComp.compute();
                this.workList = new InterWorkListBetter(new InterWorkListOrder(tac, this.connectorComp));
                connectorComp.stats(false);
            }
            if (MyOptions.optionV) {
                System.out.println("call-string analysis!");
            }
            //System.out.println("STATS:");
            //this.connectorComp.stats();
            enclosingAnalysis = new CSAnalysis(this.connectorComp);
            
            // write called-by relations to file; can be quite useful
            Utils.writeToFile(this.connectorComp.dump(), 
                    MyOptions.graphPath + "/" + "/calledby_"  + MyOptions.entryFile.getName() + ".txt");
            
            callGraph = this.connectorComp.getCallGraph();
            if (this.aliasAnalysis instanceof DummyAliasAnalysis) {
                modAnalysis = new ModAnalysis(tac.getAllFunctions(), callGraph);
            }
            
        }
        
        
        //Checker.report();
        this.gta = GenericTaintAnalysis.createAnalysis(tac, enclosingAnalysis, 
                this, this.workList, modAnalysis);
        if (this.gta == null) {
            Utils.bail("Please specify a valid type of taint analysis.");
        }
        //Checker.report();
        System.out.println("\n*** performing taint analysis ***\n");
        gta.analyze();
        
        /*
        Checker.report();
        Checker.report();
        Checker.report();
        */

        // DON'T do this here:
        // TaintAnalysis.detectVulns() requires intact context information
        /*
        System.out.println("\n*** cleaning up ***\n");
        this.taintAnalysis.clean();
        */

        System.out.println("\nFinished.");

        /*
        Checker.report();
        Checker.report();
        Checker.report();
        */

    }
   
//  analyzeIncDom ******************************************************************

    IncDomAnalysis analyzeIncDom(TacFunction function) {
        
        // ***********************************
        // PERFORM INCLUDE DOMINATOR ANALYSIS
        // ***********************************
        
        System.out.println("\n*** initializing incdom analysis ***\n");
        this.incDomAnalysis = new IncDomAnalysis(function);
        System.out.println("\n*** performing incdom analysis ***\n");
        this.incDomAnalysis.analyze();
        
        return this.incDomAnalysis;
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
    
//  provokeOutOfMemory *************************************************************
    
    /*
    private static void provokeOutOfMemory() {
        int[] big = new int[1000000000];
    }
    */

}






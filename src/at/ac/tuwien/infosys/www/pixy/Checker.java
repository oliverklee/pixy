package at.ac.tuwien.infosys.www.pixy;

import org.apache.commons.cli.*;

import at.ac.tuwien.infosys.www.pixy.GUI.PixyGUI;
import at.ac.tuwien.infosys.www.pixy.VisualizePT.GraphViz;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.AliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.DummyAliasAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inclusiondominator.InclusionDominatorAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.CallGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.ConnectorComputation;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklistBetter;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklistOrder;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklistPoor;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.functional.FunctionalAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.DummyLiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.ProgramConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseTree;

import java.io.*;
import java.util.*;

public final class Checker {

	private boolean specialNodes = true;

	private ConnectorComputation connectorComp = null;
	private InterproceduralWorklist workList;

	private int kSize = 1;

	AliasAnalysis aliasAnalysis;
	LiteralAnalysis literalAnalysis;
	public GenericTaintAnalysis gta;

	public static PixyGUI frame;
	InclusionDominatorAnalysis incDomAnalysis;

	public static void help(Options cliOptions) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("check [options] file", cliOptions);
	}

	public static void main(String[] args) {

		Options cliOptions = new Options();
		cliOptions.addOption("a", "call-string", false, "call-string analysis (else: functional)");
		cliOptions.addOption("A", "alias", false, "use alias analysis");
		cliOptions.addOption("A", "alias", true, "use alias analysis");
		cliOptions.addOption("b", "brief", false, "be brief (for regression tests)");
		cliOptions.addOption("c", "cfg", false, "dump the function CFGs in dot syntax");
		cliOptions.addOption("d", "detailcfg", false,
				"dump the function control flow graphs and the CFGs of their paramters in dot syntax");
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
		cliOptions.addOption("OH", "OutputHTML", true, "output vulnerabilities to HTML");

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

		Checker checker = new Checker(fileName);

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

		MyOptions.graphPath = MyOptions.pixy_home + "/graphs";

		File graphPathFile = new File(MyOptions.graphPath);
		graphPathFile.mkdir();
		for (File file : graphPathFile.listFiles()) {
			file.delete();
		}

		if (!MyOptions.optionW) {
			if (MyOptions.optionB) {
				System.out.println("File: " + Utils.basename(fileName));
			} else {
				System.out.println("File: " + fileName);
			}
		}
		MyOptions.option_OH = true;
		MyOptions.outputHtmlPath = MyOptions.pixy_home + "/Reports";
		File outputHtmlPathFile = new File(MyOptions.outputHtmlPath);
		outputHtmlPathFile.mkdir();
		for (File file : outputHtmlPathFile.listFiles()) {
			file.delete();
		}
		long startTime = System.currentTimeMillis();

		ProgramConverter pcv = checker.initialize();
		TacConverter tac = pcv.getTac();

		checker.analyzeTaint(tac, !MyOptions.optionA);

		if (!MyOptions.optionB) {
			long analysisEndTime = System.currentTimeMillis();
			long analysisDiffTime = (analysisEndTime - startTime) / 1000;
			System.out.println();
			System.out.println("Time: " + analysisDiffTime + " seconds");
		}

		checker.literalAnalysis = null;
		checker.aliasAnalysis = null;

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

	public static void Scan() {
		System.err.println("Start Scanning");
		String fileName = frame.FileName;
		if (frame.FileName == null) {
			Utils.bail("Please specify exactly one target file.");
		}
		Checker checker = new Checker(fileName);
		MyOptions.option_A = true;
		MyOptions.optionG = true;
		String s = "";

		if (frame.scanXSS) {
			s += "xss:";
		}
		if (frame.scanSQLI) {
			s += "sql:";
		}
		if (frame.scanXPathI) {
			s += "xpath:";

		}
		if (frame.scanCmdExec) {
			s += "cmdexec:";

		}
		if (frame.scanCdEval) {
			s += "codeeval:";

		}
		if (s.length() > 1) {
			s = s.substring(0, s.length() - 1);
		}
		if (MyOptions.setAnalyses(s) == false) {
			Utils.bail("Invalid 'y' argument");
		}

		MyOptions.option_VPS = frame.VisualParseTree;
		MyOptions.option_VXSS = frame.visualXSSDGraphs;
		MyOptions.option_VSQK = frame.visualSQLIDGraphs;
		MyOptions.option_VXP = frame.visualXPathIDGraphs;
		MyOptions.option_VCExec = frame.visualCmdExecDGraphs;
		MyOptions.option_VCEval = frame.visualCdEvalDGraphs;

		MyOptions.graphPath = MyOptions.pixy_home + "/graphs";

		File graphPathFile = new File(MyOptions.graphPath);
		graphPathFile.mkdir();
		for (File file : graphPathFile.listFiles()) {
			file.delete();
		}

		if (!MyOptions.optionW) {
			if (MyOptions.optionB) {
				System.out.println("File: " + Utils.basename(fileName));
			} else {
				System.out.println("File: " + fileName);
			}
		}
		MyOptions.option_OH = true;
		MyOptions.outputHtmlPath = MyOptions.pixy_home + "/Reports";
		File outputHtmlPathFile = new File(MyOptions.outputHtmlPath);
		outputHtmlPathFile.mkdir();
		for (File file : outputHtmlPathFile.listFiles()) {
			file.delete();
		}
		long startTime = System.currentTimeMillis();

		ProgramConverter pcv = checker.initialize();
		TacConverter tac = pcv.getTac();

		checker.analyzeTaint(tac, !MyOptions.optionA);

		if (!MyOptions.optionB) {
			long analysisEndTime = System.currentTimeMillis();
			long analysisDiffTime = (analysisEndTime - startTime) / 1000;
			System.out.println();
			System.out.println("Time: " + analysisDiffTime + " seconds");
		}

		checker.literalAnalysis = null;
		checker.aliasAnalysis = null;

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

	public Checker(String fileName) {

		try {
			MyOptions.entryFile = (new File(fileName)).getCanonicalFile();
		} catch (IOException e) {
			Utils.bail("File not found: " + fileName);
		}

	}

	public void setKSize(int kSize) {
		this.kSize = kSize;
	}

	private void readConfig() {

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
			Utils.bail("I/O exception while reading configuration file:" + configPath, e.getMessage());
		}
		props.setProperty(InternalStrings.includePath, "C:\\xampp\\php\\php.exe");
		props.setProperty(InternalStrings.phpBin, "C:\\xampp\\php\\php.exe");

		MyOptions.includePaths = new LinkedList<File>();
		MyOptions.includePaths.add(new File("."));
		String includePath = props.getProperty(InternalStrings.includePath);
		if (includePath != null) {
			StringTokenizer tokenizer = new StringTokenizer(includePath, ":");
			while (tokenizer.hasMoreTokens()) {
				String pathElement = tokenizer.nextToken();
				File pathElementFile = new File("C:\\xampp\\php");
				if (pathElementFile.isDirectory()) {
					MyOptions.includePaths.add(pathElementFile);
				} else {
					System.out.println("Warning: Invalid PHP path directory in config file: " + pathElement);
				}
			}
		}

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

		String fsaHome = props.getProperty(InternalStrings.fsaHome);
		if (fsaHome != null) {
			if (!(new File(fsaHome)).exists()) {
				fsaHome = null;
			}
		}
		MyOptions.fsa_home = fsaHome;

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
			Utils.bail("I/O exception while reading configuration file:" + hsvPath, e.getMessage());
		}
		Enumeration<Object> hsvKeys = hsvProps.keys();
		while (hsvKeys.hasMoreElements()) {
			String hsvElement = (String) hsvKeys.nextElement();
			hsvElement = hsvElement.trim();
			MyOptions.addHarmlessServerIndex(hsvElement);
		}

	}

	ProgramConverter initialize() {

		readConfig();

		MyOptions.initSinks();

		MyOptions.readCustomSinkFiles();

		MyOptions.readModelFiles();

		ProgramConverter pcv = new ProgramConverter(this.specialNodes, MyOptions.option_A/* , props */);

		System.err.println("Dot and Visualize Parse tree: " + MyOptions.option_VPS);
		if (MyOptions.option_VPS) {
			ParseTree parseTree = pcv.parse(MyOptions.entryFile.getPath());

			Dumper.dumpDot(parseTree, MyOptions.graphPath, "parseTree.dot");

			try {
				String input = MyOptions.graphPath + "/parseTree.dot";
				GraphViz gv = new GraphViz();
				gv.readSource(input);
				String type = "gif";
				File out = new File(MyOptions.graphPath + "/ParseTree." + type);
				gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
			} catch (Exception e) {
				System.err.println("Error in Visualization!!!");
			}
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

		if (MyOptions.optionM) {
			System.out.println("Maximum number of temporaries: " + tac.getMaxTempId());
		}

		if (MyOptions.optionT) {
			Dumper.dump(tac.getSuperSymbolTable(), "Superglobals");
			for (Iterator<?> iter = tac.getUserFunctions().values().iterator(); iter.hasNext();) {
				TacFunction function = (TacFunction) iter.next();
				Dumper.dump(function.getSymbolTable(), function.getName());
			}
			Dumper.dump(tac.getConstantsTable());
		}

		if (MyOptions.optionF) {
			for (Iterator<?> iter = tac.getUserFunctions().values().iterator(); iter.hasNext();) {
				TacFunction function = (TacFunction) iter.next();
				Dumper.dump(function);
			}
		}

		if (MyOptions.optionC || MyOptions.optionD) {
			for (Iterator<?> iter = tac.getUserFunctions().values().iterator(); iter.hasNext();) {
				TacFunction function = (TacFunction) iter.next();
				Dumper.dumpDot(function, MyOptions.graphPath, MyOptions.optionD);
			}
			System.exit(0);
		}

		return pcv;
	}

	AliasAnalysis analyzeAliases(TacConverter tac, boolean cleanup) {
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

	LiteralAnalysis analyzeLiterals(TacConverter tac) {

		this.analyzeAliases(tac, true);

		if (!MyOptions.option_L) {
			this.literalAnalysis = new DummyLiteralAnalysis();
			return this.literalAnalysis;
		}

		if (this.connectorComp == null) {
			this.connectorComp = new ConnectorComputation(tac.getAllFunctions(), tac.getMainFunction(), this.kSize);
			connectorComp.compute();
			this.workList = new InterproceduralWorklistBetter(
					new InterproceduralWorklistOrder(tac, this.connectorComp));

		}

		System.out.println("\n*** initializing literal analysis ***\n");
		this.literalAnalysis = new LiteralAnalysis(tac, this.aliasAnalysis, new CallStringAnalysis(this.connectorComp),
				this.workList);
		System.out.println("\n*** performing literal analysis ***\n");
		this.literalAnalysis.analyze();
		System.out.println("\n*** cleaning up ***\n");
		this.literalAnalysis.clean();
		System.out.println("\nFinished.");
		return this.literalAnalysis;

	}

	public void analyzeTaint(TacConverter tac, boolean functional) {

		this.analyzeLiterals(tac);
		AbstractAnalysisType enclosingAnalysis;
		CallGraph callGraph = null;
		GlobalsModificationAnalysis modAnalysis = null;
		if (functional) {
			System.out.println("functional analysis!");
			enclosingAnalysis = new FunctionalAnalysis();
			this.workList = new InterproceduralWorklistPoor();
		} else {
			if (this.connectorComp == null) {
				this.connectorComp = new ConnectorComputation(tac.getAllFunctions(), tac.getMainFunction(), this.kSize);
				connectorComp.compute();
				this.workList = new InterproceduralWorklistBetter(
						new InterproceduralWorklistOrder(tac, this.connectorComp));
				connectorComp.stats(false);
			}
			if (MyOptions.optionV) {
				System.out.println("call-string analysis!");
			}

			enclosingAnalysis = new CallStringAnalysis(this.connectorComp);

			Utils.writeToFile(this.connectorComp.dump(),
					MyOptions.graphPath + "/" + "/calledby_" + MyOptions.entryFile.getName() + ".txt");

			callGraph = this.connectorComp.getCallGraph();
			if (this.aliasAnalysis instanceof DummyAliasAnalysis) {
				modAnalysis = new GlobalsModificationAnalysis(tac.getAllFunctions(), callGraph);
			}

		}

		this.gta = GenericTaintAnalysis.createAnalysis(tac, enclosingAnalysis, this, this.workList, modAnalysis);
		if (this.gta == null) {
			Utils.bail("Please specify a valid type of taint analysis.");
		}
		System.out.println("\n*** performing taint analysis ***\n");
		gta.analyze();

		System.out.println("\nFinished.");

	}

	InclusionDominatorAnalysis analyzeIncDom(TacFunction function) {

		System.out.println("\n*** initializing incdom analysis ***\n");
		this.incDomAnalysis = new InclusionDominatorAnalysis(function);
		System.out.println("\n*** performing incdom analysis ***\n");
		this.incDomAnalysis.analyze();

		return this.incDomAnalysis;
	}

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

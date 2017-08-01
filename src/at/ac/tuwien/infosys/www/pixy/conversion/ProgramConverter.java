package at.ac.tuwien.infosys.www.pixy.conversion;

import java.io.*;
import java.util.*;

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
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.phpParser.ParseTree;
import at.ac.tuwien.infosys.www.pixy.phpParser.PhpLexer;
import at.ac.tuwien.infosys.www.pixy.phpParser.PhpParser;

public class ProgramConverter {

	private File workingDirectoryFile;

	private IncludeGraph includeGraph;

	private TacConverter baseTac;

	private int numConvertedFiles;

	private LiteralAnalysis literalAnalysis;

	private boolean specialNodes;

	private boolean useAliasAnalysis;

	private int lines;
	private boolean countLines = false;

	private Set<File> allFiles;

	private Set<Include> skipUs;

	private SymbolTable superSymbolTable;

	private enum IncStatus {
		NOTFOUND, INCLUDED, CYCLIC
	}

	private TypeAnalysis typeAnalysis;

	public ProgramConverter(boolean specialNodes, boolean useAliasAnalysis) {

		this.numConvertedFiles = 0;

		this.workingDirectoryFile = MyOptions.entryFile.getParentFile();

		this.includeGraph = new IncludeGraph(MyOptions.entryFile);

		this.specialNodes = specialNodes;
		this.useAliasAnalysis = useAliasAnalysis;

		this.lines = 0;

		this.allFiles = new HashSet<File>();
		this.allFiles.add(MyOptions.entryFile);
		this.CreateIncludeFileIncludes(MyOptions.entryFile.getParentFile());
		this.skipUs = new HashSet<Include>();

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

	void CreateIncludeFileIncludes(File file) {

		for (File child : file.listFiles()) {
			if (child.isDirectory()) {
				CreateIncludeFileIncludes(child);
			} else if (child.isFile()) {
				int i = child.getName().lastIndexOf('.');
				if (i > 0) {
					String extension = child.getName().substring(i + 1);
					if (extension.toLowerCase().equalsIgnoreCase("php")) {
						if (!allFiles.contains(child)) {
							this.allFiles.add(child);
						}
					}
				}
			}

		}
	}

	public TacConverter getTac() {
		return this.baseTac;
	}

	public Set<File> getAllFiles() {
		return this.allFiles;
	}

	public SymbolTable getSuperSymbolTable() {
		return this.superSymbolTable;
	}

	public void convert() {

		try {

			System.err.println("inside convert");
			ParseTree parseTree = this.parse(MyOptions.entryFile.getPath());
			baseTac = new TacConverter(parseTree, this.specialNodes, this.numConvertedFiles++, MyOptions.entryFile,
					this);
			baseTac.convert();

			List<Include> processUs = baseTac.getIncludeNodes();

			boolean goOn = true;

			Set<Include> topIncludes = new HashSet<Include>();

			SortedMap<Include, String> notFoundLiteralIncludes = new TreeMap<Include, String>();

			SortedMap<Include, String> notFoundDynamicIncludes = new TreeMap<Include, String>();

			int resolvedLit = 0;

			int resolvedNonLit = 0;

			int cyclic = 0;

			int iteration = 0;

			while (goOn && !MyOptions.optionW) {

				iteration++;

				goOn = false;
				boolean nonLiteralIncludes = false;

				List<Include> weComeAfterwards;

				System.out.println();
				System.out.println("*** resolving literal includes ***");
				System.out.println();
				for (File currentfile : this.allFiles) {

					IncStatus status = this.include(currentfile);

					switch (status) {
					case INCLUDED:
						resolvedLit++;
						break;
					case NOTFOUND:
						break;
					case CYCLIC:
						cyclic++;
						break;
					default:
						throw new RuntimeException("SNH");
					}
				}

				while (!processUs.isEmpty()) {

					weComeAfterwards = new LinkedList<Include>();

					for (Iterator<Include> iter = processUs.iterator(); iter.hasNext();) {
						Include includeNode = (Include) iter.next();
						if (this.skipUs.contains(includeNode)) {
							continue;
						}
						if (!includeNode.isLiteral()) {
							nonLiteralIncludes = true;
							continue;
						}
						IncStatus status = this.include(includeNode.getIncludeMe().toString(), includeNode,
								includeNode.getIncludeFunction(), weComeAfterwards);

						switch (status) {
						case INCLUDED:
							resolvedLit++;
							break;
						case NOTFOUND:
							this.skipUs.add(includeNode);
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
				this.baseTac.assignFunctions();
				if (!nonLiteralIncludes) {
					break;
				}
				System.out.println();
				System.out.println("*** resolving non-literal includes ***");
				System.out.println();

				this.baseTac.backpatch();

				int kSize = 1;
				ConnectorComputation connectorComp = new ConnectorComputation(baseTac.getAllFunctions(),
						baseTac.getMainFunction(), kSize);
				connectorComp.compute();
				InterproceduralWorklist workList = new InterproceduralWorklistBetter(
						new InterproceduralWorklistOrder(baseTac, connectorComp));
				connectorComp.stats(false);

				AliasAnalysis aliasAnalysis = new DummyAliasAnalysis();

				literalAnalysis = new LiteralAnalysis(baseTac, aliasAnalysis, new CallStringAnalysis(connectorComp),
						workList);
				literalAnalysis.analyze();

				processUs = literalAnalysis.getIncludeNodes();
				weComeAfterwards = new LinkedList<Include>();
				notFoundDynamicIncludes = new TreeMap<Include, String>();
				topIncludes = new HashSet<Include>();

				for (Include includeNode : processUs) {

					if (this.skipUs.contains(includeNode)) {
						continue;
					}

					Literal includedLit = literalAnalysis.getLiteral(includeNode.getIncludeMe(), includeNode);
					String includedString = null;

					if (includedLit == Literal.TOP) {

						List<String> includeTargets = ParseNodeHeuristics.getPossibleIncludeTargets(includeNode,
								literalAnalysis, notFoundDynamicIncludes, this.workingDirectoryFile.getPath());

						if (includeTargets == null) {
							topIncludes.add(includeNode);
							continue;
						} else if (includeTargets.isEmpty()) {
							continue;
						} else if (includeTargets.size() == 1) {
							includedString = includeTargets.get(0);
						} else {
							throw new RuntimeException("SNH");
						}

					} else {
						includedString = includedLit.toString();
					}
					IncStatus status = this.include(includedString, includeNode, includeNode.getIncludeFunction(),
							weComeAfterwards);

					switch (status) {
					case INCLUDED:
						goOn = true;
						resolvedNonLit++;
						break;
					case CYCLIC:
						this.skipUs.add(includeNode);
						cyclic++;
						break;
					case NOTFOUND:
						notFoundDynamicIncludes.put(includeNode, includedString);
						break;
					default:
						throw new RuntimeException("SNH");
					}
				}

				System.out.println();
				this.baseTac.assignFunctions();
				processUs = weComeAfterwards;
				processUs.addAll(topIncludes);
				processUs.addAll(notFoundDynamicIncludes.keySet());
			}

			this.removeUnreachables(topIncludes, notFoundDynamicIncludes);

			this.literalAnalysis = null;

			if (this.useAliasAnalysis) {

				this.baseTac.backpatch(true, true, null, null);

				this.baseTac.generateShadows();

			} else {

				this.baseTac.replaceGlobals();

				this.baseTac.backpatch(true, false, null, null);

				System.out.println();
				System.out.println("*** performing type analysis ***");
				System.out.println();

				ConnectorComputation connectorComp = new ConnectorComputation(baseTac.getAllFunctions(),
						baseTac.getMainFunction(), 0);
				connectorComp.compute();
				InterproceduralWorklist workList = new InterproceduralWorklistBetter(
						new InterproceduralWorklistOrder(baseTac, connectorComp));
				this.typeAnalysis = new TypeAnalysis(this.baseTac, new CallStringAnalysis(connectorComp), workList);
				typeAnalysis.analyze();

				this.baseTac.backpatch(true, true, typeAnalysis, connectorComp.getCallGraph());

			}

			int a = 1;
			if (a == 1) {
				if (MyOptions.optionV) {
					System.out.println("creating basic blocks");
				}
				this.baseTac.createBasicBlocks();
			} else {
				if (MyOptions.optionV) {
					System.out.println("not creating basic blocks");
				}
			}

			this.baseTac.assignFunctions();

			if (this.countLines) {
				System.out.println("Lines: " + this.lines);
			}

			if (true && !MyOptions.optionW) {
				System.out.println();
				System.out.println("inclusion iterations:            " + iteration);
				System.out.println("resolved literal includes:       " + resolvedLit);
				System.out.println("resolved non-literal includes:   " + resolvedNonLit);
				System.out.println("cyclic includes:                 " + cyclic);
				System.out.println("not found includes:              "
						+ (notFoundLiteralIncludes.size() + notFoundDynamicIncludes.size()));
				for (Map.Entry<Include, String> entry : notFoundLiteralIncludes.entrySet()) {
					System.out.println("- " + entry.getKey().getLoc());
					System.out.println("   [" + entry.getValue() + "]");
				}
				for (Map.Entry<Include, String> entry : notFoundDynamicIncludes.entrySet()) {
					System.out.println("- " + entry.getKey().getLoc());
					System.out.println("   [" + entry.getValue() + "]");
				}
				System.out.println("unresolved non-literal includes: " + topIncludes.size());
				List<Include> topIncludesList = new LinkedList<Include>(topIncludes);
				Collections.sort(topIncludesList);
				for (Include includeNode : topIncludesList) {
					System.out.println("- " + includeNode.getLoc());
				}
				System.out.println();

				Utils.writeToFile(this.includeGraph.dump(),
						MyOptions.graphPath + "/includes_" + MyOptions.entryFile.getName() + ".txt");
			}

			this.literalAnalysis = null;
			this.includeGraph = null;
			this.skipUs = null;

			this.baseTac.addSuperGlobalElements();

			if (!MyOptions.optionB && !MyOptions.optionW) {
				this.baseTac.stats();
			}
			System.out.println();

			baseTac.assignReversePostOrder();
		} catch (Exception ex) {

		}
	}

	private void removeUnreachables(Set<Include> includeSet, Map<Include, String> includeMap) {

		for (Iterator<Include> iter = includeSet.iterator(); iter.hasNext();) {
			Include includeNode = (Include) iter.next();
			AbstractInterproceduralAnalysisNode incAnNode = literalAnalysis.getAnalysisNode(includeNode);
			if (incAnNode.getPhi().isEmpty()) {
				iter.remove();
			}
		}
		for (Iterator<Map.Entry<Include, String>> iter = includeMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Include, String> entry = (Map.Entry<Include, String>) iter.next();
			Include includeNode = (Include) entry.getKey();
			AbstractInterproceduralAnalysisNode incAnNode = literalAnalysis.getAnalysisNode(includeNode);
			if (incAnNode.getPhi().isEmpty()) {
				iter.remove();
			}
		}
	}

	public ParseTree parse(String fileName) {

		try {
			fileName = (new File(fileName)).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		ParseTree parseTree = null;
		try {
			PhpLexer lexer = new PhpLexer(new FileReader(fileName));
			lexer.setFileName(fileName);
			PhpParser parser = new PhpParser(lexer);
			ParseNode rootNode = (ParseNode) parser.parse().value;
			parseTree = new ParseTree(rootNode);
		} catch (FileNotFoundException e) {
			Utils.bail("File not found: " + fileName);
		} catch (Exception e) {
			if (!MyOptions.optionW) {
				System.err.println("Error parsing " + fileName);
				throw new RuntimeException(e.getMessage());

			} else {
				Utils.bail();
			}
		}

		if (this.countLines) {
			this.lines += this.countLines(fileName);
		}

		return parseTree;
	}

	private File makeFile(String fileName, File includingFile) {

		File findMe = new File(fileName);
		if (findMe.isAbsolute()) {
			if (findMe.isFile()) {
				return findMe;
			}
		}

		for (Iterator<File> iter = MyOptions.includePaths.iterator(); iter.hasNext();) {
			File includePath = (File) iter.next();

			File searchIn;
			if (includePath.isAbsolute()) {
				searchIn = includePath;
			} else {
				searchIn = new File(this.workingDirectoryFile, includePath.getPath());
			}

			findMe = new File(searchIn, fileName);
			if (findMe.isFile()) {
				return findMe;
			}
		}

		if (!(fileName.startsWith("./") || fileName.startsWith("../"))) {
			for (Iterator<File> iter = MyOptions.includePaths.iterator(); iter.hasNext();) {
				File includePath = (File) iter.next();

				if (includePath.isAbsolute()) {
					continue;
				}

				try {
					File searchIn = new File(includingFile.getCanonicalFile().getParentFile(), includePath.getPath());
					findMe = new File(searchIn, fileName);

					if (findMe.isFile()) {

						return findMe;
					}

				} catch (IOException e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		}
		return null;
	}

	public IncStatus include(File includedFile) {

		if (includedFile == null) {
			return IncStatus.NOTFOUND;
		}
		try {
			this.allFiles.add(includedFile.getCanonicalFile());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		String includedFilePath = null;
		try {
			includedFilePath = includedFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		if (!MyOptions.optionB) {
			System.out.print(".");
		}
		try {
			ParseTree parseTree = this.parse(includedFilePath);
			TacConverter tac = new TacConverter(parseTree, this.specialNodes, this.numConvertedFiles++, includedFile,
					this);
			tac.convert();
			return IncStatus.INCLUDED;
		} catch (Exception ex) {
			return IncStatus.NOTFOUND;
		}
	}

	public IncStatus include(String includedFileName, Include includeNode, TacFunction function,
			List<Include> includeNodes) {

		File includingFile = includeNode.getFile();
		File includedFile = this.makeFile(includedFileName, includingFile);
		if (includedFile == null) {
			return IncStatus.NOTFOUND;
		}
		try {
			if (!allFiles.contains(includedFile.getCanonicalFile()))
				this.allFiles.add(includedFile.getCanonicalFile());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		boolean acyclic = this.includeGraph.addAcyclicEdge(includeNode.getFile(), includedFile);
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
			TacConverter tac = new TacConverter(parseTree, this.specialNodes, this.numConvertedFiles++, includedFile,
					this);
			tac.convert();
			this.baseTac.include(tac, includeNode, function);
			includeNodes.addAll(tac.getIncludeNodes());
			return IncStatus.INCLUDED;
		} else {
			return IncStatus.CYCLIC;
		}

	}

	@SuppressWarnings("resource")
	private int countLines(String fileName) {

		int lines = 0;
		try {
			File countMe = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(countMe));
			while (reader.readLine() != null) {
				lines++;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return lines;

	}

	private void addSuperGlobal(String varName) {

		Variable var = this.superSymbolTable.getVariable(varName);

		if (var == null) {
			var = new Variable(varName, this.superSymbolTable);
			this.superSymbolTable.add(var);
		}
		var.setIsSuperGlobal(true);
	}

}

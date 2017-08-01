package at.ac.tuwien.infosys.www.pixy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.BuiltinFunctions;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.TacOperators;

public class MyOptions {

	public static boolean optionA = false;
	public static boolean option_A = false;
	public static boolean optionB = false;
	public static boolean optionC = false;
	public static boolean optionD = false;
	public static boolean optionF = false;
	public static boolean optionG = true;
	public static boolean optionI = false;
	public static boolean optionL = false;
	public static boolean option_L = false;
	public static boolean optionM = false;
	public static boolean optionP = false;
	public static boolean option_P = false;
	public static boolean optionQ = false;
	public static boolean optionR = false;
	public static String optionS = null;
	public static boolean optionT = false;
	public static boolean optionW = false;
	public static boolean optionV = false;
	public static boolean option_V = true;
	public static boolean option_OH = true;
	public static boolean option_VPS = false;
	public static boolean option_VXSS = false;
	public static boolean option_VSQK = false;
	public static boolean option_VXP = false;
	public static boolean option_VCExec = false;
	public static boolean option_VCEval = false;

	public static boolean countPaths = false;

	public static File entryFile;

	public static File pixy_home;

	public static String fsa_home;

	public static String configDir = "config";

	public static List<File> includePaths;

	public static String phpBin;

	public static String graphPath;

	public static String outputHtmlPath;

	public static Set<String> harmlessServerIndices;

	public static boolean isHarmlessServerVar(String varName) {

		String index;
		if (varName.startsWith("$_SERVER[") && varName.endsWith("]")) {
			index = varName.substring(9, varName.length() - 1);
		} else if (varName.startsWith("$HTTP_SERVER_VARS[") && varName.endsWith("]")) {
			index = varName.substring(18, varName.length() - 1);
		} else {
			return false;
		}
		if (harmlessServerIndices.contains(index)) {
			return true;
		} else {
			return false;
		}
	}

	public static void addHarmlessServerIndex(String indexName) {
		harmlessServerIndices.add(indexName);
	}

	public static void readModelFiles() {
		for (VulnerabilityAnalysisInformation dci : analyses) {
			FunctionModels fm = readModelFile(dci);
			dci.setFunctionModels(fm);
		}
	}

	private static FunctionModels readModelFile(VulnerabilityAnalysisInformation dci) {

		Set<String> f_evil = new HashSet<String>();
		Map<String, Set<Integer>> f_multi = new HashMap<String, Set<Integer>>();
		Map<String, Set<Integer>> f_invMulti = new HashMap<String, Set<Integer>>();
		Set<String> f_strongSanit = new HashSet<String>();
		Map<String, Set<Integer>> f_weakSanit = new HashMap<String, Set<Integer>>();

		String strongSanitMarker = "0";
		String weakSanitMarker = "1";
		String multiMarker = "2";
		String invMultiMarker = "3";
		String evilMarker = "4";

		String modelFileName = MyOptions.pixy_home + "/" + MyOptions.configDir + "/model_" + dci.getName() + ".txt";
		File modelFile = new File(modelFileName);
		Properties sinkProps = new Properties();
		try {
			FileInputStream in = new FileInputStream(modelFile);
			sinkProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			Utils.bail("Error: Can't find configuration file: " + modelFileName);
		} catch (IOException e) {
			Utils.bail("Error: I/O exception while reading configuration file:" + modelFileName, e.getMessage());
		}

		Class<?> tacOps;
		try {
			tacOps = Class.forName("at.ac.tuwien.infosys.www.pixy.conversion.TacOperators");
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("SNH");
		}

		for (Map.Entry<Object, Object> propsEntry : sinkProps.entrySet()) {

			String funcName = ((String) propsEntry.getKey()).trim();
			String funcList = (String) propsEntry.getValue();

			if (!BuiltinFunctions.isBuiltinFunction(funcName)) {
				if (funcName.startsWith("op(") && funcName.endsWith(")")) {
					funcName = funcName.substring(3, funcName.length() - 1);
					try {
						Field field = tacOps.getDeclaredField(funcName);
						funcName = TacOperators.opToName(field.getInt(null));
					} catch (NoSuchFieldException e) {
						Utils.bail("Error: Non-builtin function in config file: " + funcName);
						continue;
					} catch (IllegalAccessException e) {
						Utils.bail("Error: Non-builtin function in config file: " + funcName);
					}
				} else {
					Utils.bail("Error: Non-builtin function in config file: " + funcName);
				}
			}

			StringTokenizer funcTokenizer = new StringTokenizer(funcList, ":");
			if (!funcTokenizer.hasMoreTokens()) {
				Utils.bail("Error: Missing type for builtin function model: " + funcName);
			}

			String type = funcTokenizer.nextToken().trim();
			if (type.equals(strongSanitMarker)) {

				f_strongSanit.add(funcName);

			} else if (type.equals(weakSanitMarker)) {

				Set<Integer> params = new HashSet<Integer>();
				while (funcTokenizer.hasMoreTokens()) {
					String param = funcTokenizer.nextToken().trim();
					try {
						params.add(Integer.valueOf(param));
					} catch (NumberFormatException e) {
						Utils.bail("Error: Illegal parameter for builtin function model: " + funcName);
					}
				}
				f_weakSanit.put(funcName, params);

			} else if (type.equals(multiMarker)) {

				Set<Integer> params = new HashSet<Integer>();
				while (funcTokenizer.hasMoreTokens()) {
					String param = funcTokenizer.nextToken().trim();
					try {
						params.add(Integer.valueOf(param));
					} catch (NumberFormatException e) {
						Utils.bail("Error: Illegal parameter for builtin function model: " + funcName);
					}
				}
				f_multi.put(funcName, params);

			} else if (type.equals(invMultiMarker)) {

				Set<Integer> params = new HashSet<Integer>();
				while (funcTokenizer.hasMoreTokens()) {
					String param = funcTokenizer.nextToken().trim();
					try {
						params.add(Integer.valueOf(param));
					} catch (NumberFormatException e) {
						Utils.bail("Error: Illegal parameter for builtin function model: " + funcName);
					}
				}
				f_invMulti.put(funcName, params);

			} else if (type.equals(evilMarker)) {
				f_evil.add(funcName);
			} else {
				Utils.bail("Error: Unknown type for builtin function model: " + funcName);
			}

		}

		f_strongSanit.add(InternalStrings.suppression);

		return new FunctionModels(f_evil, f_multi, f_invMulti, f_strongSanit, f_weakSanit);
	}

	public static boolean isSink(String functionName) {
		for (VulnerabilityAnalysisInformation dci : analyses) {
			if (dci.getSinks().containsKey(functionName)) {
				return true;
			}
		}
		return false;
	}

	public static void addSink(Map<String, Set<Integer>> sinkMap, String name, int... indices) {
		Set<Integer> indexSet;
		if (indices == null) {
			indexSet = null;
		} else {
			indexSet = new HashSet<Integer>();
			for (int index : indices) {
				indexSet.add(index);
			}
		}
		sinkMap.put(name, indexSet);
	}

	private static String readSinkFile(String sinkFileName, Map<String, Set<Integer>> sinks) {

		File sinkFile = new File(sinkFileName);
		Properties sinkProps = new Properties();
		try {
			FileInputStream in = new FileInputStream(sinkFile);
			sinkProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Warning: Can't find sink configuration file: " + sinkFileName);
		} catch (IOException e) {
			System.out.println("Warning: I/O exception while reading configuration file:" + sinkFileName);
			System.out.println(e.getMessage());
		}
		String sinkType = null;
		for (Map.Entry<Object, Object> propsEntry : sinkProps.entrySet()) {
			String functionName = (String) propsEntry.getKey();
			String params = (String) propsEntry.getValue();

			if (functionName.equals("sinkType")) {
				sinkType = params;
				continue;
			}

			StringTokenizer paramTokenizer = new StringTokenizer(params, ":");
			int numTokens = paramTokenizer.countTokens();
			Set<Integer> paramSet = new HashSet<Integer>();
			while (paramTokenizer.hasMoreTokens()) {
				String param = paramTokenizer.nextToken();
				try {
					paramSet.add(Integer.parseInt(param));
				} catch (NumberFormatException e) {
					System.out.println("Warning: Illegal param argument for " + functionName + " in " + sinkFileName);
					System.out.println("param: " + param);
				}
			}
			if (numTokens == 0) {
				paramSet = null;
			}

			sinks.put(functionName, paramSet);
		}

		return sinkType;
	}

	public static void initSinks() {
		for (VulnerabilityAnalysisInformation dci : analyses) {
			String sinkFileName = "sinks_" + dci.getName() + ".txt";
			Map<String, Set<Integer>> sinks = new HashMap<String, Set<Integer>>();
			readSinkFile(MyOptions.pixy_home + "/" + MyOptions.configDir + "/" + sinkFileName, sinks);
			dci.addSinks(sinks);
		}
	}

	public static void readCustomSinkFiles() {

		if (MyOptions.optionS != null) {

			StringTokenizer sfnTokenizer = new StringTokenizer(MyOptions.optionS, ":");

			while (sfnTokenizer.hasMoreTokens()) {
				String sinkFileName = sfnTokenizer.nextToken();

				Map<String, Set<Integer>> sinks = new HashMap<String, Set<Integer>>();

				String sinkType = readSinkFile(sinkFileName, sinks);

				if (sinkType == null) {
					System.out.println("Missing sinkType in file " + sinkFileName);
				} else {
					VulnerabilityAnalysisInformation dci = name2Analysis.get(sinkType);
					if (dci == null) {
						System.out.println("Invalid sinkType in file " + sinkFileName);
						System.out.println("- " + sinkType);
					} else {
						dci.addSinks(sinks);
					}
				}
			}
		}
	}

	private static VulnerabilityAnalysisInformation[] analyses = {
			new VulnerabilityAnalysisInformation("xss", "XssAnalysis"),
			new VulnerabilityAnalysisInformation("sql", "SqlAnalysis"),
			new VulnerabilityAnalysisInformation("xpath", "XPathAnalysis"),
			new VulnerabilityAnalysisInformation("cmdexec", "CommandExecutionAnalysis"),
			new VulnerabilityAnalysisInformation("codeeval", "CodeEvaluatingAnalysis"),
			new VulnerabilityAnalysisInformation("sqlsanit", "sanit.SQLSanitAnalysis"),
			new VulnerabilityAnalysisInformation("xsssanit", "sanit.XSSSanitAnalysis"),
			new VulnerabilityAnalysisInformation("file", "FileAnalysis") };

	private static Map<String, VulnerabilityAnalysisInformation> name2Analysis;

	private static Map<String, String> className2Name;

	public static boolean setAnalyses(String taintStrings) {

		if (taintStrings == null) {
			return true;
		}

		StringTokenizer st = new StringTokenizer(taintStrings, ":");
		while (st.hasMoreTokens()) {

			String taintString = st.nextToken();

			VulnerabilityAnalysisInformation dci = name2Analysis.get(taintString);
			if (dci != null) {
				dci.setPerformMe(true);
			} else {
				System.out.println("Invalid analysis type: " + taintString + ".");
				System.out.println("Choose one of the following: ");
				StringBuilder b = new StringBuilder();
				for (String name : name2Analysis.keySet()) {
					b.append(name + ", ");
				}
				if (!name2Analysis.isEmpty()) {
					b.deleteCharAt(b.length() - 1);
					b.deleteCharAt(b.length() - 1);
				}
				System.out.println(b.toString());
				return false;
			}
		}
		return true;
	}

	static {

		String home = System.getProperty("pixy.home");
		if (home == null) {
			Utils.bail("System property 'pixy.home' not set");
		}
		try {
			MyOptions.pixy_home = (new File(home)).getCanonicalFile();
		} catch (IOException e) {
			Utils.bail("can't set pixy_home");
		}

		harmlessServerIndices = new HashSet<String>();

		name2Analysis = new HashMap<String, VulnerabilityAnalysisInformation>();
		className2Name = new HashMap<String, String>();
		for (VulnerabilityAnalysisInformation dci : analyses) {
			name2Analysis.put(dci.getName(), dci);
			className2Name.put(dci.getClassName(), dci.getName());

		}

	}

	public static VulnerabilityAnalysisInformation getDepClientInfo(String analysisClassName) {
		analysisClassName = analysisClassName.substring(analysisClassName.lastIndexOf(".") + 1);
		String analysisName = className2Name.get(analysisClassName);
		if (analysisName == null) {
			throw new RuntimeException("Illegal analysis class: " + analysisClassName);
		}
		VulnerabilityAnalysisInformation dci = name2Analysis.get(analysisName);
		if (dci == null) {
			throw new RuntimeException("Illegal analysis name: " + analysisName);
		}
		return dci;

	}

	public static VulnerabilityAnalysisInformation[] getDepClients() {
		return analyses;
	}

	public static String getAnalysisNames() {
		StringBuilder b = new StringBuilder();
		for (String name : name2Analysis.keySet()) {
			b.append(name);
			b.append(", ");
		}
		if (b.length() > 2) {
			b.deleteCharAt(b.length() - 1);
			b.deleteCharAt(b.length() - 1);
		}
		return b.toString();
	}

}

package at.ac.tuwien.infosys.www.pixy;

import java.io.*;
import java.util.*;

public class Utils {

	public static String basename(String s) {
		return (new File(s).getName());
	}

	public static void writeToFile(String s, String fileName) {
		try {
			Writer outWriter = new FileWriter(fileName);
			outWriter.write(s);
			outWriter.close();
		} catch (IOException e) {
			System.out.println("Warning: Could not write to file " + fileName);
			System.out.println(e.getMessage());
		}
	}

	public static void appendToFile(String s, String fileName) {
		try {
			Writer outWriter = new FileWriter(fileName, true);
			outWriter.write("\n---------------------\n");
			outWriter.write(s);
			outWriter.close();
		} catch (IOException e) {
			System.out.println("Warning: Could not write to file " + fileName);
			System.out.println(e.getMessage());
		}
	}

	public static String readFile(String fileName) {
		return readFile(new File(fileName));
	}

	public static String readFile(File file) {

		StringBuilder retme = new StringBuilder();
		try {
			FileReader reader = new FileReader(file);
			BufferedReader inreader = new BufferedReader(reader);
			String line;
			while ((line = inreader.readLine()) != null) {
				retme.append(line);
				retme.append("\n");
			}
			inreader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return retme.toString();

	}

	public static List<File> fileListFromFile(String rootFileName) {
		List<File> retMe = new LinkedList<File>();
		File rootFile = new File(rootFileName);
		File rootDir = rootFile.getParentFile();
		fileListHelper(rootDir, retMe);
		return retMe;
	}

	public static List<File> fileListFromDir(String rootDirName) {
		List<File> retMe = new LinkedList<File>();
		File rootDir = new File(rootDirName);
		fileListHelper(rootDir, retMe);
		return retMe;
	}

	private static void fileListHelper(File dir, List<File> retMe) {

		try {

			List<File> dirList = new LinkedList<File>();
			File[] fileList = dir.listFiles();
			for (File f : fileList) {
				if (f.isFile()) {
					retMe.add(f.getCanonicalFile());
				} else if (f.isDirectory()) {
					dirList.add(f);
				}
			}
			for (File d : dirList) {
				fileListHelper(d, retMe);
			}

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void bail(String... msgs) {
		for (String msg : msgs) {
			System.err.println(msg);
		}
		System.exit(1);
	}

	public static String inputStreamToString(InputStream instream) {
		StringBuilder retMe = new StringBuilder();
		try {
			BufferedReader inreader = new BufferedReader(new InputStreamReader(instream));
			String line;
			while ((line = inreader.readLine()) != null) {
				retMe.append(line);
				retMe.append("\n");
			}
			inreader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return retMe.toString();
	}

	public static String exec(String command) {

		String execErrorLog = MyOptions.graphPath + "/exec-errors.txt";

		StringBuilder retMe = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec(command);

			InputStream instream = p.getInputStream();
			InputStream errstream = p.getErrorStream();
			p.waitFor();

			try {
				BufferedReader inreader = new BufferedReader(new InputStreamReader(instream));
				String line;
				while ((line = inreader.readLine()) != null) {
					retMe.append(line);
					retMe.append("\n");
				}
				inreader.close();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}

			Writer outWriter = new FileWriter(execErrorLog, true);
			try {
				BufferedReader inreader = new BufferedReader(new InputStreamReader(errstream));
				String line;

				while ((line = inreader.readLine()) != null) {
					outWriter.append(line + "\n");
				}
				inreader.close();
			} catch (IOException e) {
				outWriter.close();
				throw new RuntimeException(e.getMessage());
			}

			if (p.exitValue() != 0) {
				System.err.println("Exit value: " + p.exitValue());
				System.err.println("- " + command);
				new RuntimeException().printStackTrace();
			}
			outWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		return retMe.toString();

	}

	public static void e() {
		throw new RuntimeException();
	}

	public static void quickDot(String s) {

		try {
			Process p = Runtime.getRuntime().exec("dotty -");
			OutputStream outstream = p.getOutputStream();
			Writer outwriter = new OutputStreamWriter(outstream);
			outwriter.write(s);
			outwriter.flush();
			outwriter.close();
			p.waitFor();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}

	public static void copyFile(String src, String dst) {
		try {
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(dst);
			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static void deleteDirectory(File dir) {
		for (File entry : dir.listFiles()) {
			if (entry.isDirectory()) {
				deleteDirectory(entry);
			}
			entry.delete();
		}
		dir.delete();
	}
}

package at.ac.tuwien.infosys.www.pixy;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Utils {

    // same as PHP's basename()
    public static String basename(String s) {
        return (new File(s).getName());
    }

    // writes the given string to a file with the given name
    // (provide full path)
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

    // appends the given string to a file with the given name
    // (provide full path)
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

    // reads the given file into a string and returns it
    // (provide full path)
    public static String readFile(String fileName) {
        return readFile(new File(fileName));
    }

    // reads the given file into a string and returns it
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

    // - input: the name of some file
    // - output: a list with all files (canonical) located in the
    //   same directory and in enclosed directories
    public static List<File> fileListFromFile(String rootFileName) {
        List<File> retMe = new LinkedList<File>();
        File rootFile = new File(rootFileName);
        File rootDir = rootFile.getParentFile();
        fileListHelper(rootDir, retMe);
        return retMe;
    }

    // - input: the name of some directory
    // - output: a list with all files (canonical) located in this
    //   directory and in enclosed directories
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

    // prints the arguments to System.err and exits with code 1;
    // don't use System.exit directly, since chances are that you
    // will write the error message to System.out (not good)
    public static void bail(String... msgs) {
        for (String msg : msgs) {
            System.err.println(msg);
        }
        System.exit(1);
    }

    // converts an input stream to string;
    // might lead to slight changes in linebreaks
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

    // executes the given command and returns the output;
    // error output is written to a logfile
    public static String exec(String command) {

        String execErrorLog = MyOptions.graphPath + "/exec-errors.txt";

        StringBuilder retMe = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(command);

            InputStream instream = p.getInputStream();      // standard output of prog
            InputStream errstream = p.getErrorStream();     // error output of prog
            p.waitFor();

            // read output
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

            // read and print errors
            Writer outWriter = new FileWriter(execErrorLog, true);
            try {
                BufferedReader inreader = new BufferedReader(new InputStreamReader(errstream));
                String line;

                while ((line = inreader.readLine()) != null) {
                    outWriter.append(line + "\n");
                }
                inreader.close();
            } catch (IOException e) {
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

    // saves typing during debugging
    public static void e() {
        throw new RuntimeException();
    }

    // feeds the given string into dotty
    public static void quickDot(String s) {

        try {
            Process p = Runtime.getRuntime().exec("dotty -");

            OutputStream outstream = p.getOutputStream();    // standard input of prog

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
        // from "GoToJava"
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
        }
    }

    // deletes a directory, even if it is non-empty
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
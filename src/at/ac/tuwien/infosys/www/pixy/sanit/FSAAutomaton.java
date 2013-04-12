package at.ac.tuwien.infosys.www.pixy.sanit;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;

public class FSAAutomaton {

    // the internal FSA-Utils representation (type "normal")
    private String str;

    // a few meta-characters
    static String ccurly = encode('}');
    static String ocurly = encode('{');
    static String backslash = encode('\\');
    static String point = encode('.');
    static String minus = encode('-');
    static String dollar = encode('$');
    static String circum = encode('^');
    static String csqbra = encode(']');
    static String osqbra = encode('[');
    static String union = encode('|');
    static String cbra = encode(')');
    static String obra = encode('(');
    static String plus = encode('+');
    static String star = encode('*');
    static String slash = encode('/');

    static String single_quote = encode('\'');
    static String opointy = encode('<');



    FSAAutomaton(String str) {
        this.str = str;
    }

    public FSAAutomaton clone() {
        return new FSAAutomaton(this.str);
    }

    public String getString() {
        return this.str;
    }

    // creates an automaton that accepts exactly the given string
    public static FSAAutomaton makeString(String s) {

        // convert string to FSA regexp syntax
        s = makeRegexp(s);

        // command

        // this fails if string holds white space...
        String c = MyOptions.fsa_home + "/" + "fsa -r " + s;

        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;

    }

    // converts the given string into an appropriate regexp,
    // should also take care of escaping characters
    private static String makeRegexp(String s) {
        if (s.isEmpty()) {
            return "[]";
        }
        StringBuilder retMe = new StringBuilder();
        retMe.append('[');
        for (Character c : s.toCharArray()) {
            retMe.append(encode(c));
            retMe.append(',');
        }
        retMe.setCharAt(retMe.length() - 1, ']');

        return retMe.toString();
    }

    // makes a "dot star" automaton
    public static FSAAutomaton makeAnyString() {
        String c = MyOptions.fsa_home + "/" + "fsa -r [kleene_star(?)]";
        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;
    }

    public FSAAutomaton concatenate(FSAAutomaton auto) {
        String arg1File = this.toFile("temp1.auto");
        String arg2File = auto.toFile("temp2.auto");
        String c = MyOptions.fsa_home + "/" +
            "fsa -r concat(file('"+arg1File+"'),file('"+arg2File+"'))";
        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;
    }

    public FSAAutomaton union(FSAAutomaton auto) {
        String arg1File = this.toFile("temp1.auto");
        String arg2File = auto.toFile("temp2.auto");
        String c = MyOptions.fsa_home + "/" +
            "fsa -r union(file('"+arg1File+"'),file('"+arg2File+"'))";
        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;
    }

    public FSAAutomaton intersect(FSAAutomaton auto) {
        String arg1File = this.toFile("temp1.auto");
        String arg2File = auto.toFile("temp2.auto");
        String c = MyOptions.fsa_home + "/" +
            "fsa -r intersect(file('"+arg1File+"'),file('"+arg2File+"'))";
        String autoString = Utils.exec(c);
        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;
    }

    // write this automaton to a file with the given name,
    // and returns the absolute file name
    String toFile(String name) {
        String fileName = MyOptions.graphPath + "/" + name;
        Utils.writeToFile(this.str, fileName);
        return fileName;
    }

    public String toDot() {
        String fileName = MyOptions.graphPath + "/temp.auto";
        Utils.writeToFile(this.str, fileName);
        String c = MyOptions.fsa_home + "/" + "fsa write=dot -r file('" + fileName + "')";
        String dot = Utils.exec(c);
        return dot;
    }

    // returns a corresponding recognizer for this transducer
    // by projecting to the output side (i.e., the output labels of the
    // transitions are used as recognizer labels)
    public FSAAutomaton projectOut() {

        String fileName = MyOptions.graphPath + "/temp.auto";
        Utils.writeToFile(this.str, fileName);
        String c = MyOptions.fsa_home + "/" + "fsa -r range(file('" + fileName + "'))";
        String projected = Utils.exec(c);

        return new FSAAutomaton(projected);
    }

    // returns the character in unicode form, except if the character
    // is a lowercase character;
    // borrowed from the brics automata package
    static String encode(char c) {

        StringBuilder b = new StringBuilder();
        if (Character.isLetter(c) && Character.isLowerCase(c)) {
            b.append(c);
        } else {
            b.append("u");
            String s = Integer.toHexString(c);
            if (c < 0x10)
                b.append("000").append(s);
            else if (c < 0x100)
                b.append("00").append(s);
            else if (c < 0x1000)
                b.append("0").append(s);
            else
                b.append(s);
        }
        return b.toString();
    }

    // reverses the transformation that is performed in encode()
    static char decode(String s) {
        if (s.length() == 1) {
            return s.charAt(0);
        }
        if (!s.startsWith("u")) {
            throw new RuntimeException("SNH");
        }
        if (!(s.length() == 5)) {
            throw new RuntimeException("SNH");
        }
        String hexString = s.substring(1,5);
        String dec = new java.math.BigInteger(hexString, 16).toString();
        return (char) Integer.valueOf(dec).intValue();
    }

    // helper function that returns a string if this automaton represents
    // exactly this one string; null otherwise
    List<String> getFiniteString() {

        List<String> retMe = new LinkedList<String>();

        AutoInfo info = this.parseAutomaton();
        if (info.startStates.size() != 1) {
            return null;
        }
        if (info.finalStates.size() != 1) {
            return null;
        }

        Integer currentState = info.startStates.iterator().next();
        while (currentState != null) {
            Set<TransitionInfo> tt = info.transitions.get(currentState);
            if (tt == null || tt.size() == 0) {
                // no outgoing transitions

                if (info.finalStates.contains(currentState)) {
                    // done!
                    currentState = null;
                } else {
                    // dead end
                    return null;
                }
            } else if (tt.size() == 1) {
                // exactly one outgoing transition
                TransitionInfo t = tt.iterator().next();
                retMe.add(t.label);
                currentState = t.dest;
            } else {
                // more than one outgoing transitions
                return null;
            }

        }

        return retMe;
    }

    // returns an automaton for the language of undesired strings for sql analysis (test);
    // MISSING HERE: double quotes and other evil stuff (see PHP's addslashes())
    public static FSAAutomaton getUndesiredSQLTest() {
        // regexp that matches every string that contains an
        // unescaped single quote at the beginning
        String regexpNoPrefix = "[" +

            // zero or even number of backslashes, followed by a quote
            "kleene_star(concat("+backslash+","+backslash+"))" + "," +
            single_quote + "," +

            // an arbitrary suffix
            "kleene_star(?)" +

            "]";

        // the same as before, but with an arbitrary prefix
        String regexpWithPrefix = "[" +

            // an arbitrary prefix
            "kleene_star(?)" + "," +

            // something other than a backslash
            "term_complement("+backslash+")" + "," +

            regexpNoPrefix +

            "]";

        // the complete regexp: union of the previous two
        String regexp = "[union("+regexpNoPrefix+","+regexpWithPrefix+")]";

        String c = MyOptions.fsa_home + "/" + "fsa -r " + regexp;

        String autoString = Utils.exec(c);

        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;

    }

    // returns an automaton for the language of undesired strings for xss analysis (test);
    public static FSAAutomaton getUndesiredXSSTest() {

        if (MyOptions.fsa_home == null) {
            Utils.bail("Please set fsaHome in the main configuration file.");
        }

        // regexp that matches every string that contains a pointy bracket
        String regexp = "[" +

        // an arbitrary prefix
        "kleene_star(?)" + "," +

        // the pointy
        opointy + "," +

        // an arbitrary suffix
        "kleene_star(?)" +

        "]";

        String c = MyOptions.fsa_home + "/" + "fsa -r " + regexp;

        String autoString = Utils.exec(c);

        FSAAutomaton retMe = new FSAAutomaton(autoString);

        return retMe;

    }

    private AutoInfo parseAutomaton() {

        // "region codes" used during parsing
        final int outside = 0;
        final int inStartStates = 1;
        final int inFinalStates = 2;
        final int inTransitions = 3;
        final int inJumps = 4;
        final int inSigma= 5;
        // starting outside
        int region = outside;

        // info to be collected
        Integer numStates = null;
        String sigma = null;
        List<Integer> startStates = new LinkedList<Integer>();
        List<Integer> finalStates = new LinkedList<Integer>();
        Map<Integer,Set<TransitionInfo>> transitions = new HashMap<Integer,Set<TransitionInfo>>();

        StringTokenizer tokenizer = new StringTokenizer(this.str, "\n");
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();

            if (line.contains("number of states")) {
                numStates = Integer.parseInt(line.substring(0, line.indexOf(',')));

            // relevant region change
            } else if (line.contains("begin sigma and symbols")) {
                region = inSigma;
            } else if (line.contains("end sigma and symbols")) {
                region = outside;
            } else if (line.contains("begin start states")) {
                region = inStartStates;
            } else if (line.contains("end start states")) {
                region = outside;
            } else if (line.contains("begin final states")) {
                region = inFinalStates;
            } else if (line.contains("end final states")) {
                region = outside;
            } else if (line.contains("begin transitions")) {
                region = inTransitions;
            } else if (line.contains("end transitions")) {
                region = outside;
            } else if (line.contains("end transitions")) {
                region = outside;
            } else {

                // no region change on this line

                switch (region) {
                case outside:
                    // nothing to do
                    break;
                case inStartStates:
                    line = line.trim();
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length()-1);
                    }
                    Integer startState = Integer.parseInt(line);
                    startStates.add(startState);
                    break;
                case inFinalStates:
                    line = line.trim();
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length()-1);
                    }
                    Integer finalState = Integer.parseInt(line);
                    finalStates.add(finalState);
                    break;
                case inTransitions:
                    // content: startState,LABEL,endState
                    String content = line.substring(6, line.lastIndexOf(')'));
                    Integer sourceState = Integer.parseInt(content.substring(0,content.indexOf(',')));
                    Integer destState = Integer.parseInt(content.substring(content.lastIndexOf(',')+1));

                    String label = content.substring(content.indexOf(',')+1, content.lastIndexOf(','));

                    Set<TransitionInfo> tt = transitions.get(sourceState);
                    if (tt == null) {
                        tt = new HashSet<TransitionInfo>();
                        transitions.put(sourceState, tt);
                    }
                    tt.add(new TransitionInfo(label, destState));
                    break;

                case inSigma:
                    if (sigma != null) {
                        System.out.println(this.str);
                        throw new RuntimeException("SNH");
                    }
                    sigma = line.trim();
                    break;
                case inJumps:
                    if (true) throw new RuntimeException("not yet");
                    break;
                }
            }
        }

        if (numStates == null) {
            throw new RuntimeException("SNH");
        }

        return new AutoInfo(sigma, startStates, finalStates, transitions, numStates);

    }

    // returns true if this automaton has only one state and
    // no transitions
    public boolean isEmpty() {
        AutoInfo info = this.parseAutomaton();
        if (info.numStates == 1 && info.transitions.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // converts a PHP regex (perl-compatible or posix extended) into an FSAAutomaton;
    // returns .* for unsupported regexes
    public static FSAAutomaton convertPhpRegex(List<String> phpRegexOrig, boolean preg)
    throws UnsupportedRegexException {

        FSAAutomaton retMe;

        // may throw an exception
        String prologRegex = Regex2Prolog.convertPhpRegex(phpRegexOrig, preg);

        String c = MyOptions.fsa_home + "/" + "fsa -r " + prologRegex;
        String autoString = Utils.exec(c);
        retMe = new FSAAutomaton(autoString);

        return retMe;

    }

    // helper class for exchanging automaton information;
    // note: doesn't consider jumps yet, so use with caution
    private class AutoInfo {

        String sigma;
        List<Integer> startStates;
        List<Integer> finalStates;
        Map<Integer,Set<TransitionInfo>> transitions;
        int numStates;

        AutoInfo(String sigma, List<Integer> ss, List<Integer> fs, Map<Integer,Set<TransitionInfo>> t, int n) {
            this.sigma = sigma;
            this.startStates = ss;
            this.finalStates = fs;
            this.transitions = t;
            this.numStates = n;
        }

    }

    private class TransitionInfo {
        String label;
        Integer dest;

        TransitionInfo(String label, Integer dest) {
            this.label = label;
            this.dest = dest;
        }
    }
}
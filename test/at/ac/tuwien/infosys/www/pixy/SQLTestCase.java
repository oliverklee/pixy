package at.ac.tuwien.infosys.www.pixy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.automaton.Automaton;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;

// Eclipse hint:
// all methods named "testXX" are executed automatically when choosing
// "Run / Run as... / JUnit Test"

public class SQLTestCase
    extends TestCase {

    private String path;    // complete path to the testfile directory (with trailing slash)

    // these are recomputed for every single test
    private DepAnalysis depAnalysis;
    private SQLAnalysis sqlAnalysis;
    List<Sink> sinks;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************

    // called automatically
    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/sql/";
    }

    // call this at the beginning of each test; optionally uses
    // a functional analysis instead of call-string ("functional" param),
    // and uses a dummy literal analysis
    private void mySetUp(String testFile, boolean functional) {

        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_A = true;
        MyOptions.setAnalyses("sql");

        // initialize & analyze
        TacConverter tac = checker.initialize().getTac();
        checker.analyzeTaint(tac, functional);
        this.depAnalysis = checker.gta.depAnalysis;
        this.sqlAnalysis = (SQLAnalysis) checker.gta.getDepClients().get(0);

        // collect sinks
        this.sinks = this.sqlAnalysis.collectSinks();
        Collections.sort(sinks);
    }

    // returns the contents of the given file as string
    private String readFile(String fileName) {
        StringBuilder ret = new StringBuilder();
        try {
            FileReader fr = new FileReader(fileName);
            int c;
            ret = new StringBuilder();
            while ((c = fr.read()) != -1) {
                ret.append((char) c);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("File not found: " + fileName);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return ret.toString();
    }

    // set "generate" to false if you want to generate graphs
    // (instead of checking against existing graphs)
    private void performTest(String testNum, int sinkNum, int graphNum, boolean generate) {
        performTest(testNum, sinkNum, graphNum, generate, false);
    }

    private void performTest(String testNum, int sinkNum, int graphNum, boolean generate, boolean functional) {

        mySetUp("test" + testNum + ".php", functional);

        Assert.assertTrue("Sinks real: " + sinks.size() + ", expected: "
            + sinkNum, sinks.size() == sinkNum);

        // collect depGraphs
        List<DepGraph> depGraphs = new LinkedList<DepGraph>();
        for (Sink sink : sinks) {
            depGraphs.addAll(depAnalysis.getDepGraph(sink));
        }

        Assert.assertTrue("Graphs real: " + depGraphs.size() + ", expected: "
            + graphNum, depGraphs.size() == graphNum);

        int graphCount = 0;
        for (DepGraph depGraph : depGraphs) {
            graphCount++;

            DepGraph sqlGraph = new DepGraph(depGraph);
            Automaton auto = this.sqlAnalysis.toAutomaton(sqlGraph, depGraph);

            String fileName = "test" + testNum + "_" + graphCount;
            if (generate) {

                this.sqlAnalysis.dumpDotAutoUnique(auto, fileName, this.path);

            } else {
                String encountered = auto.toDotUnique();
                String expected = this.readFile(this.path + fileName + ".dot");
                Assert.assertEquals(expected, encountered);
            }
        }

        if (generate) {
            // just to make sure that you don't accidentally forget
            // to switch generation off, and turn checking on
            Assert.fail("no check performed");
        }
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    public void test01() {
        String testNum = "01";
        int sinkNum = 3;        // expected number of sinks
        int graphNum = 3;       // expected number of graphs
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test02() {
        String testNum = "02";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test03() {
        String testNum = "03";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test04() {
        String testNum = "04";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test05() {
        String testNum = "05";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test06() {
        String testNum = "06";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test07() {
        String testNum = "07";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test08() {
        String testNum = "08";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test09() {
        String testNum = "09";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test10() {
        String testNum = "10";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test11() {
        String testNum = "11";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test12() {
        String testNum = "12";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test13() {
        String testNum = "13";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test14() {
        String testNum = "14";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test15() {
        String testNum = "15";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test16() {
        String testNum = "16";
        int sinkNum = 3;
        int graphNum = 3;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test17() {
        String testNum = "17";
        int sinkNum = 2;
        int graphNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test18() {
        String testNum = "18";
        int sinkNum = 5;
        int graphNum = 5;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test19() {
        String testNum = "19";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test20() {
        String testNum = "20";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test21() {
        String testNum = "21";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test22() {
        String testNum = "22";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test23() {
        String testNum = "23";
        int sinkNum = 3;
        int graphNum = 3;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test25() {
        String testNum = "25";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test26() {
        String testNum = "26";
        int sinkNum = 1;
        int graphNum = 1;
        // LATER: the generated dot file for this example is not
        // always the same (limitation in the current dot conversion
        // algorithm)
        if (false)
            this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test27() {
        String testNum = "27";
        int sinkNum = 2;
        int graphNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test28() {
        String testNum = "28";
        int sinkNum = 2;
        int graphNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test29() {
        String testNum = "29";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test30() {
        String testNum = "30";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test31() {
        String testNum = "31";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test32() {
        String testNum = "32";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test33() {
        String testNum = "33";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test34() {
        String testNum = "34";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test35() {
        String testNum = "35";
        int sinkNum = 0;
        int graphNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test36() {
        String testNum = "36";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test37() {
        String testNum = "37";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test38() {
        String testNum = "38";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test39() {
        String testNum = "39";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    public void test40() {
        String testNum = "40";
        int sinkNum = 1;
        int graphNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false);
    }

    /*
     * HOW TO ADD NEW TESTS
     *
     * - write a php testfile and move it to the right directory (see above)
     * - copy one of the existing test methods and adjust the numbers
     *   (for an explanation, see the first test method)
     * - set the fourth parameter of "performTest" to true, and run
     *   the test; this has the effect that dot files for the generated
     *   graphs are dumped to the filesystem
     * - check if the dot files look as you expected
     * - switch the fourth parameter back to false
     *
     */
}
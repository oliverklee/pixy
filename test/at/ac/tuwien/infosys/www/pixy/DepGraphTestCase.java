package at.ac.tuwien.infosys.www.pixy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraphUninitNode;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.Sink;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;

// Eclipse hint:
// all methods named "testXX" are executed automatically when choosing
// "Run / Run as... / JUnit Test"

public class DepGraphTestCase
extends TestCase {

    private String path;    // complete path to the testfile directory (with trailing slash)

    // these are recomputed for every single test
    private DepAnalysis depAnalysis;
    private XSSAnalysis xssAnalysis;
    List<Sink> sinks;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************

    // called automatically
    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/depgraph/";
    }


    // call this at the beginning of each test; optionally uses
    // a functional analysis instead of call-string ("functional" param),
    // and uses a dummy literal analysis
    private void mySetUp(String testFile, boolean functional) {

        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_A = true;    // perform alias analysis!
        MyOptions.setAnalyses("xss");

        // initialize & analyze
        TacConverter tac = checker.initialize().getTac();
        checker.analyzeTaint(tac, functional);
        this.depAnalysis = checker.gta.depAnalysis;
        this.xssAnalysis = (XSSAnalysis) checker.gta.getDepClients().get(0);

        // collect sinks
        this.sinks = this.xssAnalysis.collectSinks();
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
    private void performTest(String testNum, int sinkNum, int graphNum,
            boolean generate, int vulnNum) {

        performTest(testNum, sinkNum, graphNum, generate, false, vulnNum);
    }

    private void performTest(String testNum, int sinkNum, int graphNum,
            boolean generate, boolean functional, int vulnNum) {

        //generate = true;

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
        int vulnCount = 0;
        for (DepGraph depGraph : depGraphs) {

            // check depgraph

            graphCount++;
            String depFileName = "test" + testNum + "_" + graphCount;
            if (generate) {
                depGraph.dumpDotUnique(depFileName, this.path);
            } else {
                String encountered = depGraph.makeDotUnique(depFileName);
                String expected = this.readFile(this.path + depFileName + ".dot");
                Assert.assertEquals(expected, encountered);
            }

            // check xssgraph

            String xssFileName = "test" + testNum + "_" + graphCount + "_xss";
            DepGraph relevant = this.xssAnalysis.getRelevant(depGraph);
            Map<DepGraphUninitNode, DepClient.InitialTaint> dangerousUninit = this.xssAnalysis.findDangerousUninit(relevant);
            if (!dangerousUninit.isEmpty()) {
                vulnCount++;
                relevant.reduceWithLeaves(dangerousUninit.keySet());

                if (generate) {
                    relevant.dumpDotUnique(xssFileName, this.path);
                } else {
                    String encountered = relevant.makeDotUnique(xssFileName);
                    String expected = this.readFile(this.path + xssFileName + ".dot");
                    Assert.assertEquals(expected, encountered);
                }
            }
        }

        // check if all vulns were detected
        Assert.assertEquals(vulnNum, vulnCount);

        if (generate) {
            // just to make sure that you don't accidentally forget
            // to switch generation off, and turn checking on
            Assert.fail("no check performed");
        }
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    public void test001() {
        String testNum = "001";
        int sinkNum = 2;        // expected number of sinks
        int graphNum = 2;       // expected number of graphs
        int vulnNum = 2;        // expected number of vulnerabilities
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test002() {
        String testNum = "002";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test003() {
        String testNum = "003";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test004() {
        String testNum = "004";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test005() {
        String testNum = "005";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test006() {
        String testNum = "006";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test007() {
        String testNum = "007";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test008() {
        String testNum = "008";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test009() {
        String testNum = "009";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test010() {
        String testNum = "010";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test011() {
        String testNum = "011";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test012() {
        String testNum = "012";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test013() {
        String testNum = "013";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test014() {
        String testNum = "014";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test015() {
        String testNum = "015";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test016() {
        String testNum = "016";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test017() {
        String testNum = "017";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test018() {
        String testNum = "018";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test019() {
        String testNum = "019";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test020() {
        String testNum = "020";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test021() {
        String testNum = "021";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test022() {
        String testNum = "022";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test023() {
        String testNum = "023";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test024() {
        String testNum = "024";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test025() {
        String testNum = "025";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test026() {
        String testNum = "026";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test027() {
        String testNum = "027";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test028() {
        String testNum = "028";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test029() {
        String testNum = "029";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test030() {
        String testNum = "030";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test031() {
        String testNum = "031";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test032() {
        String testNum = "032";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test033() {
        String testNum = "033";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test034() {
        String testNum = "034";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test035() {
        String testNum = "035";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test036() {
        String testNum = "036";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test037() {
        String testNum = "037";
        int sinkNum = 1;
        int graphNum = 3;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test038() {
        String testNum = "038";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test039() {
        String testNum = "039";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test040() {
        String testNum = "040";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test041() {
        String testNum = "041";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test042() {
        String testNum = "042";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 0;
        // use functional analysis here
        this.performTest(testNum, sinkNum, graphNum, false, true, vulnNum);
    }

    public void test043() {
        String testNum = "043";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test044() {
        String testNum = "044";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test045() {
        String testNum = "045";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test046() {
        String testNum = "046";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test047() {
        String testNum = "047";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test048() {
        String testNum = "048";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test049() {
        String testNum = "049";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test050() {
        String testNum = "050";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test051() {
        String testNum = "051";
        int sinkNum = 2;
        int graphNum = 2;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test052() {
        String testNum = "052";
        int sinkNum = 1;
        int graphNum = 0;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test053() {
        String testNum = "053";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test054() {
        String testNum = "054";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test055() {
        String testNum = "055";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test056() {
        String testNum = "056";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test057() {
        String testNum = "057";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test058() {
        String testNum = "058";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test059() {
        String testNum = "059";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test060() {
        String testNum = "060";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test061() {
        String testNum = "061";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test062() {
        String testNum = "062";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test063() {
        String testNum = "063";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test064() {
        String testNum = "064";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test065() {
        String testNum = "065";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test066() {
        String testNum = "066";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test067() {
        String testNum = "067";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test068() {
        String testNum = "068";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test069() {
        String testNum = "069";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test070() {
        String testNum = "070";
        int sinkNum = 3;
        int graphNum = 3;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test071() {
        String testNum = "071";
        int sinkNum = 3;
        int graphNum = 3;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test072() {
        String testNum = "072";
        int sinkNum = 3;
        int graphNum = 3;
        int vulnNum = 2;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test073() {
        String testNum = "073";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test074() {
        String testNum = "074";
        int sinkNum = 1;
        int graphNum = 0;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test075() {
        String testNum = "075";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test076() {
        String testNum = "076";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test087() {
        String testNum = "087";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test089() {
        String testNum = "089";
        int sinkNum = 1;
        int graphNum = 1;
        int vulnNum = 0;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
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
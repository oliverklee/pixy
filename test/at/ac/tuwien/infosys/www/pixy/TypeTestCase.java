package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.Sink;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.DependencyGraph;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.graph.UninitializedNode;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test case for TypeAnalysis.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TypeTestCase extends TestCase {
    private String path;    // complete path to the testfile directory (with trailing slash)

    // these are recomputed for every single test
    private DependencyAnalysis dependencyAnalysis;
    private XssAnalysis xssAnalysis;
    List<Sink> sinks;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************

    // called automatically
    protected void setUp() {
        this.path = MyOptions.pixyHome + "/testfiles/type/";
        MyOptions.graphPath = MyOptions.pixyHome + "/graphs";
    }

    // call this at the beginning of each test; optionally uses
    // a functional analysis instead of call-string ("functional" param),
    // and uses a dummy literal analysis
    private void mySetUp(String testFile, boolean functional) {

        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_A = false;   // don't perform alias analysis
        MyOptions.setAnalyses("xss");

        // initialize & analyze
        TacConverter tac = checker.initialize().getTac();
        checker.analyzeTaint(tac, functional);
        this.dependencyAnalysis = checker.gta.dependencyAnalysis;
        this.xssAnalysis = (XssAnalysis) checker.gta.getAbstractVulnerabilityAnalyses().get(0);

        // collect sinks
        this.sinks = xssAnalysis.collectSinks();
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
        mySetUp("test" + testNum + ".php", functional);

        Assert.assertTrue("Sinks real: " + sinks.size() + ", expected: "
            + sinkNum, sinks.size() == sinkNum);

        // collect dependencyGraphs
        List<DependencyGraph> dependencyGraphs = new LinkedList<>();
        for (Sink sink : sinks) {
            dependencyGraphs.addAll(dependencyAnalysis.getDepGraph(sink));
        }

        Assert.assertTrue("Graphs real: " + dependencyGraphs.size() + ", expected: "
            + graphNum, dependencyGraphs.size() == graphNum);

        int graphCount = 0;
        int vulnCount = 0;
        for (DependencyGraph dependencyGraph : dependencyGraphs) {

            // check depgraph

            graphCount++;
            String fileName = "test" + testNum + "_" + graphCount;
            if (generate) {
                dependencyGraph.dumpDotUnique(fileName, this.path);
            } else {
                String encountered = dependencyGraph.makeDotUnique(fileName);
                String expected = this.readFile(this.path + fileName + ".dot");
                Assert.assertEquals(expected, encountered);
            }

            // check xssgraph

            String xssFileName = "test" + testNum + "_" + graphCount + "_xss";
            DependencyGraph relevant = this.xssAnalysis.getRelevant(dependencyGraph);
            Map<UninitializedNode, AbstractVulnerabilityAnalysis.InitialTaint> dangerousUninit = this.xssAnalysis.findDangerousUninitialized(relevant);
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
        int graphNum = 1;       // expected number of graphs
        int vulnNum = 1;        // expected number of vulnerabilities
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test002() {
        String testNum = "002";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test003() {
        String testNum = "003";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test004() {
        String testNum = "004";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test005() {
        String testNum = "005";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test006() {
        String testNum = "006";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test007() {
        String testNum = "007";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
        this.performTest(testNum, sinkNum, graphNum, false, vulnNum);
    }

    public void test008() {
        String testNum = "008";
        int sinkNum = 2;
        int graphNum = 1;
        int vulnNum = 1;
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
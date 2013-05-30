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
 * Resembles DepGraphTestCase, but does not perform alias analysis (see initialize()) and tests object-oriented stuff.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepGraphTestCaseNA extends TestCase {
    /** complete path to the test file directory (with trailing slash) */
    private String testFilesPathWithTrailingSlash;

    // these are recomputed for every single test
    private DependencyAnalysis dependencyAnalysis;
    private XssAnalysis xssAnalysis;
    List<Sink> sinks;

    private boolean generateGraphs = false;

    protected void setUp() {
        this.testFilesPathWithTrailingSlash = MyOptions.pixyHome + "/testfiles/depgraph/";
        MyOptions.graphPath = MyOptions.pixyHome + "/graphs";
    }

    /**
     * Call this at the beginning of each test.
     *
     * Optionally uses a functional analysis instead of call-string ("functional" param), and uses
     * a dummy literal analysis.
     *
     * @param testFile
     * @param useFunctionalAnalysis whether to use functional analysis instead of call-string analysis
     */
    private void initialize(String testFile, boolean useFunctionalAnalysis) {
        Checker checker = new Checker(this.testFilesPathWithTrailingSlash + testFile);
        // Don't perform alias analysis.
        MyOptions.option_A = false;
        MyOptions.setAnalyses("xss");

        // initialize & analyze
        TacConverter tac = checker.initialize().getTac();
        checker.analyzeTaint(tac, useFunctionalAnalysis);
        this.dependencyAnalysis = checker.gta.dependencyAnalysis;
        this.xssAnalysis = (XssAnalysis) checker.gta.getAbstractVulnerabilityAnalyses().get(0);

        this.sinks = xssAnalysis.collectSinks();
        Collections.sort(sinks);
    }

    /**
     * Returns the contents of the given file as string.
     *
     * @param fileName
     *
     * @return
     */
    private String readFile(String fileName) {
        StringBuilder fileContents = new StringBuilder();
        try {
            FileReader fileReader = new FileReader(fileName);
            int character;
            fileContents = new StringBuilder();
            while ((character = fileReader.read()) != -1) {
                fileContents.append((char) character);
            }
        } catch (FileNotFoundException e) {
            Assert.fail("File not found: " + fileName);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return fileContents.toString();
    }

    /**
     * @param testNumber
     * @param expectedNumberOfSinks
     * @param expectedNumberOfGraphs
     * @param generateGraphs set to true if you want to generate graphs (instead of checking against existing graphs)
     * @param expectedNumberOfVulnerabilities
     */
    private void performTestWithCallStringAnalysis(
        String testNumber, int expectedNumberOfSinks, int expectedNumberOfGraphs, boolean generateGraphs,
        int expectedNumberOfVulnerabilities
    ) {
        this.generateGraphs = generateGraphs;

        performTest(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    /**
     * @param testNumber
     * @param expectedNumberOfSinks
     * @param expectedNumberOfGraphs
     * @param generateGraphs set to true if you want to generate graphs (instead of checking against existing graphs)
     * @param expectedNumberOfVulnerabilities
     */
    private void performTestWithFunctionalAnalysis(
        String testNumber, int expectedNumberOfSinks, int expectedNumberOfGraphs, boolean generateGraphs,
        int expectedNumberOfVulnerabilities
    ) {
        this.generateGraphs = generateGraphs;

        performTest(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, true, expectedNumberOfVulnerabilities);
    }

    private void performTest(
        String testNumber, int expectedNumberOfSinks, int expectedNumberOfGraphs, boolean useFunctionalAnalysis,
        int expectedNumberOfVulnerabilities
    ) {
        initialize("test" + testNumber + ".php", useFunctionalAnalysis);

        Assert.assertTrue(
            "Sinks real: " + sinks.size() + ", expected: " + expectedNumberOfSinks,
            sinks.size() == expectedNumberOfSinks
        );

        List<DependencyGraph> dependencyGraphs = collectDependencyGraphs();
        Assert.assertTrue(
            "Graphs real: " + dependencyGraphs.size() + ", expected: " + expectedNumberOfGraphs,
            dependencyGraphs.size() == expectedNumberOfGraphs
        );

        int graphNumber = 0;
        int actualNumberOfVulnerabilities = 0;

        for (DependencyGraph dependencyGraph : dependencyGraphs) {
            graphNumber++;
            checkDependencyGraph(testNumber, dependencyGraph, graphNumber);

            if (checkXssGraph(testNumber, dependencyGraph, graphNumber)) {
                actualNumberOfVulnerabilities++;
            }
        }

        Assert.assertEquals(expectedNumberOfVulnerabilities, actualNumberOfVulnerabilities);

        if (generateGraphs) {
            // just to make sure that you don't accidentally forget
            // to switch generation off, and turn checking on
            Assert.fail("no check performed");
        }
    }

    private List<DependencyGraph> collectDependencyGraphs() {
        List<DependencyGraph> dependencyGraphs = new LinkedList<>();
        for (Sink sink : sinks) {
            dependencyGraphs.addAll(dependencyAnalysis.getDependencyGraphsForSink(sink));
        }

        return dependencyGraphs;
    }

    private void checkDependencyGraph(String testNumber, DependencyGraph dependencyGraph, int graphNumber) {
        String fileName = "test" + testNumber + "_" + graphNumber;
        if (generateGraphs) {
            dependencyGraph.dumpDotUnique(fileName, this.testFilesPathWithTrailingSlash);
        } else {
            String encountered = dependencyGraph.makeDotUnique(fileName);
            String expected = this.readFile(this.testFilesPathWithTrailingSlash + fileName + ".dot");
            Assert.assertEquals(expected, encountered);
        }
    }

    /**
     * Checks the XSS graph.
     *
     *
     * @param testNumber
     * @param dependencyGraph
     * @param actualNumberOfGraphs
     *
     * @return true if a vulnerability has been detected, false otherwise
     */
    private boolean checkXssGraph(
        String testNumber, DependencyGraph dependencyGraph, int actualNumberOfGraphs
    ) {
        String xssFileName = "test" + testNumber + "_" + actualNumberOfGraphs + "_xss";
        DependencyGraph relevant = this.xssAnalysis.getRelevantSubgraph(dependencyGraph);
        Map<UninitializedNode, AbstractVulnerabilityAnalysis.InitialTaint> dangerousUninitializedNodes
            = this.xssAnalysis.findDangerousUninitializedNodes(relevant);

        boolean hasVulnerability = !dangerousUninitializedNodes.isEmpty();

        if (hasVulnerability) {
            relevant.reduceWithLeaves(dangerousUninitializedNodes.keySet());

            if (generateGraphs) {
                relevant.dumpDotUnique(xssFileName, this.testFilesPathWithTrailingSlash);
            } else {
                String encountered = relevant.makeDotUnique(xssFileName);
                String expected = this.readFile(this.testFilesPathWithTrailingSlash + xssFileName + ".dot");
                Assert.assertEquals(expected, encountered);
            }
        }

        return hasVulnerability;
    }
    public void test001() {
        String testNumber = "001";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test002() {
        String testNumber = "002";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test003() {
        String testNumber = "003";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test004() {
        String testNumber = "004";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test005() {
        String testNumber = "005";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test006() {
        String testNumber = "006";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test007() {
        String testNumber = "007";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test008() {
        String testNumber = "008";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test009() {
        String testNumber = "009";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test010() {
        String testNumber = "010";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test011() {
        String testNumber = "011";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test012() {
        String testNumber = "012";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test013() {
        String testNumber = "013";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test014() {
        String testNumber = "014";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test015() {
        String testNumber = "015";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test016() {
        String testNumber = "016";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test017() {
        String testNumber = "017";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test018() {
        String testNumber = "018";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test021() {
        String testNumber = "021";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test022() {
        String testNumber = "022";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test023() {
        String testNumber = "023";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test024() {
        String testNumber = "024";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test025() {
        String testNumber = "025";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test026() {
        String testNumber = "026";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test027() {
        String testNumber = "027";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test028() {
        String testNumber = "028";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test035() {
        String testNumber = "035";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test036() {
        String testNumber = "036";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test037() {
        String testNumber = "037";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test038() {
        String testNumber = "038";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test039() {
        String testNumber = "039";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test040() {
        String testNumber = "040";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test041b() {
        String testNumber = "041b";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test042() {
        String testNumber = "042";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithFunctionalAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test043() {
        String testNumber = "043";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test044() {
        String testNumber = "044";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test045() {
        String testNumber = "045";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test046() {
        String testNumber = "046";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test047() {
        String testNumber = "047";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test048() {
        String testNumber = "048";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test049() {
        String testNumber = "049";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test050() {
        String testNumber = "050";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test051() {
        String testNumber = "051";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test052() {
        String testNumber = "052";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test053() {
        String testNumber = "053";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test054() {
        String testNumber = "054";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test055b() {
        String testNumber = "055b";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test056() {
        String testNumber = "056";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test057() {
        String testNumber = "057";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test058() {
        String testNumber = "058";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test059() {
        String testNumber = "059";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test060() {
        String testNumber = "060";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test061() {
        String testNumber = "061";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test062() {
        String testNumber = "062";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test063() {
        String testNumber = "063";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test064() {
        String testNumber = "064";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test065() {
        String testNumber = "065";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test066() {
        String testNumber = "066";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test067() {
        String testNumber = "067";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test068() {
        String testNumber = "068";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test069() {
        String testNumber = "069";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test070() {
        String testNumber = "070";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test071() {
        String testNumber = "071";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test072() {
        String testNumber = "072";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test073() {
        String testNumber = "073";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test074() {
        String testNumber = "074";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test075() {
        String testNumber = "075";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test076() {
        String testNumber = "076";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test077() {
        String testNumber = "077";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test078() {
        String testNumber = "078";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test079() {
        String testNumber = "079";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test080() {
        String testNumber = "080";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test081() {
        String testNumber = "081";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test082() {
        String testNumber = "082";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test083() {
        String testNumber = "083";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test084() {
        String testNumber = "084";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test085() {
        String testNumber = "085";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test086() {
        String testNumber = "086";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test087() {
        String testNumber = "087";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test088() {
        String testNumber = "088";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test089() {
        String testNumber = "089";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test090() {
        String testNumber = "090";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test091() {
        String testNumber = "091";
        int expectedNumberOfSinks = 10;
        int expectedNumberOfGraphs = 10;
        int expectedNumberOfVulnerabilities = 7;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test092() {
        String testNumber = "092";
        int expectedNumberOfSinks = 4;
        int expectedNumberOfGraphs = 4;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test093() {
        String testNumber = "093";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test094() {
        String testNumber = "094";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test095() {
        String testNumber = "095";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test096() {
        String testNumber = "096";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test097() {
        String testNumber = "097";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test098() {
        String testNumber = "098";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test099() {
        String testNumber = "099";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test100() {
        String testNumber = "100";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test101() {
        String testNumber = "101";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test102() {
        String testNumber = "102";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test103() {
        String testNumber = "103";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test104() {
        String testNumber = "104";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test105() {
        String testNumber = "105";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test106() {
        String testNumber = "106";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test107() {
        String testNumber = "107";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test108() {
        String testNumber = "108";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test109() {
        String testNumber = "109";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test110() {
        String testNumber = "110";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test111() {
        String testNumber = "111";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test112() {
        String testNumber = "112";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test113() {
        String testNumber = "113";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test114() {
        String testNumber = "114";
        int expectedNumberOfSinks = 4;
        int expectedNumberOfGraphs = 4;
        int expectedNumberOfVulnerabilities = 3;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test115() {
        String testNumber = "115";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test116() {
        String testNumber = "116";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test117() {
        String testNumber = "117";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test118() {
        String testNumber = "118";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test119() {
        String testNumber = "119";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test120() {
        String testNumber = "120";
        int expectedNumberOfSinks = 2;
        int expectedNumberOfGraphs = 2;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test121() {
        String testNumber = "121";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test122() {
        String testNumber = "122";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test123() {
        String testNumber = "123";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        // TODO: This test currently fails. Please see issue #1 for details.
        // this.performTest(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test124() {
        String testNumber = "124";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test125() {
        String testNumber = "125";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test126() {
        String testNumber = "126";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test127() {
        String testNumber = "127";
        int expectedNumberOfSinks = 0;
        int expectedNumberOfGraphs = 0;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test128() {
        String testNumber = "128";
        int expectedNumberOfSinks = 3;
        int expectedNumberOfGraphs = 3;
        int expectedNumberOfVulnerabilities = 3;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test129() {
        String testNumber = "129";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test130() {
        String testNumber = "130";
        int expectedNumberOfSinks = 5;
        int expectedNumberOfGraphs = 5;
        int expectedNumberOfVulnerabilities = 2;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test131() {
        String testNumber = "131";
        int expectedNumberOfSinks = 4;
        int expectedNumberOfGraphs = 4;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test132() {
        String testNumber = "132";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test133() {
        String testNumber = "133";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test134() {
        String testNumber = "134";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 1;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test135() {
        String testNumber = "135";
        int expectedNumberOfSinks = 1;
        int expectedNumberOfGraphs = 1;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
    }

    public void test136() {
        String testNumber = "136";
        int expectedNumberOfSinks = 4;
        int expectedNumberOfGraphs = 4;
        int expectedNumberOfVulnerabilities = 0;
        this.performTestWithCallStringAnalysis(testNumber, expectedNumberOfSinks, expectedNumberOfGraphs, false, expectedNumberOfVulnerabilities);
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
     */
}
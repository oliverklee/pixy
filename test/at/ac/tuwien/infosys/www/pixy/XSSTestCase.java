package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.List;

/**
 * Resembles DepGraphTestCase, but does not perform alias analysis (see mySetUp()) and tests object-oriented stuff.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class XSSTestCase extends TestCase {
    private String path;    // complete path to the testfile directory (with trailing slash)

    // recomputed for every single test
    private List<Integer> vulnList;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************

    // called automatically
    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/xss/";
        MyOptions.graphPath = MyOptions.pixy_home + "/graphs";
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
        this.vulnList = checker.gta.detectVulns();
    }

    private void performTest(String testNum, List<Integer> exp) {
        mySetUp("test" + testNum + ".php", false);
        Assert.assertEquals(exp, this.vulnList);
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    public void test01() {
        String testNum = "01";
        // list containing the line numbers for which we expect a vuln report
        List<Integer> exp = new LinkedList<>();
        exp.add(6);
        exp.add(8);
        this.performTest(testNum, exp);
    }

    public void test02() {
        String testNum = "02";
        List<Integer> exp = new LinkedList<>();
        exp.add(10);
        exp.add(11);
        exp.add(12);
        exp.add(14);
        this.performTest(testNum, exp);
    }

    public void test03() {
        String testNum = "03";
        List<Integer> exp = new LinkedList<>();
        exp.add(13);
        this.performTest(testNum, exp);
    }

    /*
     * HOW TO ADD NEW TESTS
     *
     * - write a php testfile and move it to the right directory (see above)
     * - copy one of the existing test methods and adjust the numbers
     *   (for an explanation, see the first test method)
     * - run the test
     *
     */
}
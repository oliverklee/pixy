package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.alias.*;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.Context;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.functional.FunctionalAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNodeHotspot;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.*;

/**
 * Doesn't use builtin functions file.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AliasTestCase extends TestCase {
    private String path;
    private TacConverter tac;
    private AliasAnalysis aliasAnalysis;
    private AliasLatticeElement[] elements;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************

    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/alias/";
        MyOptions.graphPath = MyOptions.pixy_home + "/graphs";
    }

    // call this at the beginning of each test
    private void mySetUp(String testFile, int numHotspots) {

        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_A = true;

        // initialize & analyze
        this.tac = checker.initialize().getTac();
        this.aliasAnalysis = checker.analyzeAliases(tac, false);

        // extract (folded) lattice elements from hotspots
        this.elements = new AliasLatticeElement[numHotspots];
        for (int i = 0; i < numHotspots; i++) {
            this.elements[i] = (AliasLatticeElement) this.getHotspotInfo(i).getUnrecycledFoldedValue();
            if (this.elements[i] == null) {
                CfgNodeHotspot hot = this.tac.getHotspot(i);
                System.out.println("thenode: " + hot.toString() + ", " + hot.getOrigLineno());
                System.out.println("enclosing: " + hot.getEnclosingBasicBlock());
                throw new RuntimeException("SNH");
            }
        }
    }

//  ********************************************************************************
//  HELPERS ************************************************************************
//  ********************************************************************************

    // returns the analysis node associated with the hotspot given by its ID
    private FunctionalAnalysisNode getHotspotInfo(int hotspotId) {

        CfgNodeHotspot hot = this.tac.getHotspot(hotspotId);
        if (hot == null) {
            Assert.fail("Tried to retrieve non-existent hotspot with ID " + hotspotId);
        }
        return (FunctionalAnalysisNode) this.aliasAnalysis.getAnalysisNode(hot);
    }

    // use this method for structureEquals assertions (prints debug info if
    // the assertion fails)
    private void myAssertTrue(AliasLatticeElement found, AliasLatticeElement expected) {

        if (!found.structureEquals(expected)) {
            System.out.println("found: ");
            Dumper.dump(found);
            System.out.println();
            System.out.println("expected: ");
            Dumper.dump(expected);
            System.out.println();

            Assert.fail();
        }
    }

    // checks if the context table at the given hotspot ID contains exactly the
    // lattice elements contained in expectedElements
    private void checkContext(List<AliasLatticeElement> expectedElements, int hotspotId) {
        HashMap<Context, LatticeElement> hotspotPhi = new HashMap<>(this.getHotspotInfo(hotspotId).getPhi());

        // for each expected element...
        for (AliasLatticeElement expected : expectedElements) {
            // if you find it in the real context map: OK and remove it from there
            boolean foundIt = false;
            for (Iterator<Map.Entry<Context, LatticeElement>> elementIter = hotspotPhi.entrySet().iterator(); elementIter.hasNext(); ) {
                Map.Entry<Context, LatticeElement> entry = elementIter.next();
                LatticeElement element = entry.getValue();
                if (element.structureEquals(expected)) {
                    foundIt = true;
                    elementIter.remove();
                    break;
                }
            }
            Assert.assertTrue(foundIt);
        }

        // the phi map should be empty now
        Assert.assertTrue(hotspotPhi.isEmpty());
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    // we had to move this test case up due to strange, indeterministic failures;
    // there must be a nasty implementation bug hidden somewhere that causes
    // some influence between separate tests; same for test23 of literal
    // analysis
    public void testDev25() {

        int numHotspots = 2;
        mySetUp("dev/test25.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varU = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$u");
        Variable varV = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$v");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varAU_gs = this.tac.getFuncVariable("a", "$u_gs");
        Variable varAV_gs = this.tac.getFuncVariable("a", "$v_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varAX2_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varU, varAU_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varV, varAV_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varA1));
        may.add(new MayAliasPair(varAX1_gs, varA1));
        may.add(new MayAliasPair(varX2, varA1));
        may.add(new MayAliasPair(varAX2_gs, varA1));
        expected = new AliasLatticeElement(must, may);
        myAssertTrue(elements[0], expected);
        // Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varAX2_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varU, varAU_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varV, varAV_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varA1));
        may.add(new MayAliasPair(varAX1_gs, varA1));
        may.add(new MayAliasPair(varX2, varA1));
        may.add(new MayAliasPair(varAX2_gs, varA1));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testDev01() {

        int numHotspots = 6;
        mySetUp("dev/test01.php", numHotspots);

        // all Phi maps must have exactly one context
        // no need to check this here

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");
        Variable varD = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$d");
        Variable varE = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$e");
        Variable varF = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$f");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may = new MayAliases();  // doesn't change here
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        must.add(tempGroup);
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        tempGroup.add(varC);
        must.add(tempGroup);
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));

        // this check makes sure that structureEquals doesn't always
        // return true
        tempGroup.add(varD);
        Assert.assertFalse(elements[2].structureEquals(expected));

        // 3
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        tempGroup.add(varC);
        tempGroup.add(varD);
        must.add(tempGroup);
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[3].structureEquals(expected));

        // 4
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        tempGroup.add(varD);
        tempGroup.add(varC);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varE, varF);
        must.add(tempGroup);
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[4].structureEquals(expected));

        // 5
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        tempGroup.add(varC);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varD, varE);
        tempGroup.add(varF);
        must.add(tempGroup);
        expected = new AliasLatticeElement(must, may);
        // Assert.assertTrue(elements[5].structureEquals(expected));
        this.myAssertTrue(elements[5], expected);
    }

    public void testDev02() {

        int numHotspots = 3;
        mySetUp("dev/test02.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varB));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testDev03() {

        int numHotspots = 3;
        mySetUp("dev/test03.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");
        Variable varD = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$d");
        Variable varE = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$e");
        Variable varF = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$f");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        tempGroup.add(varC);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varD, varE);
        tempGroup.add(varF);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varB, varC);
        tempGroup.add(varD);
        tempGroup.add(varE);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varB, varC);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varD, varE);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varB));
        may.add(new MayAliasPair(varA, varC));
        may.add(new MayAliasPair(varD, varF));
        may.add(new MayAliasPair(varE, varF));
        may.add(new MayAliasPair(varB, varD));
        may.add(new MayAliasPair(varB, varE));
        may.add(new MayAliasPair(varC, varD));
        may.add(new MayAliasPair(varC, varE));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testDev04() {

        int numHotspots = 3;
        mySetUp("dev/test04.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varAY_gs = this.tac.getFuncVariable("a", "$y_gs");
        Variable varBX_gs = this.tac.getFuncVariable("b", "$x_gs");
        Variable varBY_gs = this.tac.getFuncVariable("b", "$y_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        Variable varB1 = this.tac.getFuncVariable("b", "$b1");
        Variable varB2 = this.tac.getFuncVariable("b", "$b2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        if (elements[0] == null) {
            throw new RuntimeException("SNH");
        }
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        tempGroup.add(varAX_gs);
        tempGroup.add(varAY_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varA1, varA2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        tempGroup.add(varBX_gs);
        tempGroup.add(varBY_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varB1, varB2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testDev05() {

        int numHotspots = 3;
        mySetUp("dev/test05.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");
        Variable varU = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$u");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varAY_gs = this.tac.getFuncVariable("a", "$y_gs");
        Variable varAU_gs = this.tac.getFuncVariable("a", "$u_gs");
        Variable varBX_gs = this.tac.getFuncVariable("b", "$x_gs");
        Variable varBY_gs = this.tac.getFuncVariable("b", "$y_gs");
        Variable varBU_gs = this.tac.getFuncVariable("b", "$u_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        Variable varB1 = this.tac.getFuncVariable("b", "$b1");
        Variable varB2 = this.tac.getFuncVariable("b", "$b2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;
        // AliasLatticeElement real;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varY));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varU, varAU_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varY, varAY_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varY));
        may.add(new MayAliasPair(varY, varAX_gs));
        may.add(new MayAliasPair(varX, varAY_gs));
        may.add(new MayAliasPair(varAX_gs, varAY_gs));
        may.add(new MayAliasPair(varA1, varA2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varU, varBU_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX, varBX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varY, varBY_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varY));
        may.add(new MayAliasPair(varY, varBX_gs));
        may.add(new MayAliasPair(varX, varBY_gs));
        may.add(new MayAliasPair(varBX_gs, varBY_gs));
        may.add(new MayAliasPair(varB1, varB2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));

        // 2 (crosscheck)
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varU, varBU_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX, varBX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varY, varBY_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varY));
        may.add(new MayAliasPair(varY, varBX_gs));
        // may.add(new MayAliasPair(varX, varBY_gs));
        may.add(new MayAliasPair(varBX_gs, varBY_gs));
        may.add(new MayAliasPair(varB1, varB2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertFalse(elements[2].structureEquals(expected));
    }

    public void testDev06() {

        int numHotspots = 1;
        mySetUp("dev/test06.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");
        Variable varZ = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varZ, varX);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varY));
        may.add(new MayAliasPair(varZ, varY));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev07() {

        int numHotspots = 1;
        mySetUp("dev/test07.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev08() {

        int numHotspots = 2;
        mySetUp("dev/test08.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varBX_gs = this.tac.getFuncVariable("b", "$x_gs");
        Variable varBP = this.tac.getFuncVariable("b", "$bp");
        Variable varBP_fs = this.tac.getFuncVariable("b", "$bp_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varBX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP, varBP_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testDev09() {

        int numHotspots = 1;
        mySetUp("dev/test09.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        tempGroup.add(varBP2);
        tempGroup.add(varBP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev10() {

        int numHotspots = 1;
        mySetUp("dev/test10.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        tempGroup.add(varBP2);
        tempGroup.add(varBP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev11() {

        int numHotspots = 1;
        mySetUp("dev/test11.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP2, varBP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varBP1, varBP2));
        may.add(new MayAliasPair(varBP1, varBP2_fs));
        may.add(new MayAliasPair(varBP2, varBP1_fs));
        may.add(new MayAliasPair(varBP1_fs, varBP2_fs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev12() {

        int numHotspots = 2;
        mySetUp("dev/test12.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varBX_gs = this.tac.getFuncVariable("b", "$x_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varAX = this.tac.getFuncVariable("a", "$x");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        tempGroup.add(varAX);
        tempGroup.add(varA1);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varBX_gs);
        tempGroup.add(varBP1);
        tempGroup.add(varBP1_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testDev13() {

        int numHotspots = 4;
        mySetUp("dev/test13.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varBX_gs = this.tac.getFuncVariable("b", "$x_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varAX = this.tac.getFuncVariable("a", "$x");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varAX, varX));
        may.add(new MayAliasPair(varAX, varAX_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varAX, varA1);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varAX, varX));
        may.add(new MayAliasPair(varAX, varAX_gs));
        may.add(new MayAliasPair(varA1, varX));
        may.add(new MayAliasPair(varA1, varAX_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varAX, varA1);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varAX, varX));
        may.add(new MayAliasPair(varAX, varAX_gs));
        may.add(new MayAliasPair(varA1, varX));
        may.add(new MayAliasPair(varA1, varAX_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));

        // 3
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varBX_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX, varBP1));
        may.add(new MayAliasPair(varX, varBP1_fs));
        may.add(new MayAliasPair(varBP1, varBX_gs));
        may.add(new MayAliasPair(varBP1_fs, varBX_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[3].structureEquals(expected));
    }

    public void testDev14() {

        int numHotspots = 1;
        mySetUp("dev/test14.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev15() {

        int numHotspots = 1;
        mySetUp("dev/test15.php", numHotspots);

        // variables to be tested
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varAP1_fs = this.tac.getFuncVariable("a", "$ap1_fs");
        Variable varAP2 = this.tac.getFuncVariable("a", "$ap2");
        Variable varAP2_fs = this.tac.getFuncVariable("a", "$ap2_fs");

        // construct expected context lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;
        List<AliasLatticeElement> expectedList = new LinkedList<>();

        // first context
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varAP1, varAP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varAP2, varAP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        expectedList.add(expected);

        // second context
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varAP1, varAP1_fs);
        tempGroup.add(varAP2);
        tempGroup.add(varAP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        expectedList.add(expected);

        this.checkContext(expectedList, 0);
    }

    public void testDev16() {

        int numHotspots = 1;
        mySetUp("dev/test16.php", numHotspots);

        // variables to be tested
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varAP1_fs = this.tac.getFuncVariable("a", "$ap1_fs");
        Variable varAP2 = this.tac.getFuncVariable("a", "$ap2");
        Variable varAP2_fs = this.tac.getFuncVariable("a", "$ap2_fs");

        // construct expected context lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;
        List<AliasLatticeElement> expectedList = new LinkedList<>();

        // first context
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varAP1, varAP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varAP2, varAP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        expectedList.add(expected);

        this.checkContext(expectedList, 0);
    }

    public void testDev17() {

        int numHotspots = 1;
        mySetUp("dev/test17.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");
        Variable varAY_gs = this.tac.getFuncVariable("a", "$y_gs");
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varAP1_fs = this.tac.getFuncVariable("a", "$ap1_fs");
        Variable varAP2 = this.tac.getFuncVariable("a", "$ap2");
        Variable varAP2_fs = this.tac.getFuncVariable("a", "$ap2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varY, varAY_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX, varAP1);
        tempGroup.add(varAX_gs);
        tempGroup.add(varAP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varAP2, varAP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev18() {

        int numHotspots = 1;
        mySetUp("dev/test18.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varAX_gs = this.tac.getFuncVariable("a", "$x_gs");
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varAP1_fs = this.tac.getFuncVariable("a", "$ap1_fs");
        Variable varAP2 = this.tac.getFuncVariable("a", "$ap2");
        Variable varAP2_fs = this.tac.getFuncVariable("a", "$ap2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varAP1);
        tempGroup.add(varAP2);
        tempGroup.add(varAX_gs);
        tempGroup.add(varAP1_fs);
        tempGroup.add(varAP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev19() {

        int numHotspots = 2;
        mySetUp("dev/test19.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");
        Variable varZ = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varZ);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varY, varZ));
        may.add(new MayAliasPair(varY, varX));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varY, varX));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testDev20() {

        int numHotspots = 1;
        mySetUp("dev/test20.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev21() {

        int numHotspots = 3;
        mySetUp("dev/test21.php", numHotspots);

        // variables to be tested
        // <none>

        // construct expected lattice elements and check

        // init
        //MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testDev22() {

        int numHotspots = 3;
        mySetUp("dev/test21.php", numHotspots);

        // variables to be tested
        // <none>

        // construct expected lattice elements and check

        // init
        //MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testDev23() {

        int numHotspots = 1;
        mySetUp("dev/test23.php", numHotspots);

        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varY = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$y");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX, varY);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testDev24() {

        int numHotspots = 1;
        mySetUp("dev/test24.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

//  test files from the tutorial ***************************************************

    public void testTut01() {

        int numHotspots = 1;
        mySetUp("tutorial/test01.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");

        // construct expected lattice elements and check

        // init
        //MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varB));
        may.add(new MayAliasPair(varA, varC));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut02() {

        int numHotspots = 1;
        mySetUp("tutorial/test02.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varB, varC));
        may.add(new MayAliasPair(varA, varC));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut03() {

        int numHotspots = 1;
        mySetUp("tutorial/test03.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");
        Variable varD = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$d");
        Variable varE = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$e");
        Variable varF = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$f");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varB, varC);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varD, varE);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varB));
        may.add(new MayAliasPair(varA, varC));
        may.add(new MayAliasPair(varD, varF));
        may.add(new MayAliasPair(varE, varF));
        may.add(new MayAliasPair(varB, varD));
        may.add(new MayAliasPair(varB, varE));
        may.add(new MayAliasPair(varC, varD));
        may.add(new MayAliasPair(varC, varE));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut04() {

        int numHotspots = 4;
        mySetUp("tutorial/test04.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varX3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x3");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varAX3_gs = this.tac.getFuncVariable("a", "$x3_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varBX2_gs = this.tac.getFuncVariable("b", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varX3);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varA2);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX3, varAX3_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varAX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varA2);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varX3);
        tempGroup.add(varAX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));

        // 3
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varX3);
        tempGroup.add(varBX1_gs);
        tempGroup.add(varBX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[3].structureEquals(expected));
    }

    public void testTut05() {

        int numHotspots = 1;
        mySetUp("tutorial/test05.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP3 = this.tac.getFuncVariable("b", "$bp3");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");
        Variable varBP3_fs = this.tac.getFuncVariable("b", "$bp3_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP2);
        tempGroup.add(varBP1_fs);
        tempGroup.add(varBP2_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP3, varBP3_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut06() {

        int numHotspots = 1;
        mySetUp("tutorial/test06.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP3 = this.tac.getFuncVariable("b", "$bp3");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");
        Variable varBP3_fs = this.tac.getFuncVariable("b", "$bp3_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP2);
        tempGroup.add(varBP1_fs);
        tempGroup.add(varBP2_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP3, varBP3_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut07() {

        int numHotspots = 1;
        mySetUp("tutorial/test07.php", numHotspots);

        // variables to be tested
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");
        Variable varBP2_fs = this.tac.getFuncVariable("b", "$bp2_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP2, varBP2_fs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varBP1, varBP2));
        may.add(new MayAliasPair(varBP1, varBP2_fs));
        may.add(new MayAliasPair(varBP2, varBP1_fs));
        may.add(new MayAliasPair(varBP1_fs, varBP2_fs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut08() {

        int numHotspots = 2;
        mySetUp("tutorial/test08.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varX1);
        tempGroup.add(varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varBX1_gs);
        tempGroup.add(varBP1);
        tempGroup.add(varBP1_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut09() {

        int numHotspots = 1;
        mySetUp("tutorial/test09.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varBX1_gs);
        tempGroup.add(varBP1);
        tempGroup.add(varBP1_fs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut10() {

        int numHotspots = 2;
        mySetUp("tutorial/test10.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA1, varX1));
        may.add(new MayAliasPair(varA1, varAX1_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX1, varBX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varBP1));
        may.add(new MayAliasPair(varX1, varBP1_fs));
        may.add(new MayAliasPair(varBP1, varBX1_gs));
        may.add(new MayAliasPair(varBX1_gs, varBP1_fs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut11() {

        int numHotspots = 2;
        mySetUp("tutorial/test11.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varBX2_gs = this.tac.getFuncVariable("b", "$x2_gs");

        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP1_fs = this.tac.getFuncVariable("b", "$bp1_fs");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varAX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varA1));
        may.add(new MayAliasPair(varX2, varA1));
        may.add(new MayAliasPair(varAX1_gs, varA1));
        may.add(new MayAliasPair(varAX2_gs, varA1));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varBX1_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varBX2_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varBP1, varBP1_fs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varBP1, varX1));
        may.add(new MayAliasPair(varBP1, varX2));
        may.add(new MayAliasPair(varBP1, varBX1_gs));
        may.add(new MayAliasPair(varBP1, varBX2_gs));
        may.add(new MayAliasPair(varBP1_fs, varX1));
        may.add(new MayAliasPair(varBP1_fs, varX2));
        may.add(new MayAliasPair(varBP1_fs, varBX1_gs));
        may.add(new MayAliasPair(varBP1_fs, varBX2_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut12() {

        int numHotspots = 2;
        mySetUp("tutorial/test12.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varX1);
        tempGroup.add(varX2);
        tempGroup.add(varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut13() {

        int numHotspots = 3;
        mySetUp("tutorial/test13.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varBX2_gs = this.tac.getFuncVariable("b", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varX2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varX1);
        tempGroup.add(varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX2, varX1));
        may.add(new MayAliasPair(varX2, varA1));
        may.add(new MayAliasPair(varX2, varAX1_gs));
        may.add(new MayAliasPair(varX2, varAX2_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varBX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varX2));
        may.add(new MayAliasPair(varX2, varBX1_gs));
        may.add(new MayAliasPair(varX2, varBX2_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testTut14() {

        int numHotspots = 2;
        mySetUp("tutorial/test14.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA1, varX1));
        may.add(new MayAliasPair(varA1, varX2));
        may.add(new MayAliasPair(varA1, varAX1_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut15() {

        int numHotspots = 2;
        mySetUp("tutorial/test15.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varX2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX2, varX1));
        may.add(new MayAliasPair(varX2, varAX1_gs));
        may.add(new MayAliasPair(varX2, varAX2_gs));
        may.add(new MayAliasPair(varA1, varX1));
        may.add(new MayAliasPair(varA1, varAX1_gs));
        may.add(new MayAliasPair(varA1, varX2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }

    public void testTut16() {

        int numHotspots = 1;
        mySetUp("tutorial/test16.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");

        // construct expected lattice elements and check

        // init
        //MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varB));
        may.add(new MayAliasPair(varB, varC));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut17() {

        int numHotspots = 1;
        mySetUp("tutorial/test17.php", numHotspots);

        // variables to be tested
        Variable varA = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$a");
        Variable varB = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$b");
        Variable varC = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c");
        Variable varD = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$d");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA, varB);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA, varC));
        may.add(new MayAliasPair(varB, varC));
        may.add(new MayAliasPair(varA, varD));
        may.add(new MayAliasPair(varB, varD));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));
    }

    public void testTut18() {

        int numHotspots = 3;
        mySetUp("tutorial/test18.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        must.add(tempGroup);
        may = new MayAliases();
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varAX1_gs);
        tempGroup.add(varA1);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varAX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA2, varX1));
        may.add(new MayAliasPair(varA2, varAX1_gs));
        may.add(new MayAliasPair(varA2, varA1));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX1, varX2);
        tempGroup.add(varAX2_gs);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varA1, varAX1_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA1, varA2));
        may.add(new MayAliasPair(varA2, varAX1_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testTut19() {

        int numHotspots = 3;
        mySetUp("tutorial/test19.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1_gs = this.tac.getFuncVariable("a", "$x1_gs");
        Variable varBX1_gs = this.tac.getFuncVariable("b", "$x1_gs");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varBX2_gs = this.tac.getFuncVariable("b", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varX2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varAX1_gs);
        tempGroup.add(varA1);
        must.add(tempGroup);
        tempGroup = new MustAliasGroup(varX2, varAX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA1, varA2));
        may.add(new MayAliasPair(varA1, varX1));
        may.add(new MayAliasPair(varA2, varX1));
        may.add(new MayAliasPair(varA2, varAX1_gs));
        may.add(new MayAliasPair(varX1, varX2));
        may.add(new MayAliasPair(varX1, varAX2_gs));
        may.add(new MayAliasPair(varX1, varAX1_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));

        // 2
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varX2, varBX2_gs);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varBX1_gs));
        may.add(new MayAliasPair(varX1, varX2));
        may.add(new MayAliasPair(varX1, varBX2_gs));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[2].structureEquals(expected));
    }

    public void testTut20() {

        int numHotspots = 2;
        mySetUp("tutorial/test20.php", numHotspots);

        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAX2_gs = this.tac.getFuncVariable("a", "$x2_gs");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        Variable varA3 = this.tac.getFuncVariable("a", "$a3");

        // construct expected lattice elements and check

        // init
        MustAliasGroup tempGroup;
        MustAliases must;
        MayAliases may;
        AliasLatticeElement expected;

        // 0
        must = new MustAliases();
        may = new MayAliases();
        may.add(new MayAliasPair(varX1, varX2));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[0].structureEquals(expected));

        // 1
        must = new MustAliases();
        tempGroup = new MustAliasGroup(varA1, varA3);
        tempGroup.add(varX1);
        must.add(tempGroup);
        may = new MayAliases();
        may.add(new MayAliasPair(varA2, varA1));
        may.add(new MayAliasPair(varA2, varA3));
        may.add(new MayAliasPair(varA2, varX1));
        may.add(new MayAliasPair(varX1, varX2));
        may.add(new MayAliasPair(varX2, varAX2_gs));
        may.add(new MayAliasPair(varX2, varA1));
        may.add(new MayAliasPair(varX2, varA2));
        may.add(new MayAliasPair(varX2, varA3));
        expected = new AliasLatticeElement(must, may);
        Assert.assertTrue(elements[1].structureEquals(expected));
    }
}
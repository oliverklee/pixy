package at.ac.tuwien.infosys.www.pixy;

import junit.framework.Assert;
import junit.framework.TestCase;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterAnalysisNode;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.InternalStrings;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeHotspot;

// these tests should work both with an enabled and a disabled builtin functions file
public class LiteralTestCase 
extends TestCase {
    
    private String path;    // complete path to the testfile directory (with trailing slash)
    
    private TacConverter tac;
    private LiteralAnalysis literalAnalysis;
    private LiteralLatticeElement[] elements;

//  ********************************************************************************
//  SETUP **************************************************************************
//  ********************************************************************************
    
    protected void setUp() {
        this.path = MyOptions.pixy_home + "/testfiles/literal/";
    }
    
    // call this at the beginning of each test
    private void mySetUp(String testFile, int numHotspots) {
        
        Checker checker = new Checker(this.path + testFile);
        MyOptions.option_L = true;    // perform real literals analysis!
        MyOptions.option_A = true;    // perform real alias analysis!
        
        // initialize & analyze
        this.tac = checker.initialize().getTac();
        this.literalAnalysis = checker.analyzeLiterals(tac);
        
        // extract (folded) lattice elements from hotspots
        this.elements = new LiteralLatticeElement[numHotspots];
        for (int i = 0; i < numHotspots; i++) {
            this.elements[i] = (LiteralLatticeElement) this.getHotspotInfo(i).getUnrecycledFoldedValue();
        }
    }

//  ********************************************************************************
//  HELPERS ************************************************************************
//  ********************************************************************************

    // returns the analysis node associated with the hotspot given by its ID
    private InterAnalysisNode getHotspotInfo(int hotspotId) {
        
        CfgNodeHotspot hot = this.tac.getHotspot(hotspotId);
        if (hot == null) {
            Assert.fail("Tried to retrieve non-existent hotspot with ID " + hotspotId);
        }
        return this.literalAnalysis.getAnalysisNode(hot);
    }

//  ********************************************************************************
//  TESTS **************************************************************************
//  ********************************************************************************

    public void test01() {

        int numHotspots = 2;
        mySetUp("test01.php", numHotspots);
        
        // variables to be tested
        Variable varX = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x");
        Variable varA1= this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX).equals(Literal.TOP));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(Literal.NULL));
    }

    public void test02() {

        int numHotspots = 2;
        mySetUp("test02.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varX3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x3");
        Variable varX4 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x4");
        Variable varX5 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x5");
        Variable varX6 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x6");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX3).equals(new Literal("2")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("5")));
        Assert.assertTrue(elements[hid].getLiteral(varX4).equals(new Literal("5")));
        Assert.assertTrue(elements[hid].getLiteral(varX5).equals(new Literal("5")));
        Assert.assertTrue(elements[hid].getLiteral(varX6).equals(Literal.TOP));

    }

    public void test03() {

        int numHotspots = 5;
        mySetUp("test03.php", numHotspots);
        
        // variables to be tested
        Variable varZ1_1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1][1]");
        Variable varZ1_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[2][1]");
        Variable varZ1_3_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[3][1]");
        Variable varZ1_4_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[4][1]");
        Variable varZ1_5_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[5][1]");
        Variable varZ1_5_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[5][2]");
        
        Variable varZ1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1]");
        Variable varZ1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[2]");
        Variable varZ1_3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[3]");
        Variable varZ1_4 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[4]");
        
        Variable varZ3_1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][1]");
        Variable varZ3_1_3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][3]");
        Variable varZ3_1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][2]");
        Variable varZ3_1_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][2][1]");
        Variable varZ3_1_2_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][2][2]");
        Variable varZ3_1_2_4 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][2][4]");
        
        Variable varZ4_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z4[1]");
        Variable varZ4_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z4[2][1]");
        Variable varZ4_2_3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z4[2][3]");
        Variable varZ4_2_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z4[2][2][1]");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1_1).equals(new Literal("a")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_2_1).equals(new Literal("c")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_3_1).equals(new Literal("b")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_4_1).equals(new Literal("d")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_5_1).equals(Literal.TOP));
        
        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1).equals(new Literal("a")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1_1).equals(Literal.NULL));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_2).equals(new Literal("c")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_2_1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_3).equals(new Literal("b")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_4).equals(new Literal("b")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_4_1).equals(Literal.NULL));

        // 2
        hid = 2;
        Assert.assertTrue(elements[hid].getLiteral(varZ1_5_1).equals(new Literal("b")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_5_2).equals(Literal.TOP));
        
        // 3
        hid = 3;
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_1).equals(new Literal("c")));
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_3).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_2).equals(new Literal("d")));
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_2_1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_2_2).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ3_1_2_4).equals(Literal.TOP));

        // 4
        hid = 4;
        Assert.assertTrue(elements[hid].getLiteral(varZ4_1).equals(new Literal("a")));
        Assert.assertTrue(elements[hid].getLiteral(varZ4_2_1).equals(new Literal("a")));
        Assert.assertTrue(elements[hid].getLiteral(varZ4_2_3).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ4_2_2_1).equals(new Literal("b")));

    }

    public void test04() {

        int numHotspots = 2;
        mySetUp("test04.php", numHotspots);
        
        // variables to be tested
        Variable varZ1_1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1][1]");
        Variable varZ1_1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1][2]");
        Variable varZ1_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[2][1]");
        Variable varZ1_2_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[2][2]");
        Variable varZ1_1_i = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1][_main.$i]");
        
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1_1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1_2).equals(new Literal("b")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_2_1).equals(new Literal("c")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_2_2).equals(new Literal("d")));
        Assert.assertTrue(elements[hid].getLiteral(varZ1_1_i).equals(Literal.TOP));
        
        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));
        
    }
    
    public void test05() {
        
        // non-standard test for MI algorithm
        
        int numHotspots = 1;
        mySetUp("test05.php", numHotspots);
        
        // variables for which we want to determine MI variables
        Variable varZ1_i = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[_main.$i]");
        Variable varZ2_1_i = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[1][_main.$i]");
        Variable varZ2_i_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[_main.$i][1]");
        Variable varZ2_i_j = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[_main.$i][_main.$j]");
        Variable varZ3_1_i_3_j = 
            this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][_main.$i][3][_main.$j]");
        
        
        // expected MI variables
        Variable varZ1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[1]");
        Variable varZ1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z1[2]");
        Variable varZ2_1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[1][1]");
        Variable varZ2_1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[1][2]");
        Variable varZ2_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[2][1]");
        Variable varZ2_2_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z2[2][2]");
        Variable varZ3_1_2_3_4 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][2][3][4]");
        Variable varZ3_1_9_3_9 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$z3[1][9][3][9]");

        // init
        int hid;    // hotspot ID
        Variable miVar;
        Set<Variable> miSet;
        Set<Variable> expected;

        // check MI lists
        
        // only one hotspot
        hid = 0;
        
        // z1[$i]
        miVar = varZ1_i;
        miSet = new HashSet<Variable>(elements[hid].getMiList(miVar));
        expected = new HashSet<Variable>();
        expected.add(varZ1_1);
        expected.add(varZ1_2);
        Assert.assertTrue(miSet.equals(expected));
        
        // z2[1][$i]
        miVar = varZ2_1_i;
        miSet = new HashSet<Variable>(elements[hid].getMiList(miVar));
        expected = new HashSet<Variable>();
        expected.add(varZ2_1_1);
        expected.add(varZ2_1_2);
        Assert.assertTrue(miSet.equals(expected));

        // z2[$i][1]
        miVar = varZ2_i_1;
        miSet = new HashSet<Variable>(elements[hid].getMiList(miVar));
        expected = new HashSet<Variable>();
        expected.add(varZ2_1_1);
        expected.add(varZ2_2_1);
        Assert.assertTrue(miSet.equals(expected));
        
        // z2[$i][$j]
        miVar = varZ2_i_j;
        miSet = new HashSet<Variable>(elements[hid].getMiList(miVar));
        expected = new HashSet<Variable>();
        expected.add(varZ2_1_1);
        expected.add(varZ2_1_2);
        expected.add(varZ2_2_1);
        expected.add(varZ2_2_2);
        Assert.assertTrue(miSet.equals(expected));
        
        // z3[1][$i][3][$j]
        miVar = varZ3_1_i_3_j;
        miSet = new HashSet<Variable>(elements[hid].getMiList(miVar));
        expected = new HashSet<Variable>();
        expected.add(varZ3_1_2_3_4);
        expected.add(varZ3_1_9_3_9);
        Assert.assertTrue(miSet.equals(expected));
        
        
    }

    public void test06() {

        int numHotspots = 1;
        mySetUp("test06.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("-1.0")));
        
    }

    public void test07() {

        int numHotspots = 1;
        mySetUp("test07.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varX3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x3");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varX3).equals(new Literal("3.0")));
        
    }

    public void test08() {

        int numHotspots = 2;
        mySetUp("test08.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varX3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x3");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX3).equals(Literal.TOP));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varX3).equals(new Literal("3")));

    }

    public void test09() {

        int numHotspots = 1;
        mySetUp("test09.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(Literal.NULL));

    }

    public void test10() {

        int numHotspots = 1;
        mySetUp("test10.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1[1]");
        Variable varX1_2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1[2]");
        Variable varX1_2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1[2][1]");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.NULL));
        Assert.assertTrue(elements[hid].getLiteral(varX1_1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varX1_2).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varX1_2_1).equals(Literal.TOP));

    }

    // disabled: path pruning (dangerous)
    public void xtest11() {

        int numHotspots = 1;
        mySetUp("test11.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varX3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x3");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varX3).equals(new Literal("3")));

    }

    public void test12() {

        int numHotspots = 1;
        mySetUp("test12.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(Literal.TOP));

    }

    public void test13() {

        int numHotspots = 1;
        mySetUp("test13.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("2")));

    }

    public void test14() {

        int numHotspots = 1;
        mySetUp("test14.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

    }

    public void test14a() {

        int numHotspots = 1;
        mySetUp("test14a.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("8")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("8")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("7")));

    }

    public void test15() {

        int numHotspots = 1;
        mySetUp("test15.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("2")));

    }

    public void test16() {

        int numHotspots = 1;
        mySetUp("test16.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

    }

    public void test16a() {

        int numHotspots = 1;
        mySetUp("test16a.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("7")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("7")));

    }

    public void test17() {

        int numHotspots = 2;
        mySetUp("test17.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varAP1).equals(new Literal("2")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varAP1).equals(new Literal("4")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("4")));

    }

    public void test18() {

        int numHotspots = 2;
        mySetUp("test18.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varAP1).equals(new Literal("3")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varAP1).equals(new Literal("5")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("4")));
    }

    // disabled: path pruning (dangerous)
    public void xtest19() {

        int numHotspots = 2;
        mySetUp("test19.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
    }

    public void test20() {

        int numHotspots = 2;
        mySetUp("test20.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(Literal.TOP));
    }

    public void test21() {

        int numHotspots = 2;
        mySetUp("test21.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
    }

    public void test21a() {

        int numHotspots = 2;
        mySetUp("test21a.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(Literal.TOP));
    }

    public void test22() {

        int numHotspots = 2;
        mySetUp("test22.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
    }
    
    // note: for some time, this test strangely failed in an unpredictable way;
    // this effect has disappeared in the meantime;
    // same for test23 of literal analysis
    public void test23() {

        int numHotspots = 2;
        mySetUp("test23.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));
        
        System.out.println("xyz: " + elements[hid].getLiteral(varA1) + ", " + Literal.TOP);
        
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(Literal.TOP));
    }

    public void test24() {

        int numHotspots = 2;
        mySetUp("test24.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("7")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(Literal.TOP));
    }

    public void test25() {

        int numHotspots = 2;
        mySetUp("test25.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("1")));
    }

    public void test26() {

        int numHotspots = 2;
        mySetUp("test26.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(Literal.TOP));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(Literal.TOP));
    }

    public void test27() {

        int numHotspots = 2;
        mySetUp("test27.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("4")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("4")));
    }

    public void test28() {

        int numHotspots = 3;
        mySetUp("test28.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varAX1 = this.tac.getFuncVariable("a", "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varAX1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varAX1).equals(new Literal("2")));

        // 2
        hid = 2;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varAX1).equals(new Literal("3")));

    }

    public void test29() {

        int numHotspots = 1;
        mySetUp("test29.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(Literal.TOP));
    }

    public void test30() {

        int numHotspots = 1;
        mySetUp("test30.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
    }

    public void test31() {

        int numHotspots = 1;
        mySetUp("test31.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.NULL));
    }

    public void test32() {

        int numHotspots = 1;
        mySetUp("test32.php", numHotspots);
        
        // variables to be tested
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
    }

    public void test33() {

        int numHotspots = 1;
        mySetUp("test33.php", numHotspots);
        
        // variables to be tested
        Variable varB2 = this.tac.getFuncVariable("b", "$b2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varB2).equals(new Literal("1")));
    }

    public void test34() {

        int numHotspots = 1;
        mySetUp("test34.php", numHotspots);
        
        // variables to be tested
        Variable varB2 = this.tac.getFuncVariable("b", "$b2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varB2).equals(new Literal("1")));
    }

    public void test35() {

        int numHotspots = 2;
        mySetUp("test35.php", numHotspots);
        
        // variables to be tested
        Variable varX1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1[1]");
        Variable varX2_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2[1]");
        Variable varX2_01 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2[01]");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1_1).equals(new Literal("string-val")));
        
        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX2_1).equals(new Literal("int-val")));
        Assert.assertTrue(elements[hid].getLiteral(varX2_01).equals(new Literal("string-val")));
        
        // LATER: hotspot #2
        // (need to fix modeling of implicit conversion)
    }
    
    public void test36() {

        int numHotspots = 2;
        mySetUp("test36.php", numHotspots);
        
        // variables to be tested
        Variable varAP1 = this.tac.getFuncVariable("a", "$ap1");
        Variable varBP1 = this.tac.getFuncVariable("b", "$bp1");
        Variable varBP2 = this.tac.getFuncVariable("b", "$bp2");
        Variable varBP3 = this.tac.getFuncVariable("b", "$bp3");
        Variable varBP4 = this.tac.getFuncVariable("b", "$bp4");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varAP1).equals(new Literal("1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varBP1).equals(new Literal("7")));
        Assert.assertTrue(elements[hid].getLiteral(varBP2).equals(new Literal("8")));
        Assert.assertTrue(elements[hid].getLiteral(varBP3).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varBP4).equals(new Literal("4")));

    }

    public void test37() {

        int numHotspots = 5;
        mySetUp("test37.php", numHotspots);
        
        // variables to be tested
        Variable varGET_Y = this.tac.getSuperGlobal("$_GET[y]");
        Variable varGET_Z = this.tac.getSuperGlobal("$_GET[z]");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varGET_Y).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varGET_Z).equals(new Literal("5")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varGET_Y).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varGET_Z).equals(new Literal("2")));

        // 2
        hid = 2;
        Assert.assertTrue(elements[hid].getLiteral(varGET_Y).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varGET_Z).equals(new Literal("4")));

        // 3
        hid = 3;
        Assert.assertTrue(elements[hid].getLiteral(varGET_Y).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varGET_Z).equals(new Literal("5")));

        // 4
        hid = 4;
        Assert.assertTrue(elements[hid].getLiteral(varGET_Y).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varGET_Z).equals(new Literal("5")));

    }

    public void test38() {

        int numHotspots = 6;
        mySetUp("test38.php", numHotspots);
        
        // variables to be tested
        Variable varC1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C1");
        Variable varc1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c1");
        Variable varC2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C2");
        Variable varc2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c2");
        Variable varC3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C3");
        Variable varc3 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c3");
        Variable varC4 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C4");
        Variable varC5 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C5");
        Variable varc5 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c5");
        Variable varC6 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$C6");
        Variable varc6 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$c6");

        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varC1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varc1).equals(new Literal("c1")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varC2).equals(new Literal("2")));
        Assert.assertTrue(elements[hid].getLiteral(varc2).equals(new Literal("c2")));

        // 2
        hid = 2;
        Assert.assertTrue(elements[hid].getLiteral(varC3).equals(new Literal("3")));
        Assert.assertTrue(elements[hid].getLiteral(varc3).equals(new Literal("3")));

        // 3
        hid = 3;
        Assert.assertTrue(elements[hid].getLiteral(varC4).equals(new Literal("4")));

        // 4
        hid = 4;
        Assert.assertTrue(elements[hid].getLiteral(varC5).equals(new Literal("5")));
        Assert.assertTrue(elements[hid].getLiteral(varc5).equals(Literal.TOP));

        // 5
        hid = 5;
        Assert.assertTrue(elements[hid].getLiteral(varC6).equals(new Literal("c6")));
        Assert.assertTrue(elements[hid].getLiteral(varc6).equals(new Literal("c6")));

    }
    
    public void test39() {

        int numHotspots = 2;
        mySetUp("test39.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        Variable varA1 = this.tac.getFuncVariable("a", "$a1");
        Variable varA2 = this.tac.getFuncVariable("a", "$a2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("2")));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varA1).equals(new Literal("1")));
        Assert.assertTrue(elements[hid].getLiteral(varA2).equals(new Literal("2")));

    }

    // disabled: path pruning (dangerous)
    public void xtest40() {

        int numHotspots = 1;
        mySetUp("test40.php", numHotspots);
        
        // variables to be tested
        // <none>
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid] == null);   // unreachable

    }

    // with disabled path pruning: just terminate!
    public void test40b() {

        int numHotspots = 1;
        mySetUp("test40.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));

    }

    public void test41() {

        int numHotspots = 3;
        mySetUp("test41.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        Variable varX2 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x2");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));

        // 1
        hid = 1;
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));

        // 2
        hid = 2;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));
        Assert.assertTrue(elements[hid].getLiteral(varX2).equals(new Literal("1")));

    }

    public void test42() {

        int numHotspots = 1;
        mySetUp("test42.php", numHotspots);
        
        // variables to be tested
        // <none>
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid] != null);

    }

    public void test43() {

        int numHotspots = 1;
        mySetUp("test43.php", numHotspots);
        
        // variables to be tested
        Variable varX1_1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1[1]");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1_1).equals(new Literal("1")));

    }

    public void test44() {

        int numHotspots = 1;
        mySetUp("test44.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));

    }
    
    public void test45() {
        
        int numHotspots = 1;
        mySetUp("test45.php", numHotspots);
        
        // variables to be tested
        Variable varY = this.tac.getFuncVariable("foo", "$y");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varY).equals(Literal.TOP));
        
    }
    
    public void test46() {

        int numHotspots = 1;
        mySetUp("test46.php", numHotspots);
        
        // variables to be tested
        Variable varX1 = this.tac.getFuncVariable(InternalStrings.mainFunctionName, "$x1");
        
        // init
        int hid;    // hotspot ID
        
        // check literals
        
        // 0
        hid = 0;
        Assert.assertTrue(elements[hid].getLiteral(varX1).equals(Literal.TOP));

    }


}

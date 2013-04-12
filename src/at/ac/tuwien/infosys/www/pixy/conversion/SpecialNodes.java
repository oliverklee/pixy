package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeHotspot;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeTester;

class SpecialNodes {

    private SpecialNodes() {
        // utility class, no instances needed
    }

    static CfgNode get(String marker, TacFunction function, TacConverter tac) {
        // EFF: distinguish between markers more efficiently
        CfgNode retMe = null;

        if (marker.startsWith("_test_")) {
            // a tester node:
            //
            // _test_<whatToTest>_<numList>
            // e.g.:
            // _test_taint_0
            // _test_taint_3_7
            // _test_arrayLabel_12_24

            // find the underline after the "whatToTest" substring
            int delimiter = marker.indexOf('_', 6);

            if (delimiter == -1) {
                throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
            }

            // the "whatToTest" substring
            String whatToTest = marker.substring(6, delimiter);

            // find out what to test...
            int whatToTestInt;
            if (whatToTest.equals("taint")) {
                whatToTestInt = CfgNodeTester.TEST_TAINT;
            } else if (whatToTest.equals("arrayLabel")) {
                whatToTestInt = CfgNodeTester.TEST_ARRAYLABEL;
            } else {
                throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
            }

            Set numSet;
            try {
                numSet = makeNumSet(marker.substring(delimiter + 1, marker.length()));
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
            }

            retMe = new CfgNodeTester(whatToTestInt, numSet);

        } else if (marker.startsWith("_hotspot")) {
            // hotspots are only used for JUnit tests
            try {
                Integer hotspotId = Integer.valueOf(marker.substring(8));
                retMe = new CfgNodeHotspot(hotspotId);
                tac.addHotspot((CfgNodeHotspot) retMe);
            } catch (IndexOutOfBoundsException ex) {
                throw new RuntimeException("Illegal hotspot marker: no ID suffix");
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Illegal hotspot marker: non-numeric ID suffix");
            }

        } else {
            System.out.println("Unkown marker: " + marker);
            throw new RuntimeException("SNH");
        }
        return retMe;
    }

    // input: "num1" or "num1_num2" or "num1_num2_num3" ...
    // with numX from {0, 1, 2, ...};
    // output: Set of Integers
    private static Set<Integer> makeNumSet(String numString)
        throws NumberFormatException {

        Set<Integer> numSet = new HashSet<Integer>();

        boolean findNext = true;
        int from = 0;
        int to;

        while (findNext) {

            to = numString.indexOf('_', from);
            String num;
            if (to == -1) {
                // this is the last one
                num = numString.substring(from, numString.length());
                findNext = false;
            } else {
                // there are others
                num = numString.substring(from, to);
                findNext = true;
                from = to + 1;
            }
            Integer numInt = Integer.valueOf(num);
            numSet.add(numInt);
        }

        return numSet;
    }
}
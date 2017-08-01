package at.ac.tuwien.infosys.www.pixy.conversion;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Hotspot;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester;

class SpecialNodes {

	private SpecialNodes() {
	}

	static AbstractCfgNode get(String marker, TacFunction function, TacConverter tac) {
		AbstractCfgNode retMe = null;

		if (marker.startsWith("_test_")) {

			int delimiter = marker.indexOf('_', 6);

			if (delimiter == -1) {
				throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
			}
			String whatToTest = marker.substring(6, delimiter);

			int whatToTestInt;
			if (whatToTest.equals("taint")) {
				whatToTestInt = Tester.TEST_TAINT;
			} else if (whatToTest.equals("arrayLabel")) {
				whatToTestInt = Tester.TEST_ARRAYLABEL;
			} else {
				throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
			}

			Set<?> numSet;
			try {
				numSet = makeNumSet(marker.substring(delimiter + 1, marker.length()));
			} catch (NumberFormatException ex) {
				throw new RuntimeException("Error: Invalid '~_test_' marker in builtin functions file");
			}

			retMe = new Tester(whatToTestInt, numSet);

		} else if (marker.startsWith("_hotspot")) {
			try {
				Integer hotspotId = Integer.valueOf(marker.substring(8));
				retMe = new Hotspot(hotspotId);
				tac.addHotspot((Hotspot) retMe);
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

	private static Set<Integer> makeNumSet(String numString) throws NumberFormatException {

		Set<Integer> numSet = new HashSet<Integer>();

		boolean findNext = true;
		int from = 0;
		int to;

		while (findNext) {

			to = numString.indexOf('_', from);
			String num;
			if (to == -1) {
				num = numString.substring(from, numString.length());
				findNext = false;
			} else {
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

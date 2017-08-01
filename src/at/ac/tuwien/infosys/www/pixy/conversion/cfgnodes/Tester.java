package at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Tester extends AbstractCfgNode {

	private Set<?> paramNumbers;

	static public final int TEST_TAINT = 0;
	static public final int TEST_ARRAYLABEL = 1;

	private int whatToTest;

	public Tester(int whatToTest, Set<?> paramNumbers) {
		super();
		this.whatToTest = whatToTest;
		this.paramNumbers = paramNumbers;
	}

	public List<Variable> getVariables() {
		return Collections.emptyList();
	}

	public int getWhatToTest() {
		return this.whatToTest;
	}

	public Set<?> getParamNumbers() {
		return this.paramNumbers;
	}

	public void replaceVariable(int index, Variable replacement) {
		throw new RuntimeException("SNH");
	}
}
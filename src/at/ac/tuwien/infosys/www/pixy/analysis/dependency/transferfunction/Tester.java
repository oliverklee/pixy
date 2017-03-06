package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencySet;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFormalParameter;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;

public class Tester extends AbstractTransferFunction {

	private Variable retVar;

	private int whatToTest;

	private List<Variable> testUs;

	public Tester(at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester cfgNode) {

		TacFunction function = cfgNode.getEnclosingFunction();
		this.retVar = (Variable) function.getRetVar();
		this.whatToTest = cfgNode.getWhatToTest();
		this.testUs = new LinkedList<Variable>();

		for (Iterator<?> iter = cfgNode.getParamNumbers().iterator(); iter.hasNext();) {
			Integer paramNum = (Integer) iter.next();
			int param_int = paramNum.intValue();
			TacFormalParameter formalParam = function.getParam(param_int);
			if (formalParam == null) {
				throw new RuntimeException("Error: Function " + function.getName() + " has no param #" + param_int
						+ "; check builtin functions" + "file");
			}
			this.testUs.add(formalParam.getVariable());
		}
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		DependencyLatticeElement in = (DependencyLatticeElement) inX;
		DependencyLatticeElement out = new DependencyLatticeElement(in);

		if (whatToTest == at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester.TEST_TAINT) {

			DependencySet useMe = null;
			for (Variable testMe : this.testUs) {
				DependencySet testMeTaint = in.getDep(testMe);
				if (useMe == null) {
					useMe = testMeTaint;
				} else {
					useMe = DependencySet.lub(useMe, testMeTaint);
				}
			}

			out.setRetVar(this.retVar, useMe, useMe);
			return out;

		} else if (whatToTest == at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester.TEST_ARRAYLABEL) {

			throw new RuntimeException("not yet");

		} else {
			throw new RuntimeException("SNH");
		}
	}
}

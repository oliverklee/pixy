package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.automaton.Transition;

public class MyTransducer extends AbstractAutomaton {

	public MyTransducer(MyAcceptor orig) {

		super(orig);

		Set<MyTransition> origDelta = this.getDelta();
		this.clearTransitions();
		for (MyTransition t : origDelta) {
			MyTransducerRelation tr = new MyTransducerRelation(t.getLabel(), t.getLabel());
			this.addTransition(new MyTransition(t.getStart(), tr, t.getEnd()));
		}
	}

	public MyTransducer(MyTransducer orig) {
		super(orig);
	}

	public MyTransducer(rationals.transductions.TransducerNivat a) {

		super();

		Map<rationals.State, MyState> map = new HashMap<rationals.State, MyState>();

		for (Iterator<?> iter = a.states().iterator(); iter.hasNext();) {
			rationals.State e = (rationals.State) iter.next();
			map.put(e, this.addState(e.isInitial(), e.isTerminal()));
		}

		for (Iterator<?> iter = a.delta().iterator(); iter.hasNext();) {
			rationals.Transition t = (rationals.Transition) iter.next();
			rationals.transductions.TransducerRelation origtr = (rationals.transductions.TransducerRelation) t.label();
			this.addTransition(new MyTransition(map.get(t.start()),
					new MyTransducerRelation(origtr.getIn(), origtr.getOut()), map.get(t.end())));
		}
	}

	public void toFsmFile(String filename, Map<Object, Integer> label2Int) throws IOException {

		Writer writer = new FileWriter(filename + ".txt");

		int i = 1 + label2Int.size();
		Set<MyTransition> delta = this.getDelta();
		for (MyTransition t : delta) {
			MyTransducerRelation rel = (MyTransducerRelation) t.getLabel();
			Object inLabel = rel.getIn();
			Object outLabel = rel.getOut();
			if (inLabel != null) {
				if (label2Int.get(inLabel) == null) {
					label2Int.put(inLabel, i++);
				}
			}
			if (outLabel != null) {
				if (label2Int.get(outLabel) == null) {
					label2Int.put(outLabel, i++);
				}
			}
		}

		String eps = "EPS";

		Writer symWriter = new FileWriter(filename + ".sym");
		symWriter.write(eps + " 0\n");
		for (Map.Entry<Object, Integer> entry : label2Int.entrySet()) {
			symWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
		}
		symWriter.close();

		for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
			writeTransition(t, writer, eps);
		}

		for (MyTransition t : delta) {
			if (t.getStart().isInitial()) {
				continue;
			}
			writeTransition(t, writer, eps);
		}

		for (MyState state : this.getTerminals()) {
			writer.write(state + "\n");
		}

		writer.close();
	}

	public void toFsmFile(String filename) throws IOException {

		Writer writer = new FileWriter(filename + ".txt");

		Set<MyTransition> delta = this.getDelta();

		String eps = "EPS";

		for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
			writeTransition(t, writer, eps);
		}

		for (MyTransition t : delta) {
			if (t.getStart().isInitial()) {
				continue;
			}
			writeTransition(t, writer, eps);
		}

		for (MyState state : this.getTerminals()) {
			writer.write(state + "\n");
		}
		writer.close();
	}

	private void writeTransition(MyTransition t, Writer writer, String eps) throws IOException {

		MyTransducerRelation rel = (MyTransducerRelation) t.getLabel();
		Object inLabel = rel.getIn();
		String inString;
		if (inLabel == null) {
			inString = eps;
		} else {
			inString = inLabel.toString();
			StringBuilder temp = new StringBuilder();
			Transition.appendCharString(inString.charAt(0), temp);
			inString = temp.toString();
		}

		Object outLabel = rel.getOut();
		String outString;
		if (outLabel == null) {
			outString = eps;
		} else {
			outString = outLabel.toString();
			StringBuilder temp = new StringBuilder();
			Transition.appendCharString(outString.charAt(0), temp);
			outString = temp.toString();
		}
		writer.write(t.getStart() + " " + t.getEnd() + " " + inString + " " + outString + " " + "\n");
	}
}
package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.*;
import java.util.*;

public class MyAcceptor extends AbstractAutomaton {

	public MyAcceptor() {
		super();
	}

	public MyAcceptor(AbstractAutomaton orig) {
		super(orig);
	}

	public MyAcceptor(rationals.Automaton orig) {
		super(orig);
	}

	public String toRegEx() throws Exception {

		MyAcceptor aut = this;

		aut.addUniqueStartEnd();

		MyAcceptor my = aut;

		my.mergeTransitions();
		my.toDot("aut1.dot");

		for (Iterator<?> iter = my.getStates().iterator(); iter.hasNext();) {
			MyState state = (MyState) iter.next();
			if (state.isTerminal() || state.isInitial()) {
				continue;
			}

			MyTransition loopTrans = my.getTransition(state, state);
			String regex_s = null;
			if (loopTrans == null) {
				regex_s = "(1)";
			} else {
				regex_s = (String) loopTrans.getLabel();
			}

			Set<MyTransition> incoming = my.getIncomingNoLoop(state);
			for (MyTransition in : incoming) {

				MyState state_qi = in.getStart();
				String regex_qi = (String) in.getLabel();

				Set<MyTransition> outgoing = my.getOutgoingNoLoop(state);
				for (MyTransition out : outgoing) {

					MyState state_pj = out.getEnd();
					String regex_pj = (String) out.getLabel();

					String regex_r = null;
					MyTransition trans_r = my.getTransition(state_qi, state_pj);
					if (trans_r == null) {
						regex_r = "(0)";
					} else {
						regex_r = (String) trans_r.getLabel();
						my.removeTransitions(state_qi, state_pj);
					}

					String regex_total = "(" + regex_r + "+" + regex_qi + regex_s + "*" + regex_pj + ")";
					System.out.println(
							"Regex_total for " + state + " / " + state_qi + "->" + state_pj + ": " + regex_total);

					my.addTransition(new MyTransition(state_qi, regex_total, state_pj));
				}
			}
			my.removeState(state);
		}

		Set<MyTransition> delta = my.getDelta();
		if (delta.size() != 1) {
			throw new RuntimeException("can't determine final regex! delta size: " + delta.size());
		}
		MyTransition trans = delta.iterator().next();
		String final_regex = (String) trans.getLabel();

		return final_regex;
	}

	public void toFsmFile(String filename, Map<Object, Integer> label2Int) throws IOException {

		Writer writer = new FileWriter(filename + ".txt");

		int i = 1 + label2Int.size();
		Set<MyTransition> delta = this.getDelta();
		for (MyTransition t : delta) {
			Object label = t.getLabel();
			if (label2Int.get(label) == null) {
				label2Int.put(label, i++);
			}
		}

		Writer symWriter = new FileWriter(filename + ".sym");
		symWriter.write("EPS 0\n");
		for (Map.Entry<Object, Integer> entry : label2Int.entrySet()) {
			symWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
		}
		symWriter.close();

		for (MyTransition t : this.getOutgoingTransitions(this.getInitial())) {
			writer.write(t.getStart() + " " + t.getEnd() + " " + t.getLabel() + "\n");
		}

		for (MyTransition t : delta) {
			if (t.getStart().isInitial()) {
				continue;
			}
			writer.write(t.getStart() + " " + t.getEnd() + " " + t.getLabel() + "\n");
		}

		for (MyState state : this.getTerminals()) {
			writer.write(state + "\n");
		}

		writer.close();
	}

}

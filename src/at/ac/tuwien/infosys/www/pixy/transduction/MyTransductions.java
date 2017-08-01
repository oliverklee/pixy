package at.ac.tuwien.infosys.www.pixy.transduction;

import java.io.*;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.automaton.Automaton;
import rationals.State;
import rationals.Transition;
import rationals.transductions.TransducerNivat;
import rationals.transductions.TransducerRelation;

public class MyTransductions {

	private MyAlphabet alphabet;

	public MyTransductions() {
		this.alphabet = new MyAlphabet();
	}

	public Automaton str_replace(String search, String replace, Automaton subject) {

		String subjectAutoFSM = subject.toFsmTools();
		String tempDir = MyOptions.pixy_home + "/transducers/temp/";
		new File(tempDir).mkdir();

		write(tempDir + "subject.txt", subjectAutoFSM);
		write(tempDir + "subject-sym.txt", this.alphabet.getFSMSymbols());

		try {
			MyTransducer t = str_replace_transducer(search, replace);
			t.toFsmFile(tempDir + "transducer");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("");
		}

		try {
			Process p = Runtime.getRuntime().exec(MyOptions.pixy_home + "/scripts/transduce.sh");
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("");
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("");
		}

		Automaton result = Automaton.fromFsmTools(tempDir + "result.txt");
		return result;
	}

	private void write(String filename, String contents) {
		try {
			Writer writer = new FileWriter(filename);
			writer.write(contents);
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private MyTransducer str_replace_transducer(String search, String replace) throws Exception {

		if (search.length() == 0 || replace.length() == 0) {
			throw new RuntimeException("not yet");
		}

		TransducerNivat t = new TransducerNivat();
		State start = t.addState(true, true);
		State firstAccept = null;
		State trap = t.addState(false, true);
		firstAccept = t.addState(false, false);
		t.addTransition(new Transition(start, new TransducerRelation(search.charAt(0), null), firstAccept));

		State current = firstAccept;
		List<Object> pushback = new LinkedList<Object>();
		for (int i = 1; i < search.length(); i++) {
			State next = t.addState(false, false);

			pushback.add(search.charAt(i - 1));

			t.addTransition(new Transition(current, new TransducerRelation(search.charAt(i), null), next));

			makeCompositeTransition(t, pushback, null, current, trap);

			makeCompositeTransition(t, pushback, search.charAt(0), current, firstAccept);

			addPrefixedRemainingTransitions(t, alphabet, pushback, current, start);

			current = next;

		}

		State lastAccept = current;

		List<Object> replaceWithList = new LinkedList<Object>();
		for (char c : replace.toCharArray()) {
			replaceWithList.add(c);
		}
		makeCompositeTransition(t, replaceWithList, null, lastAccept, start);

		addRemainingTransitions(t, alphabet, start, start);

		return new MyTransducer(t);
	}

	public static void makeCompositeTransition(TransducerNivat t, List<Object> composite, Object firstInLabel,
			State from, State to) throws Exception {

		if (composite.size() == 0) {
			throw new RuntimeException("SNH");
		}
		if (composite.size() == 1) {
			Object compositeElement = composite.iterator().next();
			t.addTransition(new Transition(from, new TransducerRelation(firstInLabel, compositeElement), to));
			return;
		}

		int i = 0;
		State previous = from;
		for (Object compositeElement : composite) {

			Object inLabel;
			if (i == 0) {
				inLabel = firstInLabel;
			} else {
				inLabel = null;
			}

			State targetState;
			if (i == composite.size() - 1) {
				targetState = to;
			} else {
				targetState = t.addState(false, false);
			}

			t.addTransition(new Transition(previous, new TransducerRelation(inLabel, compositeElement), targetState));
			previous = targetState;

			i++;
		}

	}

	public static void addPrefixedRemainingTransitions(TransducerNivat t, MyAlphabet alphabet, List<Object> prefix,
			State from, State to) throws Exception {

		Set<?> fromTrans = t.delta(from);
		Set<Object> existingInLabels = new HashSet<Object>();

		for (Iterator<?> iter = fromTrans.iterator(); iter.hasNext();) {
			Transition trans = (Transition) iter.next();
			TransducerRelation rel = (TransducerRelation) trans.label();
			Object inLabel = rel.getIn();
			existingInLabels.add(inLabel);
		}

		for (Object c : alphabet.getAlphabet()) {
			if (!existingInLabels.contains(c)) {
				List<Object> composite = new LinkedList<Object>(prefix);
				composite.add(c);
				makeCompositeTransition(t, composite, c, from, to);
			}
		}
	}

	public static void addRemainingTransitions(TransducerNivat t, MyAlphabet alphabet, State from, State to)
			throws Exception {

		Set<?> fromTrans = t.delta(from);
		Set<Object> existingInLabels = new HashSet<Object>();

		for (Iterator<?> iter = fromTrans.iterator(); iter.hasNext();) {
			Transition trans = (Transition) iter.next();
			TransducerRelation rel = (TransducerRelation) trans.label();
			Object inLabel = rel.getIn();
			existingInLabels.add(inLabel);
		}

		for (Object c : alphabet.getAlphabet()) {
			if (!existingInLabels.contains(c)) {
				t.addTransition(new Transition(from, new TransducerRelation(c, c), to));
			}
		}
	}
}

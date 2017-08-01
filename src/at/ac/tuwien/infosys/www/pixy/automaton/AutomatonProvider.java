package at.ac.tuwien.infosys.www.pixy.automaton;

import java.io.IOException;

public interface AutomatonProvider {

	public Automaton getAutomaton(String name) throws IOException;
}

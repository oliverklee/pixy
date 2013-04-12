package at.ac.tuwien.infosys.www.pixy.transduction;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.automaton.Transition;

public class MyAlphabet {

    private Set<Object> alphabet;

    public MyAlphabet() {
        // LATER: do we need a complete alphabet here...?
        char[] alphabet = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                ' ', '*', '"', '\'', '='};

        this.alphabet = new HashSet<Object>();
        for (char c : alphabet) {
            this.alphabet.add(c);
        }
    }

    public Set<Object> getAlphabet() {
        return new HashSet<Object>(this.alphabet);
    }

    // returns the contents for an fsmtools symbols file
    public String getFSMSymbols() {
        StringBuilder b = new StringBuilder();
        b.append("EPS 0\n");
        for (Object o : this.alphabet) {
            StringBuilder temp = new StringBuilder();
            Transition.appendCharString((Character) o, temp);
            b.append(temp.toString()).append(" ").append((int)(Character)o).append("\n");
        }
        return b.toString();
    }
}
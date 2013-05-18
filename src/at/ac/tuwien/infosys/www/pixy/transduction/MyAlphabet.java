package at.ac.tuwien.infosys.www.pixy.transduction;

import at.ac.tuwien.infosys.www.pixy.automaton.Transition;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class MyAlphabet {
    private Set<Character> alphabet;

    public MyAlphabet() {
        // LATER: do we need a complete alphabet here...?
        char[] alphabet = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            ' ', '*', '"', '\'', '='};

        this.alphabet = new HashSet<>();
        for (char c : alphabet) {
            this.alphabet.add(c);
        }
    }

    /**
     * Gets a copy of the alphabet.
     *
     * @return a copy of the alphabet
     */
    public Set<Character> getAlphabet() {
        return new HashSet<>(this.alphabet);
    }

    // returns the contents for an fsmtools symbols file
    public String getFSMSymbols() {
        StringBuilder b = new StringBuilder();
        b.append("EPS 0\n");
        for (Character characterFromAlphabet : this.alphabet) {
            StringBuilder temp = new StringBuilder();
            Transition.appendCharString(characterFromAlphabet, temp);
            b.append(temp.toString()).append(" ").append((int) characterFromAlphabet).append("\n");
        }
        return b.toString();
    }
}
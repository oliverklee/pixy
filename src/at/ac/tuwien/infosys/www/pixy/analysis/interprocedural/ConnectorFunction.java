package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CSContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps from position to position for a certain call node.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ConnectorFunction {
    // CSContext -> CSContext
    private Map<CSContext, CSContext> pos2pos;

    // reverse mapping of pos2pos:
    // CSContext -> Set of CSContexts
    private Map<CSContext, Set<CSContext>> reverse;

    // creates an empty connector function
    public ConnectorFunction() {
        this.pos2pos = new HashMap<>();
        this.reverse = new HashMap<>();
    }

    // adds the given mapping
    public void add(int from, int to) {

        CSContext fromInt = new CSContext(from);
        CSContext toInt = new CSContext(to);

        this.pos2pos.put(fromInt, toInt);

        // maintain reverse mapping

        Set<CSContext> reverseSet = this.reverse.get(toInt);
        if (reverseSet == null) {
            // there was no such reverse mapping:
            // create it together with a new set
            reverseSet = new HashSet<>();
            reverseSet.add(fromInt);
            this.reverse.put(toInt, reverseSet);
        } else {
            // add to already existing reverse mapping set
            reverseSet.add(fromInt);
        }
    }

    // applies this connector function to the given input value
    public CSContext apply(int input) {
        CSContext output = this.pos2pos.get(new CSContext(input));
        return output;
    }

    // reverse application: returns a set of inputs (CSContext's) for the given output
    // (might be null if there is no such output)
    public Set<CSContext> reverseApply(int output) {
        return this.reverse.get(new CSContext(output));
    }

    public String toString() {
        if (this.pos2pos.isEmpty()) {
            return "<empty>";
        }
        StringBuilder myString = new StringBuilder();
        for (Map.Entry<CSContext, CSContext> entry : this.pos2pos.entrySet()) {
            CSContext from = entry.getKey();
            CSContext to = entry.getValue();
            myString.append(from);
            myString.append(" -> ");
            myString.append(to);
            myString.append(System.getProperty("line.separator"));
        }
        return myString.substring(0, myString.length() - 1);
    }
}
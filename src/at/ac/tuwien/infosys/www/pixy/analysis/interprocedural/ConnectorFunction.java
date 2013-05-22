package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural;

import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring.CallStringContext;

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
    // CallStringContext -> CallStringContext
    private Map<CallStringContext, CallStringContext> pos2pos;

    // reverse mapping of pos2pos:
    // CallStringContext -> Set of CSContexts
    private Map<CallStringContext, Set<CallStringContext>> reverse;

    // creates an empty connector function
    public ConnectorFunction() {
        this.pos2pos = new HashMap<>();
        this.reverse = new HashMap<>();
    }

    // adds the given mapping
    public void add(int from, int to) {

        CallStringContext fromInt = new CallStringContext(from);
        CallStringContext toInt = new CallStringContext(to);

        this.pos2pos.put(fromInt, toInt);

        // maintain reverse mapping

        Set<CallStringContext> reverseSet = this.reverse.get(toInt);
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
    public CallStringContext apply(int input) {
        CallStringContext output = this.pos2pos.get(new CallStringContext(input));
        return output;
    }

    // reverse application: returns a set of inputs (CallStringContext's) for the given output
    // (might be null if there is no such output)
    public Set<CallStringContext> reverseApply(int output) {
        return this.reverse.get(new CallStringContext(output));
    }

    public String toString() {
        if (this.pos2pos.isEmpty()) {
            return "<empty>";
        }
        StringBuilder myString = new StringBuilder();
        for (Map.Entry<CallStringContext, CallStringContext> entry : this.pos2pos.entrySet()) {
            CallStringContext from = entry.getKey();
            CallStringContext to = entry.getValue();
            myString.append(from);
            myString.append(" -> ");
            myString.append(to);
            myString.append(System.getProperty("line.separator"));
        }
        return myString.substring(0, myString.length() - 1);
    }
}
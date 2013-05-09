package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class ECS {

    // a list of CallString objects
    List<CallString> callStrings;

    // creates empty ECS
    public ECS() {
        this.callStrings = new LinkedList<CallString>();
    }

    // creates one-element ECS
    public ECS(CallString firstCallString) {
        this.callStrings = new LinkedList<CallString>();
        this.callStrings.add(firstCallString);
    }

    // returns the position of the given call string ( >= 0), or -1 if
    // it's not in here
    public int getPosition(CallString findMe) {
        int index = 0;
        for (Iterator iter = this.callStrings.iterator(); iter.hasNext(); ) {
            CallString callString = (CallString) iter.next();
            if (callString.equals(findMe)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    // returns the call string at the given position (null if
    // there is no such position)
    public CallString getCallString(int position) {
        if (position >= this.callStrings.size()) {
            return null;
        }
        return (CallString) this.callStrings.get(position);
    }

    public List getCallStrings() {
        return this.callStrings;
    }

    // appends the given call string and returns its index
    public int append(CallString appendMe) {
        int newIndex = this.callStrings.size();
        this.callStrings.add(appendMe);
        return newIndex;
    }

    public String toString() {
        if (this.callStrings.isEmpty()) {
            return "<empty>";
        }

        StringBuilder myString = new StringBuilder();
        for (Iterator iter = this.callStrings.iterator(); iter.hasNext(); ) {
            CallString callString = (CallString) iter.next();
            myString.append(callString);
            myString.append(", ");
        }
        return myString.substring(0, myString.length() - 2);
    }

    public String dump() {
        if (this.callStrings.isEmpty()) {
            return "<empty>\n";
        }

        StringBuilder b = new StringBuilder();
        for (Iterator iter = this.callStrings.iterator(); iter.hasNext(); ) {
            CallString callString = (CallString) iter.next();
            b.append(callString.dump());
        }
        return b.toString();
    }

    public boolean isEmpty() {
        return this.callStrings.isEmpty();
    }

    public int size() {
        return this.callStrings.size();
    }
}
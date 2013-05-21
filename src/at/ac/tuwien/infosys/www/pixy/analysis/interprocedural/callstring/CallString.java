package at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.callstring;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Call;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallString {
    // a list of Call's; never longer than the k-size of the analysis
    private LinkedList<Call> callNodeList;

    // creates the empty call string
    public CallString() {
        this.callNodeList = new LinkedList<>();
    }

    // shall only be used by CallString.append
    private CallString(LinkedList<Call> callNodeList) {
        this.callNodeList = callNodeList;
    }

    public CallString append(Call callNode, int kSize) {
        LinkedList<Call> newList = new LinkedList<>(this.callNodeList);
        newList.add(callNode);
        if (newList.size() > kSize) {
            newList.remove(0);
        }
        return new CallString(newList);
    }

    // returns the last (rightmost) call node
    public Call getLast() {
        return this.callNodeList.getLast();
    }

    public LinkedList<Call> getCallNodeList() {
        return this.callNodeList;
    }

    public int hashCode() {
        return this.callNodeList.hashCode();
    }

    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CallString)) {
            return false;
        }
        CallString comp = (CallString) obj;

        return this.callNodeList.equals(comp.getCallNodeList());
    }

    public String dump() {
        StringBuilder b = new StringBuilder();
        for (Call callNode : this.callNodeList) {
            b.append(callNode.getFileName());
            b.append(":");
            b.append(callNode.getOrigLineno());
            b.append("\n");
        }
        return b.toString();
    }
}
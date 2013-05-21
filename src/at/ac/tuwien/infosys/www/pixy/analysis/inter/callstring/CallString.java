package at.ac.tuwien.infosys.www.pixy.analysis.inter.callstring;

import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgNodeCall;

import java.util.LinkedList;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class CallString {
    // a list of CfgNodeCall's; never longer than the k-size of the analysis
    private LinkedList<CfgNodeCall> callNodeList;

    // creates the empty call string
    public CallString() {
        this.callNodeList = new LinkedList<>();
    }

    // shall only be used by CallString.append
    private CallString(LinkedList<CfgNodeCall> callNodeList) {
        this.callNodeList = callNodeList;
    }

    public CallString append(CfgNodeCall callNode, int kSize) {
        LinkedList<CfgNodeCall> newList = new LinkedList<>(this.callNodeList);
        newList.add(callNode);
        if (newList.size() > kSize) {
            newList.remove(0);
        }
        return new CallString(newList);
    }

    // returns the last (rightmost) call node
    public CfgNodeCall getLast() {
        return this.callNodeList.getLast();
    }

    public LinkedList<CfgNodeCall> getCallNodeList() {
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
        for (CfgNodeCall callNode : this.callNodeList) {
            b.append(callNode.getFileName());
            b.append(":");
            b.append(callNode.getOrigLineno());
            b.append("\n");
        }
        return b.toString();
    }
}
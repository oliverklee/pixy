package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class TacClass {
    // node where the class definition starts
    private ParseNode parseNode;

    // the name of the class
    private String name;

    // method name -> TacFunction
    private Map<String, TacFunction> methods;

    // member name -> Pair(initializer controlFlowGraph, AbstractTacPlace that summarizes the controlFlowGraph)
    private Map<String, TacMember> members;

    TacClass(String name, ParseNode parseNode) {
        this.name = name;
        this.methods = new HashMap<>();
        this.members = new HashMap<>();
        this.parseNode = parseNode;
    }

    // if this class already contains a method with the given name,
    // false is returned
    boolean addMethod(String name, TacFunction function) {
        if (this.methods.get(name) == null) {
            this.methods.put(name, function);
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getFileName() {
        return this.parseNode.getFileName();
    }

    public String getLoc() {
        if (!MyOptions.optionB) {
            return this.parseNode.getFileName() + ":" + this.parseNode.getLinenoLeft();
        } else {
            return Utils.basename(this.parseNode.getFileName()) + ":" + this.parseNode.getLinenoLeft();
        }
    }

    public void addMember(String name, ControlFlowGraph controlFlowGraph, AbstractTacPlace place) {
        TacMember member = new TacMember(name, controlFlowGraph, place);
        this.members.put(name, member);
    }

// TacMember (private class) *******************************************************

    private class TacMember {

        // member name
        private String name;

        // initializer controlFlowGraph
        private ControlFlowGraph controlFlowGraph;

        // place that summarizes the initializer controlFlowGraph; e.g., if you have
        // a member declaration such as
        private AbstractTacPlace place;

        TacMember(String name, ControlFlowGraph controlFlowGraph, AbstractTacPlace place) {
            this.name = name;
            this.controlFlowGraph = controlFlowGraph;
            this.place = place;
        }
    }
}
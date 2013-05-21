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

    // member name -> Pair(initializer cfg, TacPlace that summarizes the cfg)
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

    public int getLine() {
        return this.parseNode.getLinenoLeft();
    }

    public String getLoc() {
        if (!MyOptions.optionB) {
            return this.parseNode.getFileName() + ":" + this.parseNode.getLinenoLeft();
        } else {
            return Utils.basename(this.parseNode.getFileName()) + ":" + this.parseNode.getLinenoLeft();
        }
    }

    public void addMember(String name, Cfg cfg, TacPlace place) {
        TacMember member = new TacMember(name, cfg, place);
        this.members.put(name, member);
    }

    public String dump() {
        StringBuilder b = new StringBuilder();
        b.append("Class ");
        b.append(this.name);
        b.append("\n");
        b.append("Functions:\n");
        for (String methodName : this.methods.keySet()) {
            b.append(methodName);
            b.append("\n");
        }
        b.append("Members:\n");
        for (TacMember member : this.members.values()) {
            b.append(member.dump());
        }
        b.append("\n");

        return b.toString();
    }

// TacMember (private class) *******************************************************

    private class TacMember {

        // member name
        private String name;

        // initializer cfg
        private Cfg cfg;

        // place that summarizes the initializer cfg; e.g., if you have
        // a member declaration such as
        private TacPlace place;

        TacMember(String name, Cfg cfg, TacPlace place) {
            this.name = name;
            this.cfg = cfg;
            this.place = place;
        }

        String getName() {
            return this.name;
        }

        Cfg getCfg() {
            return this.cfg;
        }

        TacPlace getPlace() {
            return this.place;
        }

        String dump() {
            StringBuilder b = new StringBuilder();
            b.append(this.name);
            b.append("\n");
            return b.toString();
        }
    }
}
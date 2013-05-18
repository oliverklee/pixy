package at.ac.tuwien.infosys.www.pixy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class contains meta-information about a DepClient.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepClientInfo {

    private String name;
    private String className;
    private boolean performMe;
    private Map<String, Set<Integer>> sinks;
    private FunctionModels functionModels;

    DepClientInfo(String name, String className) {
        this.name = name;
        this.className = className;
        this.performMe = false;
        this.sinks = new HashMap<>();
    }

    void addSinks(Map<String, Set<Integer>> addUs) {
        this.sinks.putAll(addUs);
    }

    public FunctionModels getFunctionModels() {
        return this.functionModels;
    }

    public void setFunctionModels(FunctionModels fm) {
        this.functionModels = fm;
    }

    public String getName() {
        return this.name;
    }

    public String getClassName() {
        return this.className;
    }

    public Map<String, Set<Integer>> getSinks() {
        return this.sinks;
    }

    public void setPerformMe(boolean b) {
        this.performMe = b;
    }

    public boolean performMe() {
        return this.performMe;
    }

    public boolean isModelled(String funcName) {
        return this.functionModels.isModelled(funcName);
    }
}
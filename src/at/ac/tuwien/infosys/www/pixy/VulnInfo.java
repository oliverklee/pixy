package at.ac.tuwien.infosys.www.pixy;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepGraph;

public class VulnInfo {

    // number of graphs that were initially considered
    private int initialGraphCount;

    // vulnerable depgraphs
    private List<DepGraph> depGraphs;

    // minimized depgraphs corresponding to depGraphs
    private List<DepGraph> depGraphsMin;

    private int totalPathCount;
    private int basicPathCount;
    private int customSanitCount;
    private int customSanitThrownAwayCount;

    // dangerous uninit nodes
    //private List<Map<DepGraphUninitNode, InitialTaint>> dangerousUninit;

    VulnInfo() {
        this.depGraphs = new LinkedList<DepGraph>();
        this.depGraphsMin = new LinkedList<DepGraph>();
        this.totalPathCount = 0;
        this.basicPathCount = 0;
        //this.dangerousUninit = new LinkedList<Map<DepGraphUninitNode, InitialTaint>>();
    }

    public List<DepGraph> getDepGraphs() {
        return this.depGraphs;
    }

    public List<DepGraph> getDepGraphsMin() {
        return this.depGraphsMin;
    }

    public void addDepGraph(DepGraph depGraph, DepGraph min
            /*, Map<DepGraphUninitNode, InitialTaint> dangerousUninit*/) {
        this.depGraphs.add(depGraph);
        this.depGraphsMin.add(min);
        //this.dangerousUninit.add(dangerousUninit);
    }

//  **********************************************************
// various counters

    public int getInitialGraphCount() {
        return initialGraphCount;
    }

    public void setInitialGraphCount(int initialGraphNum) {
        this.initialGraphCount = initialGraphNum;
    }

    public int getTotalPathCount() {
        return this.totalPathCount;
    }

    public int getBasicPathCount() {
        return this.basicPathCount;
    }

    public void setTotalPathCount(int count) {
        this.totalPathCount = count;
    }

    public void setBasicPathCount(int count) {
        this.basicPathCount = count;
    }

    public int getCustomSanitCount() {
        return customSanitCount;
    }

    public void setCustomSanitCount(int customSanitCount) {
        this.customSanitCount = customSanitCount;
    }



    public int getCustomSanitThrownAwayCount() {
        return customSanitThrownAwayCount;
    }

    public void setCustomSanitThrownAwayCount(int customSanitThrownAwayCount) {
        this.customSanitThrownAwayCount = customSanitThrownAwayCount;
    }
}
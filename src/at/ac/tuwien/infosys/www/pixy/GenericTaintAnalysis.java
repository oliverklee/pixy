package at.ac.tuwien.infosys.www.pixy;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.AnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.inter.InterWorkList;
import at.ac.tuwien.infosys.www.pixy.analysis.mod.ModAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;

// helper class to prevent code redundancy and confusion, used by Checker;
// perhaps there are more elegant ways to do this
public class GenericTaintAnalysis {

    private List<DepClient> depClients;

    public DepAnalysis depAnalysis;

//  ********************************************************************************

    private GenericTaintAnalysis() {
        this.depClients = new LinkedList<DepClient>();
    }

//  ********************************************************************************

    private void addDepClient(DepClient depClient) {
        this.depClients.add(depClient);
    }

//  ********************************************************************************

    // returns null if the given taintString is illegal
    static GenericTaintAnalysis createAnalysis(TacConverter tac,
            AnalysisType enclosingAnalysis, Checker checker,
            InterWorkList workList, ModAnalysis modAnalysis) {

        GenericTaintAnalysis gta = new GenericTaintAnalysis();

        gta.depAnalysis = new DepAnalysis(tac,
                checker.aliasAnalysis, checker.literalAnalysis, enclosingAnalysis,
                workList, modAnalysis);

        try {

            // each of the depclients will get the depAnalysis as parameter
            Class<?>[] argsClass = new Class<?>[] {
                    Class.forName("at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis")};
            Object[] args = new Object[] {gta.depAnalysis};

            // for each requested depclient...
            for (DepClientInfo dci : MyOptions.getDepClients()) {
                if (!dci.performMe()) {
                    continue;
                }
                Class<?> clientDefinition = Class.forName(dci.getClassName());
                Constructor constructor = clientDefinition.getConstructor(argsClass);
                DepClient depClient = (DepClient) constructor.newInstance(args);
                gta.addDepClient(depClient);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return gta;
    }

//  ********************************************************************************

    void analyze() {
        this.depAnalysis.analyze();

        // check for unreachable code
        this.depAnalysis.checkReachability();
    }

//  ********************************************************************************

    List<Integer> detectVulns() {
        List<Integer> retMe = new LinkedList<Integer>();
        for (DepClient depClient : this.depClients) {
            retMe.addAll(depClient.detectVulns());
        }
        return retMe;
    }

//  ********************************************************************************

    List<DepClient> getDepClients() {
        return this.depClients;
    }
}
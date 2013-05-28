package at.ac.tuwien.infosys.www.pixy;

import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.globalsmodification.GlobalsModificationAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.AbstractAnalysisType;
import at.ac.tuwien.infosys.www.pixy.analysis.interprocedural.InterproceduralWorklist;
import at.ac.tuwien.infosys.www.pixy.conversion.TacConverter;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a helper class to prevent code redundancy and confusion.
 *
 * It is used in the Checker class.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class GenericTaintAnalysis {
    private List<AbstractVulnerabilityAnalysis> dependencyClients;

    public DependencyAnalysis dependencyAnalysis;

//  ********************************************************************************

    private GenericTaintAnalysis() {
        this.dependencyClients = new LinkedList<>();
    }

//  ********************************************************************************

    private void addDepClient(AbstractVulnerabilityAnalysis dependencyClient) {
        this.dependencyClients.add(dependencyClient);
    }

//  ********************************************************************************

    // returns null if the given taintString is illegal
    static GenericTaintAnalysis createAnalysis(TacConverter tac,
                                               AbstractAnalysisType enclosingAnalysis, Checker checker,
                                               InterproceduralWorklist workList, GlobalsModificationAnalysis globalsModificationAnalysis) {

        GenericTaintAnalysis gta = new GenericTaintAnalysis();

        gta.dependencyAnalysis = new DependencyAnalysis(tac,
            checker.aliasAnalysis, checker.literalAnalysis, enclosingAnalysis,
            workList, globalsModificationAnalysis);

        try {

            // each of the depclients will get the dependencyAnalysis as parameter
            Class<?>[] argsClass = new Class<?>[]{
                Class.forName("at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis")};
            Object[] args = new Object[]{gta.dependencyAnalysis};

            // for each requested depclient...
            for (DependencyClientInformation dci : MyOptions.getDepClients()) {
                if (!dci.performMe()) {
                    continue;
                }
                Class<?> clientDefinition = Class.forName(dci.getClassName());
                Constructor<?> constructor = clientDefinition.getConstructor(argsClass);
                AbstractVulnerabilityAnalysis dependencyClient = (AbstractVulnerabilityAnalysis) constructor.newInstance(args);
                gta.addDepClient(dependencyClient);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return gta;
    }

//  ********************************************************************************

    void analyze() {
        this.dependencyAnalysis.analyze();

        // check for unreachable code
        this.dependencyAnalysis.checkReachability();
    }

//  ********************************************************************************

    List<Integer> detectVulns() {
        List<Integer> retMe = new LinkedList<>();
        for (AbstractVulnerabilityAnalysis dependencyClient : this.dependencyClients) {
            retMe.addAll(dependencyClient.detectVulns());
        }
        return retMe;
    }

//  ********************************************************************************

    List<AbstractVulnerabilityAnalysis> getDependencyClients() {
        return this.dependencyClients;
    }
}
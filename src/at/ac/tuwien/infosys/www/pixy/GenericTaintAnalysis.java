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
    private List<AbstractVulnerabilityAnalysis> abstractVulnerabilityAnalyses;

    public DependencyAnalysis dependencyAnalysis;

    private GenericTaintAnalysis() {
        this.abstractVulnerabilityAnalyses = new LinkedList<>();
    }

    private void addDepClient(AbstractVulnerabilityAnalysis dependencyClient) {
        this.abstractVulnerabilityAnalyses.add(dependencyClient);
    }

    /**
     * Returns null if the given taintString is illegal.
     *
     * @param tac
     * @param enclosingAnalysis
     * @param checker
     * @param workList
     * @param globalsModificationAnalysis
     *
     * @return
     */
    static GenericTaintAnalysis createAnalysis(
        TacConverter tac, AbstractAnalysisType enclosingAnalysis, Checker checker, InterproceduralWorklist workList,
        GlobalsModificationAnalysis globalsModificationAnalysis
    ) {
        GenericTaintAnalysis genericTaintAnalysis = new GenericTaintAnalysis();

        genericTaintAnalysis.dependencyAnalysis = new DependencyAnalysis(
            tac, checker.aliasAnalysis, checker.literalAnalysis, enclosingAnalysis, workList, globalsModificationAnalysis
        );

        try {
            // each of the VulnerabilityAnalysis will get the dependencyAnalysis as parameter
            Class<?>[] argumentsClass = new Class<?>[] {
                Class.forName("at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyAnalysis")
            };
            Object[] arguments = new Object[]{genericTaintAnalysis.dependencyAnalysis};

            // for each requested VulnerabilityAnalysis ...
            for (VulnerabilityAnalysisInformation analysisInformation : MyOptions.getVulnerabilityAnalyses()) {
                if (!analysisInformation.performMe()) {
                    continue;
                }
                Class<?> clientDefinition = Class.forName(analysisInformation.getClassName());
                Constructor<?> constructor = clientDefinition.getConstructor(argumentsClass);
                AbstractVulnerabilityAnalysis dependencyClient = (AbstractVulnerabilityAnalysis) constructor.newInstance(arguments);
                genericTaintAnalysis.addDepClient(dependencyClient);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return genericTaintAnalysis;
    }

    void analyze() {
        this.dependencyAnalysis.analyze();

        // check for unreachable code
        this.dependencyAnalysis.checkReachability();
    }

    /**
     * Detects vulnerabilities and returns a list with the line numbers of the detected vulnerabilities.
     *
     * @return the line numbers of the detected vulnerabilities
     */
    List<Integer> detectVulnerabilities() {
        List<Integer> lineNumbersOfVulnerabilities = new LinkedList<>();
        for (AbstractVulnerabilityAnalysis dependencyClient : this.abstractVulnerabilityAnalyses) {
            lineNumbersOfVulnerabilities.addAll(dependencyClient.detectVulnerabilities());
        }

        return lineNumbersOfVulnerabilities;
    }

    List<AbstractVulnerabilityAnalysis> getAbstractVulnerabilityAnalyses() {
        return this.abstractVulnerabilityAnalyses;
    }
}
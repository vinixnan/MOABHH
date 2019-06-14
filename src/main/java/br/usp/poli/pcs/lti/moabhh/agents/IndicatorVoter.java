package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.DoubleTaggedSolution;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.TaggedSolution;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.IndicatorFactory;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.*;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.Calculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.extrametrics.DummyAE;
import br.usp.poli.pcs.lti.jmetalproblems.interfaces.RealWorldProblem;
import br.usp.poli.pcs.lti.moabhh.util.ReferencePointUtils;

import cartago.AgentId;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.Op;
import cartago.OpFeedbackParam;
import com.google.common.primitives.Doubles;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;
import org.uma.jmetal.problem.DoubleProblem;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.front.imp.ArrayFront;


/**
 * The type Indicator voter.
 *
 * @param <S> the type parameter
 */
public class IndicatorVoter<S extends Solution<?>> extends SimplerAgent {

    protected final String indicatorName;
    protected String[] arms;
    protected Calculator qualityIndicator;
    protected ArtifactId problemArtifactId;
    protected ArtifactId votingArtifactId;
    protected double[] nadirPoints;
    protected double[] idealPoints;
    protected List<S> previousPopulation;
    protected int normalizationVar=1;

    /**
     * Instantiates a new Indicator voter.
     *
     * @param indicatorName the indicator name
     * @param uId
     * @param seed
     */
    public IndicatorVoter(String indicatorName, long uId, long seed) {
        super("iVoter_" + indicatorName, uId, seed);
        this.indicatorName = indicatorName;
        this.qualityIndicator = null;
        previousPopulation = null;
    }

    /**
     * Instantiates a new Indicator voter.
     *
     * @param indicatorName the indicator name
     * @param agentName
     * @param uId
     * @param seed
     */
    public IndicatorVoter(String indicatorName, String agentName, long uId, long seed) {
        super("iVoter_" + agentName, uId, seed);
        this.indicatorName = indicatorName;
        this.qualityIndicator = null;
        previousPopulation = null;
    }
    
    protected void findReference(Problem problem, List<S> pop){
        pop.forEach((s) -> {
            for (int i = 0; i < s.getNumberOfObjectives(); i++) {
                nadirPoints[i] = Math.max(nadirPoints[i], s.getObjective(i));
                idealPoints[i] = Math.min(idealPoints[i], s.getObjective(i));
            }
        });
    }

    public Solution[] findNadir(DoubleProblem problem, List<S> pop) {
        findReference(problem, pop);
        Solution[] toReturn=new Solution[problem.getNumberOfObjectives()];
        for (int k = 0; k < problem.getNumberOfObjectives(); k++) {
            Solution s = new DoubleTaggedSolution(problem);
            for (int i = 0; i < s.getNumberOfObjectives(); i++) {
                if(k==i){
                    s.setObjective(i, nadirPoints[i]);
                }
                else{
                    s.setObjective(i, 0);
                }
            }
            toReturn[k]=s;
        }
        return toReturn;
    }

    @Override
    public void init() {
        super.init();
        try {
            problemArtifactId = lookupArtifact("problem");
            votingArtifactId = lookupArtifact("voting");
            Problem problem = (Problem) this.getAttributeArtifact(problemArtifactId, "getProblem_");
            nadirPoints = new double[problem.getNumberOfObjectives()];
            idealPoints = new double[problem.getNumberOfObjectives()];
            int populationSize = (int) this.getAttributeArtifact(problemArtifactId, "getQtdPopulation");
            this.qualityIndicator = IndicatorFactory
                    .buildCalculator(indicatorName, problem, populationSize);
            //this.qualityIndicator.setParetoTrueFront(new ArrayFront("pareto_fronts/simpleRef.pf"));//using the same reference as maashi
            //when use IGD GD R remove it
            ArrayList<AgentId> agids = this.getMhAgentIds();
            this.arms = new String[agids.size()];
            for (int i = 0; i < agids.size(); i++) {
                this.arms[i] = agids.get(i).getAgentName();
            }
        } catch (CartagoException | IOException ex) {
            Logger.getLogger(IndicatorVoter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        int epsilon = (int) this.getAttributeArtifact(problemArtifactId, "getEpsilon");
        Problem problem = (Problem) this.getAttributeArtifact(problemArtifactId, "getProblem_");
        while (true) {
            try {
                int algStep = (int) this.getAttributeArtifact(problemArtifactId, "getAlgStep");
                OpFeedbackParam<Object> res = new OpFeedbackParam<>();
                doAction(problemArtifactId, new Op("isThisIndicatorVoterExecuted", this.id, res));
                Boolean alreadyVoted = (Boolean) res.get();
                double[] qualityValues = new double[arms.length];
                if (algStep == 0 && !alreadyVoted) {
                    List<S> currentPopulation = this.getPopulationToEvaluate();
                    int[] qtds = (int[]) this.getAttributeArtifact(votingArtifactId, "getQtdOfSolutions");
                    double[] qtdSolutions = new double[qtds.length];
                    for (int i = 0; i < qtds.length; i++) {
                        qtdSolutions[i] = qtds[i];
                    }
                    int popSize = (int) this.getAttributeArtifact(problemArtifactId, "getQtdPopulation");
                    double beta = (double) this.getAttributeArtifact(problemArtifactId, "getBeta");
                    if (StatUtils.max(qtdSolutions) == popSize && StatUtils.sum(qtdSolutions) == popSize) {
                        int maxPos = Doubles.indexOf(qtdSolutions, Doubles.max(qtdSolutions));
                        qualityValues[maxPos] = 1;
                    } else {
                        //System.out.println(this.getAgentName()+" votes");
                        int gen = (int) this.getAttributeArtifact(problemArtifactId, "getIteration");
                        if (useObtainedReferencePoint(problem)) {
                            findReference(problem, currentPopulation);
                        }
                        else{
                            currentPopulation=this.processingForBenchmark(currentPopulation);
                            ReferencePointUtils.findReference(problem.getName(), problem.getNumberOfObjectives());
                            nadirPoints=ReferencePointUtils.nadir;
                            idealPoints=ReferencePointUtils.ideal;
                        }
                        this.qualityIndicator.setMinMax(idealPoints, nadirPoints);
                        //System.out.println(this.getAgentName()+" gets population "+currentPopulation.size());
                        List<List<S>[]> allsharestype = this.separatePopulationByOp(currentPopulation);
                        List<S>[] shares;
                        if (this.qualityIndicator instanceof HypervolumeCalculator) {
                            shares = allsharestype.get(0);//no meio da execucao, se tiver poucas solucoes, vai zerar, usar 2 diminui o impacto
                        } else {
                            shares = allsharestype.get(2);
                        }
                        qualityValues = performEvaluation(problem, currentPopulation, shares, qtdSolutions, epsilon, popSize);
                    }
                    previousPopulation = currentPopulation;
                    res = new OpFeedbackParam<>();
                    //System.out.println(this.getAgentName() + " votes " + Arrays.toString(qualityValues));
                    doAction(votingArtifactId, new Op("vote", qualityValues, this.id, beta, res));
                    doAction(problemArtifactId, new Op("setAlreadyVoted", this.id));
                }
            } catch (CartagoException ex) {
                Logger.getLogger(IndicatorVoter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IndicatorVoter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected double[] performEvaluation(Problem problem, List<S> currentPopulation, List<S>[] shares, double[] qtdSolutions, int epsilon, int popSize) throws CartagoException {
        double[] qualityValues = new double[arms.length];
        OpFeedbackParam<Object> res;
        if(this.qualityIndicator instanceof DummyAE){
            for (int i = 0; i < arms.length; i++) {
                ((DummyAE)qualityIndicator).setAlgName(arms[i].split("_")[0]);
                qualityValues[i] = qualityIndicator.calculate(null, null, null);
            }
        }
        else if (this.qualityIndicator instanceof AlgorithmEffort) {
            for (int i = 0; i < qualityValues.length; i++) {
                res = new OpFeedbackParam<>();
                doAction(problemArtifactId, new Op("getBeginTime", arms[i], res));
                if(res.get()!=null){
                    long beginTime = (long) res.get();
                    res = new OpFeedbackParam<>();
                    doAction(problemArtifactId, new Op("getEndTime", arms[i], res));
                    if(res.get()!=null){
                        long endTime = (long) res.get();
                        qualityValues[i] = ((AlgorithmEffort) this.qualityIndicator).execute(endTime - beginTime, (int) (qtdSolutions[i] * epsilon));
                    }
                }
            }
            double maxval=Double.MAX_EXPONENT*Math.E;
            for (int i = 0; i < qualityValues.length; i++) {
                if (qualityValues[i] != 0.0) {//0 is impossible, thats just occur when there is notthing to evaluate
                    qualityValues[i] = maxval - qualityValues[i];
                }
            }
        } else if (this.qualityIndicator instanceof RniCalculator) {
            for (int i = 0; i < qualityValues.length; i++) {
                double quality = 0;
                if (shares[i].size() > 0) {
                    //int size = (int) qtdSolutions[i];
                    ((RniCalculator) this.qualityIndicator).setPopulationMaxSize(popSize);
                    quality = this.qualityIndicator.execute(shares[i]);
                }
                qualityValues[i] = quality;
            }
        } else if (this.qualityIndicator instanceof NRCalculator) {
            for (int i = 0; i < qualityValues.length; i++) {
                double quality = 0;
                if (shares[i].size() > 0) {
                    quality = ((NRCalculator) this.qualityIndicator).calculate(shares[i], SolutionListUtils.getNondominatedSolutions(currentPopulation));
                }
                qualityValues[i] = quality;
            }
        } else if (this.qualityIndicator instanceof HypervolumeRatioCalculator) {
            for (int i = 0; i < qualityValues.length; i++) {
                double quality = 0;
                if (shares[i].size() > 0) {
                    quality = ((HypervolumeRatioCalculator) this.qualityIndicator).calculate(new ArrayFront(shares[i]), new ArrayFront(currentPopulation));
                }
                qualityValues[i] = quality;
            }
        } else {
            if (this.qualityIndicator.isLowerValuesAreBetter()) {
                for (int i = 0; i < qualityValues.length; i++) {
                    double quality = this.normalizationVar;
                    if (shares[i].size() > 0) {
                        quality = this.qualityIndicator.execute(shares[i]);
                    }
                    qualityValues[i] = (this.normalizationVar - quality)/this.normalizationVar;
                }
            } else {
                for (int i = 0; i < qualityValues.length; i++) {
                    double quality = 0;
                    if (shares[i].size() > 0) {
                        quality = this.qualityIndicator.execute(shares[i]);
                    }
                    qualityValues[i] = quality;
                }
            }
        }
        return qualityValues;
    }

    protected List<S>[] splitPopulation(List<S> population) {
        List<S>[] separatedSolutionSet;
        separatedSolutionSet = new ArrayList[arms.length];
        for (int i = 0; i < arms.length; i++) {
            separatedSolutionSet[i] = new ArrayList();
        }
        for (int i = 0; i < population.size(); i++) {
            Solution solution = population.get(i);
            if (solution instanceof TaggedSolution) {
                TaggedSolution ts = (TaggedSolution) solution;
                if (ts.getAlgorithm() != null) {
                    boolean found = false;
                    for (int j = 0; !found && j < arms.length; j++) {
                        String act = arms[j].replace(String.valueOf(uId), "").replace("_", "");
                        if (ts.getAlgorithm().getName().equalsIgnoreCase(act)) {
                            separatedSolutionSet[j].add((S) ts);
                            found = true;
                        }
                    }
                }
            }
        }
        return separatedSolutionSet;
    }

    /**
     * Separate population by op list [ ].
     *
     * @param completePopulation
     * @return the list [ ]
     */
    protected List<List<S>[]> separatePopulationByOp(List<S> completePopulation) {
        //Collections.shuffle(completePopulation);
        List<S> population = SolutionListUtils.getNondominatedSolutions(completePopulation);
        List<S>[] separatedSolutionSetJustNonDominated = this.splitPopulation(population);
        List<S>[] separatedSolutionSetAll = this.splitPopulation(completePopulation);
        List<S>[] separatedSolutionSetFull = this.splitPopulation(completePopulation);
        for (int i = 0; i < arms.length; i++) {
            NonDominatedSolutionListArchive ndominated = new NonDominatedSolutionListArchive();
            separatedSolutionSetAll[i].forEach((s) -> {
                ndominated.add(s);
            });
            separatedSolutionSetAll[i] = ndominated.getSolutionList();
        }
        List<List<S>[]> toReturn = new ArrayList<>();
        toReturn.add(separatedSolutionSetJustNonDominated);
        toReturn.add(separatedSolutionSetAll);
        toReturn.add(separatedSolutionSetFull);
        return toReturn;
    }

    protected List<S> getPopulationToEvaluate() throws CartagoException {
        ArtifactId populationArtifactId = lookupArtifact("population");
        List<S> currentPopulation = (List<S>)getAttributeArtifact(populationArtifactId, "getPopulation");
        List<S> offspring = (List<S>)getAttributeArtifact(populationArtifactId, "getOffspring");
        List<S> toReturn=new ArrayList<>();
        toReturn.addAll(currentPopulation);
        toReturn.addAll(offspring);
        return toReturn;
    }

    protected List<S> processingForBenchmark(List<S> currentPopulation) {
        //just to overhide
        return currentPopulation;
    }

    protected boolean useObtainedReferencePoint(Problem problem) {
        //necessary for overhide
        return problem instanceof RealWorldProblem;
    }
}

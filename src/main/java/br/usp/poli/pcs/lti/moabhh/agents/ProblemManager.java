package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.IgdCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.RniCalculator;
import br.usp.poli.pcs.lti.jmetalproblems.interfaces.RealWorldProblem;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;


import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.Op;
import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * The type Problem manager.
 *
 * @param <S> the type parameter
 */
@SuppressWarnings("serial")
public class ProblemManager<S extends Solution<?>> extends SimplerAgent {

    protected final Problem problem;
    protected final int qtdPopulation;
    protected final int maxIterations;
    protected final int delta;
    protected final int epsilon;
    protected final int beta;
    protected final VotingMethod votingmethod;
    protected final int executionCounter;
    protected String fileNameAppendix;
    protected int typeOfReward;

    /**
     * Instantiates a new Problem manager.
     *
     * @param problem the problem
     * @param uId
     * @param qtdPopulation the qtd population
     * @param maxIterations the max iterations
     * @param beta the beta
     * @param delta the delta
     * @param epsilon the epsilon
     * @param votingmethod
     * @param executionCounter
     */
    public ProblemManager(Problem problem, long uId, int qtdPopulation, int maxIterations, int beta, int delta,
            int epsilon, VotingMethod votingmethod, int executionCounter, long seed) {
        super("ProblemManager", uId, seed);
        this.problem = problem;
        this.qtdPopulation = qtdPopulation;
        this.maxIterations = maxIterations;
        this.delta = delta;
        this.epsilon = epsilon;
        this.beta = beta;
        this.votingmethod = votingmethod;
        this.executionCounter = executionCounter;
        fileNameAppendix="";
        typeOfReward=0;
    }

    public String getFileNameAppendix() {
        return fileNameAppendix;
    }

    public void setFileNameAppendix(String fileNameAppendix) {
        this.fileNameAppendix = fileNameAppendix;
    }

    protected ArtifactId getSystemVariablesArtifact() throws CartagoException {
        ArtifactId idProblem = makeArtifact("problem",
                "br.usp.poli.pcs.lti.moabhh.artifacts.SystemVariablesArtifact");
        return idProblem;
    }

    /**
     * Init artifacts.
     */
    public void initArtifacts() {

        try {
            //use jcommander here, no more params
            ArtifactId idProblem = this.getSystemVariablesArtifact();
            Op op = new Op("setProblem_", problem);
            doAction(idProblem, op);
            op = new Op("setMaxIterations", maxIterations);
            doAction(idProblem, op);
            op = new Op("setQtdPopulation", qtdPopulation);
            doAction(idProblem, op);

            op = new Op("setDelta", delta);//@TODO params 112
            doAction(idProblem, op);

            op = new Op("setEpsilon", epsilon);//@TODO params
            doAction(idProblem, op);

            op = new Op("setBeta", beta);//@TODO params
            doAction(idProblem, op);

            ArtifactId idPopulation = makeArtifact("population",
                    "br.usp.poli.pcs.lti.moabhh.artifacts.PopulationArtifact");
            op = new Op("init", qtdPopulation);
            doAction(idPopulation, op);
            JMetalRandom.getInstance().setSeed(randomGenerator.getSeed());
            op = new Op("initPopulation", problem, qtdPopulation);
            doAction(idPopulation, op);

        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Init artifacts second part.
     */
    public void initArtifactsSecondPart() {
        try {
            ArtifactId idProblem = lookupArtifact("problem");
            doAction(idProblem, new Op("generateAlreadyVoted", this.getVoterAgentIds()));
            doAction(idProblem, new Op("generateAlreadyExecuted", this.getMhAgentIds()));
            
            ArtifactId idPopulationShare = makeArtifact("populationshare",
                    "br.usp.poli.pcs.lti.moabhh.artifacts.PopulationShareArtifact");
            Op op = new Op("init", this.getMhAgentIds());
            doAction(idPopulationShare, op);

            ArtifactId idVoting = makeArtifact("voting",
                    "br.usp.poli.pcs.lti.moabhh.artifacts.CopelandArtifact");
            doAction(idVoting, new Op("generateAlreadyVoted", this.getVoterAgentIds()));
            doAction(idVoting, new Op("setVotingMethod", this.votingmethod));
        } catch (CartagoException ex) {
            Logger.getLogger(ProblemManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected List getResultingPopulation() throws CartagoException {
        ArtifactId populationArtifactId = lookupArtifact("population");
        List<S> currentPopulation = (List<S>) this
                .getAttributeArtifact(populationArtifactId, "getPopulation");
        return currentPopulation;
    }

    @Override
    public void run() {
        long begin = System.currentTimeMillis();
        while (true) {
            try {
                ArtifactId problemArtifactId = lookupArtifact("problem");
                int maxIterations = (int) this.getAttributeArtifact(problemArtifactId, "getMaxIterations");
                int iterations = (int) this.getAttributeArtifact(problemArtifactId, "getIteration");
                //System.out.println(this.getAgentName()+" "+iterations+" of "+maxIterations);

                if (iterations >= maxIterations) {
                    //System.out.println("Saida");
                    List<S> currentPopulation = this.getResultingPopulation();
                    //System.out.println(currentPopulation.size());
                    long end = System.currentTimeMillis();
                    ArrayList<int[]> allvotes = (ArrayList<int[]>) this.getAttributeArtifact(problemArtifactId, "getAllVotes");
                    int[] firstPlace = new int[allvotes.size()];
                    int[] secondPlace = new int[allvotes.size()];
                    int[] thirdPlace = new int[allvotes.size()];
                    int[] fourthPlace = new int[allvotes.size()];
                    int[] fiftthPlace = new int[allvotes.size()];
                    Arrays.fill(firstPlace, -1);
                    Arrays.fill(secondPlace, -1);
                    Arrays.fill(thirdPlace, -1);
                    Arrays.fill(fourthPlace, -1);
                    Arrays.fill(fiftthPlace, -1);
                    
                    for (int i = 0; i < allvotes.size(); i++) {
                        int[] aux = allvotes.get(i);
                        firstPlace[i] = aux[0];
                        if(aux.length > 1){
                            secondPlace[i] = aux[1];
                        }
                        if(aux.length > 2){
                            thirdPlace[i] = aux[2];
                        }
                        if (aux.length > 3) {
                            fourthPlace[i] = aux[3];
                        }
                        if (aux.length > 4) {
                            fiftthPlace[i] = aux[4];
                        }
                    }
                    
                    //System.out.println("HH;" + this.printResults(currentPopulation, problem) + ";" + (end - begin));
                    System.out.println("HH;"  + (end - begin));
                    System.err.println(Arrays.toString(firstPlace).replace(", ", ";").replace("[", "").replace("]", ""));
                    System.err.println(Arrays.toString(secondPlace).replace(", ", ";").replace("[", "").replace("]", ""));
                    System.err.println(Arrays.toString(thirdPlace).replace(", ", ";").replace("[", "").replace("]", ""));
                    System.err.println(Arrays.toString(fourthPlace).replace(", ", ";").replace("[", "").replace("]", ""));
                    System.err.println(Arrays.toString(fiftthPlace).replace(", ", ";").replace("[", "").replace("]", ""));
                    System.exit(0);
                    //this.initArtifacts();
                    //this.initArtifactsSecondPart();
                }

            } catch (CartagoException ex) {
                Logger.getLogger(ProblemManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Print results string.
     *
     * @param currentPopulation the current population
     * @param problem the problem
     * @return the string
     * @throws FileNotFoundException the file not found exception
     */
    public String printResults(List<S> currentPopulation, Problem problem)
            throws FileNotFoundException {
        List<S> archive = this.generateNonDominated(currentPopulation, problem);
        String strToReturn = "";
        if(!(problem instanceof RealWorldProblem)){
            //just for benchmarks
            if (problem.getName().contains("WFG")) {
                String pf
                        = "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
                HypervolumeCalculator hyp = new HypervolumeCalculator(problem.getNumberOfObjectives(), pf);
                hyp.updatePointsUsingNadir(this.getNadir(problem.getName(), problem.getNumberOfObjectives()));

                double hypValue = hyp.execute(archive);
                RniCalculator rni = new RniCalculator(problem.getNumberOfObjectives(), currentPopulation.size(),
                        pf);
                double rniValue = rni.execute(archive);
                IgdCalculator igd = new IgdCalculator(problem.getNumberOfObjectives(), pf);
                double igdValue = igd.execute(archive);
                strToReturn = hypValue + ";" + igdValue + ";" + rniValue;
            } else {
                String pf
                        = "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
                if (problem.getName().equals("UF")) {
                    pf
                            = "pareto_fronts/" + problem.getClass().getSimpleName() + "." + problem.getNumberOfObjectives() + "D.pf";
                }
                HypervolumeCalculator hyp = new HypervolumeCalculator(problem.getNumberOfObjectives(), pf);

                double hypValue = hyp.execute(archive);
                RniCalculator rni = new RniCalculator(problem.getNumberOfObjectives(), currentPopulation.size(),
                        pf);
                double rniValue = rni.execute(archive);
                IgdCalculator igd = new IgdCalculator(problem.getNumberOfObjectives(), pf);
                double igdValue = igd.execute(archive);
                strToReturn = hypValue + ";" + igdValue + ";" + rniValue;
            }
        }
        String dir = "result/MOABHH_"+beta+"_"+epsilon+"_"+delta+"_"+fileNameAppendix+"/" + problem.getName() + "_" + problem.getNumberOfObjectives();
        new File(dir).mkdirs();
        String funFile = dir + "/FUN" + executionCounter + ".tsv";
        String varFile = dir + "/VAR" + executionCounter + ".tsv";
        new SolutionListOutput(archive)
                .setSeparator("\t")
                .setFunFileOutputContext(new DefaultFileOutputContext(funFile))
                .setVarFileOutputContext(new DefaultFileOutputContext(varFile))
                .print();
        return strToReturn;
    }

    /**
     * Generate non dominated list.
     *
     * @param population the population
     * @return the list
     */
    public List<S> generateNonDominated(List<S> population) {
        return SolutionListUtils.getNondominatedSolutions(population);
    }

    /**
     * Generate non dominated list.
     *
     * @param population the population
     * @param problem
     * @return the list
     */
    public List<S> generateNonDominated(List<S> population, Problem problem) {
        if (problem.getName().contains("WFG")) {
            //double[] nadir = this.getNadir(problem.getName(), problem.getNumberOfObjectives());
            //List aux = this.removeWorseThanNadir(population, nadir, problem.getNumberOfObjectives());
            List aux=population;
            return SolutionListUtils.getNondominatedSolutions(aux);
        }
        return SolutionListUtils.getNondominatedSolutions(population);
    }

    protected List removeWorseThanNadir(List population, double[] nadir, int m) {
        List newpopulation = new ArrayList();
        for (Object o : population) {
            Solution s = (Solution) o;
            boolean stillOk = true;
            for (int i = 0; i < m && stillOk; i++) {
                if (nadir[i] < s.getObjective(i)) {
                    //System.err.println(nadir[i]+" menor "+s.getObjective(i));
                    stillOk = false;
                }
            }
            if (stillOk) {
                newpopulation.add(s);
            }
        }
        return newpopulation;
    }

    protected double[] getNadir(String problemName, int m) {
        double[] nadir = new double[m];
        if (problemName.contains("WFG")) {
            double[] base = new double[]{3.0, 5.0, 7.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0, 39.0, 41.0, 43.0};
            nadir = Arrays.copyOf(base, m);
        } else if (problemName.contains("DTLZ1") && m == 3) {
            Arrays.fill(nadir, 1.0);
        } else if (problemName.contains("DTLZ1")) {
            Arrays.fill(nadir, 10.0);
        } else if (problemName.contains("DTLZ") && m == 3) {
            Arrays.fill(nadir, 2.0);
        } else if (problemName.contains("DTLZ")) {
            Arrays.fill(nadir, 20.0);
        }
        return nadir;
    }
}

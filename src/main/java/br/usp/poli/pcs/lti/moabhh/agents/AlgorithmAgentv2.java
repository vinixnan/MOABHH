package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.jmetalhhhelper.imp.algs.GDE3;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforAlgorithm;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforHeuristics;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.interfaces.ArchivedLLHInterface;
import br.usp.poli.pcs.lti.jmetalhhhelper.imp.algs.SPEA2;
import br.usp.poli.pcs.lti.jmetalhhhelper.imp.algs.mIBEA;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.AlgorithmBuilder;

import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.Op;
import cartago.OpFeedbackParam;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.interfaces.LLHInterface;
import br.usp.poli.pcs.lti.jmetalproblems.interfaces.RealWorldProblem;

/**
 * The type Algorithm agent.
 *
 * @param <S> the type parameter
 */
@SuppressWarnings("serial")
public class AlgorithmAgentv2<S extends Solution<?>> extends SimplerAgent {

    protected LLHInterface alg;
    protected ArtifactId problemArtifactId;
    protected ArtifactId populationArtifactId;
    protected ArtifactId populationShareArtifactId;
    protected boolean hasPopulationAssigned;
    protected boolean increasedPopulation;
    protected ParametersforHeuristics pHeur;
    protected ParametersforAlgorithm paramAlg;
    protected AlgorithmBuilder hb;
    protected Problem problem;
    /**
     * The Copeland artifact.
     */
    protected ArtifactId copelandArtifact;

    /**
     * Instantiates a new Algorithm agent.
     *
     * @param agentName the agent name
     * @param uId
     * @param algConfigFileName the alg config file name
     * @param parameterHeuristic the parameter heuristic
     * @param seed
     */
    public AlgorithmAgentv2(String agentName, long uId, String algConfigFileName, String parameterHeuristic, long seed) {
        super(agentName, uId, seed);
        try {
            problemArtifactId = lookupArtifact("problem");
            populationArtifactId = lookupArtifact("population");
            problem = (Problem) this.getAttributeArtifact(problemArtifactId, "getProblem_");
            int populationSize = (int) this.getAttributeArtifact(problemArtifactId, "getQtdPopulation");
            int maxIterations = (int) this.getAttributeArtifact(problemArtifactId, "getMaxIterations");
            paramAlg = new ParametersforAlgorithm(algConfigFileName);
            paramAlg.setMaxIteractions(maxIterations);
            paramAlg.setPopulationSize(populationSize);
            paramAlg.setArchiveSize(populationSize);
            pHeur = new ParametersforHeuristics(parameterHeuristic, problem.getNumberOfVariables());
            if(seed==-1){
                hb = new AlgorithmBuilder(problem);
            }
            else{
                hb = new AlgorithmBuilder(problem, () -> randomGenerator.nextDouble());
            }
        } catch (ConfigurationException | CartagoException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected List<S> warrantySize(List<S> pop, int size){
        boolean toPrint=false;
        int from=size;
        while(pop.size() < size){
            int chosen = randomGenerator.nextInt(0, pop.size() - 1);
            pop.add((S) pop.get(chosen).copy());
            toPrint=true;
        }
        if(toPrint){
            System.out.println("Increased "+from+" to "+size+" "+alg.getClass().getSimpleName());
        }
        //System.out.println("Run "+alg.getClass().getSimpleName());
        return pop;
    }
    
    protected void selectAlg(List<S> population) {
        try {
            alg = hb.create(paramAlg, pHeur);
            population=warrantySize(population, paramAlg.getPopulationSize());
            this.alg.setPopulationSize(population.size());
            this.alg.initMetaheuristic(population);
        } catch (JMException | FileNotFoundException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void selectAlg(List<S> population, List<S> offspring, int size) {
        try {
            alg = hb.create(paramAlg, pHeur);
            if (this.alg instanceof SPEA2) {
                ((SPEA2) alg).setEnvironmentalSelectionSize(size);
            }
            this.alg.setPopulationSize(size);
            this.alg.initMetaheuristic(population, offspring);
        } catch (JMException | FileNotFoundException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void runMOABHHPrepare(int iteration) {

        try {
            OpFeedbackParam<Object> res = new OpFeedbackParam<>();
            Op op = new Op("getPopulationShare", this.id, res);
            doAction(populationShareArtifactId, op);
            List<S> currentPopulation = (List<S>) res.get();

            op = new Op("getOffspringShare", this.id, res);
            doAction(populationShareArtifactId, op);
            List<S> offspring = (List<S>) res.get();

            //List<S> fullPop = SolutionListUtils.getNondominatedSolutions(currentPopulation);//VER
            res = new OpFeedbackParam<>();
            op = new Op("getQtdOfSolutionsForId", this.id, res);
            doAction(copelandArtifact, op);
            int size = (int) (res.get());
            //System.out.println(this.id+" "+size);
            this.hasPopulationAssigned = size > 0;
            if ((this.hasPopulationAssigned && size % 2 != 0) && !(alg instanceof mIBEA)) {
                this.increasedPopulation = true;
                int chosen = randomGenerator.nextInt(0, currentPopulation.size() - 1);
                currentPopulation.add((S) currentPopulation.get(chosen).copy());
                offspring.add((S) currentPopulation.get(chosen).copy());
                size++;
            } else {
                this.increasedPopulation = false;
            }
            if (hasPopulationAssigned) {
                paramAlg.setArchiveSize(size);
                paramAlg.setPopulationSize(size);
                this.selectAlg(currentPopulation);
                if (this.alg instanceof ArchivedLLHInterface) {
                    ((ArchivedLLHInterface) alg).setPopulationSize(size);
                    ((ArchivedLLHInterface) alg).setArchive(currentPopulation);//real difference here
                    ((ArchivedLLHInterface) alg).setArchiveSize(size);
                    ((ArchivedLLHInterface) alg).setPopulation(offspring);
                    if (this.alg instanceof SPEA2) {
                        ((SPEA2) alg).setEnvironmentalSelectionSize(size);
                        if (iteration == 1) {
                            alg.initMetaheuristic(currentPopulation);
                        } else {
                            ((ArchivedLLHInterface) alg).setArchive(currentPopulation);//real difference here
                            ((ArchivedLLHInterface) alg).setPopulation(offspring);
                        }
                    }
                } else if (this.alg instanceof GDE3) {
                    //((GDE3) alg).setRealSize(size);//to generate n
                    this.alg.setPopulation(currentPopulation);
                    this.alg.setPopulationSize(size);
                } else {
                    this.alg.setPopulationSize(size);
                    this.alg.setPopulation(currentPopulation);
                }
                alg.setIterations(iteration);
                op = new Op("setBeginTime", this.id.getAgentName(), System.currentTimeMillis());
                doAction(problemArtifactId, op);
            }
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
            this.hasPopulationAssigned = false;
        }

    }

    protected void runIMOABHHPrepare(int iteration) {
        OpFeedbackParam<Object> res = new OpFeedbackParam<>();
        OpFeedbackParam<Object> res2 = new OpFeedbackParam<>();
        Op op = new Op("getPopulation", res);
        Op op2 = new Op("getOffspring", res2);
        try {
            doAction(populationArtifactId, op);
            doAction(populationArtifactId, op2);
            List<S> completePopulation = ((List<S>) res.get());
            List<S> completeOffspring = ((List<S>) res2.get());
            res = new OpFeedbackParam<>();
            op = new Op("getQtdOfSolutionsForId", this.id, res);
            doAction(copelandArtifact, op);
            int size = (int) (res.get());

            this.hasPopulationAssigned = size > 0;
            if (this.hasPopulationAssigned && size % 2 != 0) {
                this.increasedPopulation = true;
                size++;
            } else {
                this.increasedPopulation = false;
            }
            if (hasPopulationAssigned) {
                paramAlg.setArchiveSize(size);
                paramAlg.setPopulationSize(size);
                this.selectAlg(completePopulation, completeOffspring, size);
                alg.setIterations(iteration);
                op = new Op("setBeginTime", this.id.getAgentName(), System.currentTimeMillis());
                doAction(problemArtifactId, op);
            }
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
            this.hasPopulationAssigned = false;
        }
        //System.out.println("IN "+this.getAgentName()+" "+iteration);
        //System.out.println("IN "+this.getAgentName()+" "+iteration);
    }
    
    protected void prepareToExecute(int iteration) {
        //this.runIMOABHHPrepare(iteration);
        this.runMOABHHPrepare(iteration);
    }
    
    protected void prepareToGetOut() {
        try {
            int iterations = (int) this.getAttributeArtifact(problemArtifactId, "getIteration");
            int maxIterations = (int) this.getAttributeArtifact(problemArtifactId, "getMaxIterations");
            int epsilon = (int) this.getAttributeArtifact(problemArtifactId, "getEpsilon");
            List<S> archive;
            List<S> offspring;
            if (iterations < maxIterations - epsilon) {
                if (alg instanceof ArchivedLLHInterface) {
                    archive = ((((ArchivedLLHInterface) alg).getArchive()));
                    offspring = (this.alg.getPopulation());
                    if (this.increasedPopulation) {
                        int pos = randomGenerator.nextInt(0, archive.size() - 1);
                        archive.remove(pos);
                        offspring.remove(pos);
                    }
                } else {
                    archive = (this.alg.getPopulation());
                    offspring = (this.alg.getPopulation());
                    if (this.increasedPopulation) {
                        int pos = randomGenerator.nextInt(0, archive.size() - 1);
                        //pos=0;
                        archive.remove(pos);
                    }
                }
            } else {
                offspring = new ArrayList<>();
                if (alg instanceof ArchivedLLHInterface) {
                    archive = ((((ArchivedLLHInterface) alg).getArchive()));
                } else {
                    archive = (this.alg.getPopulation());
                }
                if (this.increasedPopulation) {
                    int pos = randomGenerator.nextInt(0, archive.size() - 1);
                    //pos=0;
                    archive.remove(pos);
                }
                archive = alg.getResult();
            }
            Op op = new Op("setPopulationShare", this.id, archive);
            doAction(populationShareArtifactId, op);
            op = new Op("setOffspringShare", this.id, offspring);
            doAction(populationShareArtifactId, op);
            op = new Op("setEndTime", this.id.getAgentName(), System.currentTimeMillis());
            doAction(problemArtifactId, op);
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("OUt "+this.getAgentName());
        //System.out.println("OUt "+this.getAgentName());
    }
    
    protected void updateIterations(int iterations) throws CartagoException {
        alg.setIterations(iterations);
        doAction(problemArtifactId, new Op("setIteration", iterations));
    }

    @Override
    public void run() {
        int maxGen = (int) this.getAttributeArtifact(problemArtifactId, "getMaxIterations");
        int epsilon = (int) this.getAttributeArtifact(problemArtifactId, "getEpsilon");
        try {
            populationShareArtifactId = lookupArtifact("populationshare");
            this.copelandArtifact = lookupArtifact("voting");
            while (true) {
                int algStep = (int) this.getAttributeArtifact(problemArtifactId, "getAlgStep");
                OpFeedbackParam<Object> res = new OpFeedbackParam<>();

                doAction(problemArtifactId, new Op("isThisMetaHeuristicExecuted", this.id, res));
                Boolean alreadyExecuted = (Boolean) res.get();
                //System.out.println(this.getAgentName() + " " + alreadyExecuted + " " + algStep);
                if (!alreadyExecuted && (algStep == 2 || algStep == 11)) {
                    doAction(problemArtifactId, new Op("isThisMetaHeuristicAllowedToExecute", this.id, res));
                    Boolean allowedToExecute = (Boolean) res.get();
                    int iterations = (int) this.getAttributeArtifact(problemArtifactId, "getIteration");
                    if (allowedToExecute && iterations < maxGen) {
                        this.prepareToExecute(iterations);
                        if (this.hasPopulationAssigned) {
                            //System.out.println(this.getAgentName());
                            if(problem instanceof RealWorldProblem){
                                //System.out.println(this.alg.getClass().getSimpleName()+" before "+((RealWorldProblem)problem).getQtdEvaluated());
                            }
                            //System.out.println(this.getAgentName() + " executes at "+iterations);
                            for (int i = 0; i < epsilon; i++) {
                                //System.err.print(" run");
                                this.alg.generateNewPopulation();
                                iterations++;
                                updateIterations(iterations);
                                if(problem instanceof RealWorldProblem){
                                //System.out.println(this.alg.getClass().getSimpleName()+" after "+((RealWorldProblem)problem).getQtdEvaluated());
                            }
                            }
                            //System.out.println("");
                            //System.out.println(this.getAgentName() + " set population share");
                            this.prepareToGetOut();
                            //System.out.println(this.getAgentName() + " sets already executed");
                            doAction(problemArtifactId, new Op("setAlreadyExecuted", this.id));
                        } else {
                            //System.out.println(this.getAgentName() + " has no population");
                            doAction(problemArtifactId, new Op("setAlreadyExecuted", this.id));
                        }
                    }
                }
            }

        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgentv2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

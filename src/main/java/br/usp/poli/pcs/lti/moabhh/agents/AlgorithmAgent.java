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

/**
 * The type Algorithm agent.
 *
 * @param <S> the type parameter
 */
@SuppressWarnings("serial")
public class AlgorithmAgent<S extends Solution<?>> extends SimplerAgent {

    protected LLHInterface alg;
    protected ArtifactId problemArtifactId;
    protected ArtifactId populationArtifactId;
    protected ArtifactId populationShareArtifactId;
    protected boolean hasPopulationAssigned;
    protected boolean increasedPopulation;
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
    public AlgorithmAgent(String agentName, long uId, String algConfigFileName, String parameterHeuristic, long seed) {
        super(agentName, uId, seed);
        this.selectAlg(algConfigFileName, parameterHeuristic);
    }

    protected void selectAlg(String algConfigFileName, String parameterHeuristic) {
        try {
            problemArtifactId = lookupArtifact("problem");
            populationArtifactId = lookupArtifact("population");
            Problem problem = (Problem) this.getAttributeArtifact(problemArtifactId, "getProblem_");
            int populationSize = (int) this.getAttributeArtifact(problemArtifactId, "getQtdPopulation");
            int maxIterations = (int) this.getAttributeArtifact(problemArtifactId, "getMaxIterations");
            ParametersforAlgorithm paramAlg = new ParametersforAlgorithm(algConfigFileName);
            paramAlg.setMaxIteractions(maxIterations);
            paramAlg.setPopulationSize(populationSize);
            paramAlg.setArchiveSize(populationSize);
            ParametersforHeuristics pHeur = new ParametersforHeuristics(parameterHeuristic, problem.getNumberOfVariables());
            AlgorithmBuilder hb = new AlgorithmBuilder(problem, () -> randomGenerator.nextDouble());
            //AlgorithmBuilder hb = new AlgorithmBuilder(problem, () -> JMetalRandom.getInstance().nextDouble());
            alg = hb.create(paramAlg, pHeur);
            List<S> population = (List<S>) this
                    .getAttributeArtifact(populationArtifactId, "getPopulation");
            this.alg.initMetaheuristic(population);
        } catch (CartagoException | JMException | ConfigurationException | FileNotFoundException ex) {
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
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
                int chosen = randomGenerator.nextInt(0, currentPopulation.size()-1);
                currentPopulation.add((S) currentPopulation.get(chosen).copy());
                offspring.add((S) currentPopulation.get(chosen).copy());
                size++;
            } else {
                this.increasedPopulation = false;
            }
            if(hasPopulationAssigned){
                if (this.alg instanceof ArchivedLLHInterface) {
                    ((ArchivedLLHInterface) alg).setPopulationSize(size);
                    ((ArchivedLLHInterface) alg).setArchive(currentPopulation);//real difference here
                    ((ArchivedLLHInterface) alg).setArchiveSize(size);
                    ((ArchivedLLHInterface) alg).setPopulation(offspring);
                    if (this.alg instanceof SPEA2) {
                        ((SPEA2) alg).setEnvironmentalSelectionSize(size);
                        if(iteration==1){
                            alg.initMetaheuristic(currentPopulation);
                        }
                        else{
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
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
            this.hasPopulationAssigned = false;
        }

    }

    /*
    //STOPED TO WORK in current version
    protected void runIMOABHHPrepare(int iteration) {
        OpFeedbackParam<Object> res = new OpFeedbackParam<>();
        Op op = new Op("getPopulation", res);
        try {
            doAction(populationArtifactId, op);
            List<S> completePopulation = ((List<S>) res.get());
            Collections.shuffle(completePopulation);
            List<S> fullPop = SolutionListUtils.getNondominatedSolutions(completePopulation);
            if (iteration == 1) {
                fullPop = completePopulation;
            }
            if (fullPop.size() < 4) {
                fullPop = completePopulation;
            }
            res = new OpFeedbackParam<>();
            op = new Op("getQtdOfSolutionsForId", this.id, res);
            doAction(copelandArtifact, op);
            int size = (int) ((double) res.get());

            this.hasPopulationAssigned = size > 0;
            if (this.hasPopulationAssigned && size % 2 != 0) {
                this.increasedPopulation = true;
                size++;
            } else {
                this.increasedPopulation = false;
            }

            if (this.alg instanceof ArchivedLLHInterface) {
                ((ArchivedLLHInterface) alg).setPopulationSize(size);
                ((ArchivedLLHInterface) alg).setPopulation(new ArrayList<>());
                ((ArchivedLLHInterface) alg).setArchiveSize(size);
                ((ArchivedLLHInterface) alg).setArchive(new ArrayList<>());
                if (this.alg instanceof SPEA2) {
                    ((SPEA2) alg).setEnvironmentalSelectionSize(size);
                }
                List<S> selected = new ArrayList<>();
                if (hasPopulationAssigned) {
                    selected = this.alg.updateMainPopulation(fullPop);
                    //System.out.println(this.id.getAgentName()+" "+size+" "+fullPop.size()+" "+selected.size());
                }
                ((ArchivedLLHInterface) alg).setArchive(selected);
            } else if (this.alg instanceof GDE3) {
                ((GDE3) alg).setRealSize(size);//to generate n
                List<S> selected = this.alg.updateMainPopulation(completePopulation);
                this.alg.setPopulation(selected);
            } else {
                this.alg.setPopulation(new ArrayList<>());
                this.alg.setPopulationSize(size);
                List<S> selected = new ArrayList<>();
                if (hasPopulationAssigned) {
                    selected = this.alg.updateMainPopulation(fullPop);
                    //System.out.println(this.id.getAgentName()+" "+size+" "+fullPop.size()+" "+selected.size());
                }
                this.alg.setPopulation(selected);
            }
            alg.setIterations(iteration);
            op = new Op("setBeginTime", this.id.getAgentName(), System.currentTimeMillis());
            doAction(problemArtifactId, op);
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
            this.hasPopulationAssigned = false;
        }
        //System.out.println("IN "+this.getAgentName()+" "+iteration);
    }
*/

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
                    archive=((((ArchivedLLHInterface) alg).getArchive()));
                    offspring=(this.alg.getPopulation());
                    if (this.increasedPopulation) {
                        int pos=randomGenerator.nextInt(0, archive.size()-1);
                        archive.remove(pos);
                        offspring.remove(pos);
                    }
                }
                else{
                    archive=(this.alg.getPopulation());
                    offspring=(this.alg.getPopulation());
                    if (this.increasedPopulation) {
                        int pos=randomGenerator.nextInt(0, archive.size()-1);
                        archive.remove(pos);
                    }
                }
            } else {
                offspring=new ArrayList<>();
                if (alg instanceof ArchivedLLHInterface) {
                    archive=((((ArchivedLLHInterface) alg).getArchive()));
                } else {
                    archive = (this.alg.getPopulation());
                }
                if (this.increasedPopulation) {
                    int pos=randomGenerator.nextInt(0, archive.size()-1);
                    archive.remove(pos);
                }
                archive=alg.getResult();
            }
            Op op = new Op("setPopulationShare", this.id, archive);
            doAction(populationShareArtifactId, op);
            op = new Op("setOffspringShare", this.id, offspring);
            doAction(populationShareArtifactId, op);
            op = new Op("setEndTime", this.id.getAgentName(), System.currentTimeMillis());
            doAction(problemArtifactId, op);
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("OUt "+this.getAgentName());
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
                if (!alreadyExecuted && (algStep == 2 || algStep==11)) {
                    doAction(problemArtifactId, new Op("isThisMetaHeuristicAllowedToExecute", this.id, res));
                    Boolean allowedToExecute = (Boolean) res.get();
                    if(allowedToExecute){
                        int iterations = (int) this.getAttributeArtifact(problemArtifactId, "getIteration");
                        this.prepareToExecute(iterations);
                        if (this.hasPopulationAssigned) {
                            //System.out.println(this.getAgentName());
                            //System.out.println(this.getAgentName() + " executes");
                            for (int i = 0; i < epsilon && iterations < maxGen; i++) {
                                this.alg.generateNewPopulation();
                                iterations++;
                                alg.setIterations(iterations);
                            }
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
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

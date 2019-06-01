package br.usp.poli.pcs.lti.moabhh.artifacts;

import cartago.AgentId;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.util.ArrayList;
import java.util.HashMap;

import org.uma.jmetal.problem.Problem;

/**
 * The type System variables artifact.
 */
public class SystemVariablesArtifact extends Artifact {

    /*Control Variables*/
    private final HashMap<AgentId, Boolean> alreadyVoted;

    private final HashMap<AgentId, Boolean> alreadyExecuted;

    private final HashMap<AgentId, Boolean> allowedToExecute;

    private final ArrayList<int[]> allvotes;

    private Problem problem;

    private int maxIteration;

    private int iteration;

    private int qtdPopulation;

    private int qtdArchive;
    /*how many generations to spend during the initialization*/
    private int delta;
    /*how many generations to process until the Copeland voting happen*/
    private int epsilon;
    /*the decreasing percent of the population size that should be given to 
  the best meta-heuristics agent at the end*/
    private double beta;
    private int algStep;

    private final HashMap<String, Long> initTimes;

    private final HashMap<String, Long> endTimes;

    private int posOfExecutingAlg;

    protected int[] qtdToRun;

    /**
     * Instantiates a new System variables artifact.
     */
    public SystemVariablesArtifact() {
        this.problem = null;
        this.maxIteration = 0;
        this.iteration = 1;
        this.qtdPopulation = 0;
        this.qtdArchive = 0;
        this.delta = 0;
        this.epsilon = 0;
        this.beta = 0;
        this.alreadyVoted = new HashMap<>();
        this.alreadyExecuted = new HashMap<>();
        this.algStep = -1;
        this.initTimes = new HashMap<>();
        this.endTimes = new HashMap<>();
        this.allvotes = new ArrayList<>();
        this.allowedToExecute = new HashMap<>();
    }

    /**
     * Gets problem.
     *
     * @param res the res
     */
    @OPERATION
    public void getProblem_(OpFeedbackParam<Object> res) {
        res.set(this.problem);
    }

    /**
     * Sets problem.
     *
     * @param problem the problem
     */
    @OPERATION
    public void setProblem_(Problem problem) {
        this.problem = problem;
    }

    /**
     * Gets max iterations.
     *
     * @param res the res
     */
    @OPERATION
    public void getMaxIterations(OpFeedbackParam<Object> res) {
        res.set(maxIteration);
    }

    /**
     * Sets max iterations.
     *
     * @param maxEvaluations the max evaluations
     */
    @OPERATION
    public void setMaxIterations(int maxEvaluations) {
        this.maxIteration = maxEvaluations;
    }

    /**
     * Gets iteration.
     *
     * @param res the res
     */
    @OPERATION
    public void getIteration(OpFeedbackParam<Object> res) {
        res.set(iteration);
    }

    /**
     * Sets iteration.
     *
     * @param iteration the iteration
     */
    @OPERATION
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    /**
     * Gets qtd population.
     *
     * @param res the res
     */
    @OPERATION
    public void getQtdPopulation(OpFeedbackParam<Object> res) {
        res.set(qtdPopulation);
    }

    /**
     * Sets qtd population.
     *
     * @param qtdPopulation the qtd population
     */
    @OPERATION
    public void setQtdPopulation(int qtdPopulation) {
        this.qtdPopulation = qtdPopulation;
    }

    /**
     * Gets qtd archive.
     *
     * @param res the res
     */
    @OPERATION
    public void getQtdArchive(OpFeedbackParam<Object> res) {
        res.set(qtdArchive);
    }

    /**
     * Gets delta.
     *
     * @param res the res
     */
    @OPERATION
    public void getDelta(OpFeedbackParam<Object> res) {
        res.set(delta);
    }

    /**
     * Gets epsilon.
     *
     * @param res the res
     */
    @OPERATION
    public void getEpsilon(OpFeedbackParam<Object> res) {
        res.set(epsilon);
    }

    /**
     * Gets beta.
     *
     * @param res the res
     */
    @OPERATION
    public void getBeta(OpFeedbackParam<Object> res) {
        res.set(beta);
    }

    /**
     * Sets qtd archive.
     *
     * @param qtdArchive the qtd archive
     */
    @OPERATION
    public void setQtdArchive(int qtdArchive) {
        this.qtdArchive = qtdArchive;
    }

    /**
     * Sets delta.
     *
     * @param delta the delta
     */
    @OPERATION
    public void setDelta(int delta) {
        this.delta = delta;
    }

    /**
     * Sets epsilon.
     *
     * @param epsilon the epsilon
     */
    @OPERATION
    public void setEpsilon(int epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Sets beta.
     *
     * @param beta the beta
     */
    @OPERATION
    public void setBeta(double beta) {
        this.beta = beta;
    }

    /**
     * Is this indicator voter executed.
     *
     * @param agid the agid
     * @param res the res
     */
    @OPERATION
    public void isThisIndicatorVoterExecuted(AgentId agid, OpFeedbackParam<Object> res) {
        res.set(this.alreadyVoted.getOrDefault(agid, Boolean.FALSE));
    }

    /**
     * Sets already voted.
     *
     * @param agid the agid
     */
    @OPERATION
    public void setAlreadyVoted(AgentId agid) {
        this.alreadyVoted.put(agid, Boolean.TRUE);
    }

    /**
     * Gets alg step.
     *
     * @param res the res
     */
    @OPERATION
    public void getAlgStep(OpFeedbackParam<Object> res) {
        res.set(algStep);
    }

    /**
     * Sets alg step.
     *
     * @param algStep the alg step
     */
    @OPERATION
    public void setAlgStep(int algStep) {
        this.algStep = algStep;
    }

    /**
     * Generate already voted.
     *
     * @param agents the agents
     */
    @OPERATION
    public void generateAlreadyVoted(ArrayList<AgentId> agents) {
        for (AgentId agid : agents) {
            this.alreadyVoted.put(agid, Boolean.FALSE);
        }
    }

    /**
     * Generate already executed.
     *
     * @param agents the agents
     */
    @OPERATION
    public void generateAlreadyExecuted(ArrayList<AgentId> agents) {
        for (AgentId agid : agents) {
            this.alreadyExecuted.put(agid, Boolean.FALSE);
            this.allowedToExecute.put(agid, Boolean.FALSE);
        }
    }

    /**
     * Reset already executed.
     */
    @OPERATION
    public void resetAlreadyExecuted() {
        AgentId[] indices = this.alreadyExecuted.keySet()
                .toArray(new AgentId[alreadyExecuted.keySet().size()]);
        for (AgentId index : indices) {
            this.alreadyExecuted.put(index, Boolean.FALSE);
        }
    }

    /**
     * Reset already executed.
     */
    @OPERATION
    public void noneAllowedToExecute() {
        AgentId[] indices = this.allowedToExecute.keySet()
                .toArray(new AgentId[allowedToExecute.keySet().size()]);
        for (AgentId index : indices) {
            this.allowedToExecute.put(index, Boolean.FALSE);
        }
    }

    /**
     * Reset already executed.
     */
    @OPERATION
    public void allAllowedToExecute() {
        AgentId[] indices = this.allowedToExecute.keySet()
                .toArray(new AgentId[allowedToExecute.keySet().size()]);
        for (AgentId index : indices) {
            this.allowedToExecute.put(index, Boolean.TRUE);
        }
    }

    /**
     * Reset already voted.
     */
    @OPERATION
    public void resetAlreadyVoted() {
        AgentId[] indices = this.alreadyVoted.keySet()
                .toArray(new AgentId[alreadyVoted.keySet().size()]);
        for (AgentId index : indices) {
            this.alreadyVoted.put(index, Boolean.FALSE);
        }
    }

    /**
     * Is votation finished.
     *
     * @param res the res
     */
    @OPERATION
    public void isVotationFinished(OpFeedbackParam<Object> res) {
        AgentId[] indices = this.alreadyVoted.keySet().toArray(new AgentId[alreadyVoted.size()]);
        int qtd = 0;
        for (AgentId index : indices) {
            if (this.alreadyVoted.get(index)) {
                qtd++;
            }
        }
        if (qtd == alreadyVoted.size()) {
            res.set(true);
        } else {
            res.set(false);
        }
    }

    /**
     * Is execution finished.
     *
     * @param res the res
     */
    @OPERATION
    public void isExecutionFinished(OpFeedbackParam<Object> res) {
        AgentId[] indices = this.alreadyExecuted.keySet().toArray(new AgentId[alreadyExecuted.size()]);
        int qtd = 0;
        for (AgentId index : indices) {
            if (this.alreadyExecuted.get(index)) {
                qtd++;
            }
        }
        if (qtd == alreadyExecuted.size()) {
            res.set(true);
        } else {
            res.set(false);
        }
    }

    /**
     * Is this mh executed.
     *
     * @param agid the agid
     * @param res the res
     */
    @OPERATION
    public void isThisMetaHeuristicExecuted(AgentId agid, OpFeedbackParam<Object> res) {
        res.set(this.alreadyExecuted.getOrDefault(agid, Boolean.FALSE));
    }

    /**
     * Is this mh executed.
     *
     * @param agid the agid
     * @param res the res
     */
    @OPERATION
    public void isThisMetaHeuristicAllowedToExecute(AgentId agid, OpFeedbackParam<Object> res) {
        res.set(this.allowedToExecute.getOrDefault(agid, Boolean.FALSE));
    }

    /**
     * Sets already executed.
     *
     * @param agid the agid
     */
    @OPERATION
    public void setAlreadyExecuted(AgentId agid) {
        this.alreadyExecuted.put(agid, Boolean.TRUE);
        AgentId[] indices = this.alreadyExecuted.keySet()
                .toArray(new AgentId[alreadyExecuted.keySet().size()]);
    }

    /**
     * Sets already executed.
     *
     * @param agid the agid
     */
    @OPERATION
    public void setToExecute(AgentId agid) {
        this.allowedToExecute.put(agid, Boolean.TRUE);
    }

    /**
     * Set Begin Time.
     *
     * @param agid
     * @param time
     */
    @OPERATION
    public void setBeginTime(String agid, long time) {
        this.initTimes.put(agid, time);
    }

    /**
     * Get Begin Time.
     *
     * @param agid
     * @param res
     */
    @OPERATION
    public void getBeginTime(String agid, OpFeedbackParam<Object> res) {
        res.set(this.initTimes.get(agid));
    }

    /**
     * Set End Time.
     *
     * @param agid
     * @param time
     */
    @OPERATION
    public void setEndTime(String agid, long time) {
        this.endTimes.put(agid, time);
    }

    /**
     * Get End Time.
     *
     * @param agid
     * @param res
     */
    @OPERATION
    public void getEndTime(String agid, OpFeedbackParam<Object> res) {
        res.set(this.endTimes.get(agid));
    }

    @OPERATION
    public void addVote(int[] rank) {
        this.allvotes.add(rank);
    }

    @OPERATION
    public void getAllVotes(OpFeedbackParam<Object> res) {
        res.set(this.allvotes);
    }

    /**
     * Gets alg step.
     *
     * @param res the res
     */
    @OPERATION
    public void getPosOfExecutingAlg(OpFeedbackParam<Object> res) {
        res.set(posOfExecutingAlg);
    }

    /**
     * Sets alg step.
     *
     * @param posOfExecutingAlg
     */
    @OPERATION
    public void setPosOfExecutingAlg(int posOfExecutingAlg) {
        this.posOfExecutingAlg = posOfExecutingAlg;
    }

    @OPERATION
    public void getQtdToRun(OpFeedbackParam<Object> res) {
        res.set(qtdToRun);
    }

    @OPERATION
    public void setQtdToRun(int[] qtdToRun) {
        this.qtdToRun = qtdToRun;
    }

}

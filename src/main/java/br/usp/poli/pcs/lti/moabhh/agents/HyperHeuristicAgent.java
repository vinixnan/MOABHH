package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.TaggedSolution;
import cartago.AgentId;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.Op;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.solution.Solution;

/**
 * The type Hh agent.
 *
 * @param <S> the type parameter
 */
public abstract class HyperHeuristicAgent<S extends Solution<?>> extends SimplerAgent {

    /**
     * The Copeland artifact.
     */
    protected ArtifactId copelandArtifact;
    /**
     * The Population artifact.
     */
    protected ArtifactId populationArtifact;
    /**
     * The Population share artifact.
     */
    protected ArtifactId populationShareArtifact;
    /**
     * The Problem artifact.
     */
    protected ArtifactId problemArtifact;
    /**
     * The List mh agents.
     */
    protected ArrayList<AgentId> listMhAgents;
    /**
     * The Qtd solutions.
     */
    protected double[] qtdSolutions;

    protected int[] discretizedQtdSolutions;

    protected Integer posOfExecutingAlg;

    protected boolean isStochastic;

    /**
     * Instantiates a new Hh agent.
     *
     * @param agentName the agent name
     * @param uId
     * @param seed
     */
    public HyperHeuristicAgent(String agentName, long uId, long seed) {
        super(agentName, uId, seed);
        isStochastic = (seed == -1);
    }

    @Override
    public void init() {
        super.init();
        try {
            this.copelandArtifact = lookupArtifact("voting");
            this.populationArtifact = lookupArtifact("population");
            this.populationShareArtifact = lookupArtifact("populationshare");
            this.problemArtifact = lookupArtifact("problem");
            this.listMhAgents = this.getMhAgentIds();
            this.qtdSolutions = new double[this.listMhAgents.size()];
            this.discretizedQtdSolutions = new int[this.qtdSolutions.length];
        } catch (CartagoException ex) {
            Logger.getLogger(HyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shared population to population.
     *
     * @throws CartagoException the cartago exception
     */
    protected void sharedPopulationToPopulation() throws CartagoException {
        List<S> pop = (List<S>) getAttributeArtifact(populationShareArtifact, "getAllPopulationShare");
        List<S> off = (List<S>) getAttributeArtifact(populationShareArtifact, "getAllOffspringShare");
        int delta = (int) this.getAttributeArtifact(problemArtifact, "getDelta");
        int gen = (int) this.getAttributeArtifact(problemArtifact, "getIteration");
        if (gen < delta) {
            //System.err.println("Reset");
            for (S sl : pop) {
                if (sl instanceof TaggedSolution) {
                    ((TaggedSolution) sl).setAlgorithm(null);
                    ((TaggedSolution) sl).setAction(null);

                }
            }
            for (S sl : off) {
                if (sl instanceof TaggedSolution) {
                    ((TaggedSolution) sl).setAlgorithm(null);
                    ((TaggedSolution) sl).setAction(null);

                }
            }

        }
        //System.out.println("Shared to Population " + pop.size());
        doAction(populationArtifact, new Op("setPopulation", pop));
        doAction(populationArtifact, new Op("setOffspring", off));
    }

    /**
     * Start voting.
     *
     * @throws CartagoException the cartago exception
     */
    protected void startVoting() throws CartagoException {
        doAction(problemArtifact, new Op("resetAlreadyVoted"));
        doAction(copelandArtifact, new Op("resetVotation"));
    }

    /**
     * Start mh.
     *
     * @throws CartagoException the cartago exception
     */
    protected void startMhAgent() throws CartagoException {
        if (isThisStochastic() || isOnlyOneAlgExecuting()) {
            doAction(problemArtifact, new Op("resetAlreadyExecuted"));
            doAction(problemArtifact, new Op("allAllowedToExecute"));
            doAction(problemArtifact, new Op("setAlgStep", 2));
        } else {
            posOfExecutingAlg = getFirstNonZeroOcorrence(qtdSolutions, -1);
            doAction(problemArtifact, new Op("resetAlreadyExecuted"));
            doAction(problemArtifact, new Op("noneAllowedToExecute"));
            doAction(problemArtifact, new Op("setToExecute", listMhAgents.get(posOfExecutingAlg)));
            doAction(problemArtifact, new Op("setAlgStep", 11));
        }
    }
    
    protected void clearNoParticipatingSharedPopulations() throws CartagoException {
        //clear all other population
        for (int i = 0; i < listMhAgents.size(); i++) {
            AgentId otherId = listMhAgents.get(i);
            if (discretizedQtdSolutions[i] <= 0) {
                Op op = new Op("setPopulationShare", otherId, new ArrayList());
                doAction(this.populationShareArtifact, op);
                op = new Op("setOffspringShare", otherId, new ArrayList());
                doAction(this.populationShareArtifact, op);
            }
        }
    }

    /**
     * Population to shared population.
     *
     * @throws CartagoException the cartago exception
     */
    protected void populationToSharedPopulation() throws CartagoException {
        List<S> currentPopulation = (List<S>) getAttributeArtifact(this.populationArtifact, "getPopulation");
        List<S> offspring = (List<S>) getAttributeArtifact(this.populationArtifact, "getOffspring");
        int populationSize = (int) this.getAttributeArtifact(problemArtifact, "getQtdPopulation");
        if (isOnlyOneAlgExecuting()) {
            //necessary to avoid randomness in tests with fixed seed for one alg
            int posAlg = Ints.indexOf(discretizedQtdSolutions, Ints.max(discretizedQtdSolutions));
            AgentId idAlg = listMhAgents.get(posAlg);
            Op op = new Op("setPopulationShare", idAlg, currentPopulation);
            doAction(this.populationShareArtifact, op);
            op = new Op("setOffspringShare", idAlg, offspring);
            doAction(this.populationShareArtifact, op);
        } else {
            while (currentPopulation.size() < populationSize) {
                currentPopulation.add(currentPopulation.get(randomGenerator.nextInt(0, currentPopulation.size() - 1)));
            }
            ArrayList<List<S>> divisionPopulation = new ArrayList<>();
            for (int x = 0; x < discretizedQtdSolutions.length; x++) {
                int toAdd = 0;
                divisionPopulation.add(new ArrayList<>(populationSize));
                while (toAdd < discretizedQtdSolutions[x] && currentPopulation.size() > 0) {//MOABHH dobra
                    int chosen = randomGenerator.nextInt(0, currentPopulation.size() - 1);
                    Solution s = currentPopulation.get(chosen);
                    if (s instanceof TaggedSolution) {
                        ((TaggedSolution) s).setAlgorithm(null);
                        ((TaggedSolution) s).setAction(null);

                    }
                    currentPopulation.remove(chosen);
                    divisionPopulation.get(x).add((S) s);
                    toAdd++;
                }
                Op op = new Op("setPopulationShare", this.listMhAgents.get(x), divisionPopulation.get(x));
                doAction(this.populationShareArtifact, op);
            }
            ArrayList<List<S>> divisionOffspring = new ArrayList<>();
            for (int x = 0; x < discretizedQtdSolutions.length; x++) {
                int toAdd = 0;
                divisionOffspring.add(new ArrayList<>(populationSize));
                while (toAdd < discretizedQtdSolutions[x] && offspring.size() > 0) {//MOABHH dobra
                    int chosen = randomGenerator.nextInt(0, offspring.size() - 1);
                    Solution s = offspring.get(chosen);
                    if (s instanceof TaggedSolution) {
                        ((TaggedSolution) s).setAlgorithm(null);
                        ((TaggedSolution) s).setAction(null);

                    }
                    offspring.remove(chosen);
                    divisionOffspring.get(x).add((S) s);
                    toAdd++;
                }
                Op op = new Op("setOffspringShare", this.listMhAgents.get(x), divisionOffspring.get(x));
                doAction(this.populationShareArtifact, op);

            }
        }
    }

    protected boolean isOnlyOneAlgExecuting() {
        int populationSize = (int) this.getAttributeArtifact(problemArtifact, "getQtdPopulation");
        return Ints.max(discretizedQtdSolutions) == populationSize;
    }

    protected boolean isThisStochastic() {
        return this.isStochastic;
    }

    protected Integer getFirstNonZeroOcorrence(double[] array, int begin) {
        for (int j = begin + 1; j < array.length; j++) {
            if (array[j] > 0) {
                //System.out.println(j);
                return j;
            }
        }
        return null;
    }
}

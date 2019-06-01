package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingOutcomeProcessing;

import cartago.CartagoException;
import cartago.Op;
import cartago.OpFeedbackParam;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.solution.Solution;

/**
 * The type Copeland hh agent.
 *
 * @param <S> the type parameter
 */
public class ElectionBasedHyperHeuristicAgent<S extends Solution<?>> extends HyperHeuristicAgent {

    protected VotingMethod votingmethod;

    /**
     * Instantiates a new Copeland hh agent.
     *
     * @param agentName the agent name
     * @param uId
     * @param seed
     */
    public ElectionBasedHyperHeuristicAgent(String agentName, long uId, long seed) {
        super(agentName, uId, seed);
    }

    @Override
    public void run() {
        this.init();
        votingmethod = (VotingMethod) this.getAttributeArtifact(copelandArtifact, "getVotingMethod");
        int populationSize = (int) this.getAttributeArtifact(problemArtifact, "getQtdPopulation");
        int maxGen = (int) this.getAttributeArtifact(problemArtifact, "getMaxIterations");
        int epsilon = (int) this.getAttributeArtifact(problemArtifact, "getEpsilon");
        int delta = (int) this.getAttributeArtifact(problemArtifact, "getDelta");
        double beta = (double) this.getAttributeArtifact(problemArtifact, "getBeta");
        try {
            while (true) {
                int gen = (int) this.getAttributeArtifact(problemArtifact, "getIteration");
                int algStep = (int) this.getAttributeArtifact(problemArtifact, "getAlgStep");
                //System.out.println(this.getAgentName()+" algstep "+algStep);
                if (gen < delta) {
                    //System.out.println(algStep+" "+gen);
                    switch (algStep) {
                        case -1:
                            this.uniformSplit(populationSize);
                            //System.out.println("\n\n" + Arrays.toString(qtdSolutions));
                            //share population
                            this.populationToSharedPopulation();
                            //set alg to execute
                            this.startMhAgent();
                            break;
                        case 11:
                            OpFeedbackParam<Object> res = new OpFeedbackParam<>();
                            doAction(problemArtifact, new Op("isThisMetaHeuristicExecuted", listMhAgents.get(posOfExecutingAlg), res));
                            Boolean alreadyExecuted = (Boolean) res.get();
                            if (alreadyExecuted) {
                                posOfExecutingAlg = getFirstNonZeroOcorrence(qtdSolutions, posOfExecutingAlg);
                                if (posOfExecutingAlg == null) {
                                    //System.out.println("\n");
                                    doAction(problemArtifact, new Op("setAlgStep", 3));
                                } else {
                                    doAction(problemArtifact, new Op("setToExecute", listMhAgents.get(posOfExecutingAlg)));
                                }
                            }
                            break;
                        case 2:
                            boolean allfinished = (boolean) this
                                    .getAttributeArtifact(problemArtifact, "isExecutionFinished");
                            if (allfinished) {
                                //System.out.println(this.getAgentName() + " sets all finished");
                                doAction(problemArtifact, new Op("setAlgStep", 3));
                            }
                            break;
                        case 3:
                            //set population share to population
                            this.sharedPopulationToPopulation();
                            //update evaluation
                            doAction(problemArtifact, new Op("setIteration", gen + epsilon));
                            doAction(problemArtifact, new Op("setAlgStep", -1));
                            break;
                        default:
                            break;

                    }

                } else if (gen < maxGen) {
                    //System.out.println(algStep+" "+gen+" Second stage");
                    switch (algStep) {
                        case -1:
                            if ((gen - 1) % epsilon == 0) {
                                //time to voting
                                //System.out.println(gen+" of "+maxGen+" "+(gen%epsilon));
                                this.startVoting();
                                doAction(problemArtifact, new Op("setAlgStep", 0));
                            } else {
                                doAction(problemArtifact, new Op("setAlgStep", 1));
                            }
                            break;
                        case 0:
                            boolean allfinishedVote = (boolean) this
                                    .getAttributeArtifact(problemArtifact, "isVotationFinished");
                            if (allfinishedVote) {
                                //System.out.println(this.getAgentName() + " sets all votation finished");
                                doAction(problemArtifact, new Op("setAlgStep", 1));
                            }
                            break;
                        case 1://indicator voter will change from 1 setAlgStep to 1
                            if ((gen - 1) % epsilon == 0) {
                                //apply percentual
                                this.applyPercentual(beta, (gen - 1) % epsilon == 0, populationSize);
                                try {
                                    doAction(copelandArtifact, new Op("setQtdOfSolutions", discretizedQtdSolutions));
                                    doAction(copelandArtifact, new Op("generateQtdSolutionsHash", this.listMhAgents, discretizedQtdSolutions));
                                } catch (CartagoException ex) {
                                    Logger.getLogger(ElectionBasedHyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }   //share population
                            this.populationToSharedPopulation();
                            this.startMhAgent();
                            break;
                        case 11:
                            OpFeedbackParam<Object> res = new OpFeedbackParam<>();
                            doAction(problemArtifact, new Op("isThisMetaHeuristicExecuted", listMhAgents.get(posOfExecutingAlg), res));
                            Boolean alreadyExecuted = (Boolean) res.get();
                            if (alreadyExecuted) {
                                posOfExecutingAlg = getFirstNonZeroOcorrence(qtdSolutions, posOfExecutingAlg);
                                if (posOfExecutingAlg == null) {
                                    //System.out.println("\n");
                                    doAction(problemArtifact, new Op("setAlgStep", 3));
                                } else {
                                    doAction(problemArtifact, new Op("setToExecute", listMhAgents.get(posOfExecutingAlg)));
                                }
                            }
                            break;
                        case 2:
                            boolean allfinished = (boolean) this
                                    .getAttributeArtifact(problemArtifact, "isExecutionFinished");
                            if (allfinished) {
                                //System.out.println(this.getAgentName() + " sets all finished");
                                doAction(problemArtifact, new Op("setAlgStep", 3));
                            }
                            break;
                        //case 2 just MHAgents can execute and they will change setAlgStep to 3
                        case 3:
                            //set population share to population
                            this.sharedPopulationToPopulation();
                            //update evaluation
                            doAction(problemArtifact, new Op("setIteration", gen + epsilon));
                            doAction(problemArtifact, new Op("setAlgStep", -1));
                            break;
                        default:
                            break;
                    }
                }

            }
        } catch (CartagoException ex) {
            Logger.getLogger(HyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void applyPercentual(double beta, boolean timeToUpdate, int populationSize) throws CartagoException {
        int maxGen = (int) this.getAttributeArtifact(problemArtifact, "getMaxIterations");
        int gen = (int) this.getAttributeArtifact(problemArtifact, "getIteration");
        double linbase = (((double) gen) / ((double) maxGen));
        //gambiarra
        double fn;
        if (beta == 666.0) {
            beta = 1;
            fn = linbase * beta;//linear
        } else if (beta == 777.0) {
            beta = 1;
            fn = (Math.exp(linbase) / Math.exp(1)) * beta;//exp
        } else {
            beta = beta / 100.0;
            fn = beta;
        }

        int[] votationResults = (int[]) this
                .getAttributeArtifact(copelandArtifact, "getVotationResults");
        if (timeToUpdate) {
            VotingOutcomeProcessing.votesProcessing(qtdSolutions, discretizedQtdSolutions, votationResults, fn, populationSize);
            discretizedQtdSolutions = VotingOutcomeProcessing.discretizeArray(qtdSolutions, populationSize, randomGenerator);
            clearNoParticipatingSharedPopulations();
            
            String toprint = "";
            for (Object id : listMhAgents) {
                toprint += id.toString().split("_")[0] + " ";
            }
            System.out.println(toprint + "\n" + Arrays.toString(discretizedQtdSolutions) + " at " + gen + " beta=" + fn + "\n");
            int sum = 0;
            for (int i = 0; i < discretizedQtdSolutions.length; i++) {
                if (discretizedQtdSolutions[i] > 0) {
                    sum += discretizedQtdSolutions[i];
                }
            }
            //System.out.println("Total +"+sum);
            
             
        }

        try {
            int[] rank = new int[qtdSolutions.length];
            for (int i = 0; i < rank.length; i++) {
                rank[i] = (int) qtdSolutions[i];
            }
            doAction(problemArtifact, new Op("addVote", rank));
        } catch (CartagoException ex) {
            Logger.getLogger(ElectionBasedHyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void uniformSplit(int populationSize) {
        //calculate each one participation
        double equalsPercent = 1.0 / ((double) listMhAgents.size());
        double qtd = Math.floor(equalsPercent * populationSize);
        Arrays.fill(qtdSolutions, qtd);
        try {
            discretizedQtdSolutions = VotingOutcomeProcessing.discretizeArray(qtdSolutions, populationSize, randomGenerator);
            doAction(copelandArtifact, new Op("setQtdOfSolutions", discretizedQtdSolutions));
            doAction(copelandArtifact, new Op("generateQtdSolutionsHash", this.listMhAgents, discretizedQtdSolutions));
        } catch (CartagoException ex) {
            Logger.getLogger(ElectionBasedHyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

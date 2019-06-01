package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.ExtraPseudoRandom;
import cartago.CartagoException;
import cartago.Op;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uma.jmetal.solution.Solution;

/**
 * The type Random hh agent.
 *
 * @param <S> the type parameter
 */
public class RandomHyperHeuristicAgent<S extends Solution<?>> extends HyperHeuristicAgent {

    /**
     * Instantiates a new Random hh agent.
     *
     * @param agentName the agent name
     */
    public RandomHyperHeuristicAgent(String agentName, long uId, long seed) {
        super(agentName, uId, seed);
    }

    @Override
    public void run() {
        this.init();
        int populationSize = (int) this.getAttributeArtifact(problemArtifact, "getQtdPopulation");
        int maxGen = (int) this.getAttributeArtifact(problemArtifact, "getMaxIteration");
        try {
            while (true) {
                int gen = (int) this.getAttributeArtifact(problemArtifact, "getIteration");
                int algStep = (int) this.getAttributeArtifact(problemArtifact, "getAlgStep");
                //System.out.println(this.getAgentName()+" algstep "+algStep);
                if (gen < maxGen) {
                    switch (algStep) {
                        case -1:
                            //calculate each one participation
                            Arrays.fill(qtdSolutions, 0);
                            int size = populationSize;
                            for (int i = 0; i < qtdSolutions.length - 1; i++) {
                                if (size > 0) {
                                    qtdSolutions[i] = ExtraPseudoRandom.getInstance().nextInt(0, size - 1);
                                }
                                size -= qtdSolutions[i];
                            }
                            qtdSolutions[qtdSolutions.length - 1] = size;
                            System.out.println("\n\n" + Arrays.toString(qtdSolutions));
                            //share population
                            this.populationToSharedPopulation();
                            //set alg to execute
                            this.startMhAgent();
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
                            doAction(problemArtifact, new Op("setIteration", gen + 1));
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
}

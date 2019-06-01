package br.usp.poli.pcs.lti.moabhh.core.votingmethods;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Copeland method.
 */
public class Copeland extends VotingMethod {

    /**
     * Votation method int [ ].
     *
     * @param metricValues the metric values
     * @return the int [ ]
     */
    @Override
    public int[] performVoting(double[][] metricValues) {
        //this.normalizeValues(metricValues);
        int qtdCandidates = metricValues[0].length;
        ArrayList<String> namesInComparision = this.generateComparisionNames(qtdCandidates);
        HashMap<String, Double> pairwiseComparision = this.generatePairWiseComparisionTable(namesInComparision, metricValues);
        return generateCopelandRank(qtdCandidates, namesInComparision, pairwiseComparision);
    }

    public int[] generateCopelandRank(int qtdCandicates, ArrayList<String> namesInComparision, HashMap<String, Double> pairwiseComparision) {
        int[] wins = new int[qtdCandicates];
        int[] losses = new int[qtdCandicates];
        for (String nameComp : namesInComparision) {
            String[] ids = nameComp.split("-");
            int one = Integer.parseInt(ids[0]);
            int other = Integer.parseInt(ids[1]);
            String mirror = other + "-" + one;
            double val1 = pairwiseComparision.getOrDefault(nameComp, 0.0);
            double val2 = pairwiseComparision.getOrDefault(mirror, 0.0);
            if (val1 > val2) {
                wins[one]++;
            } else if (val2 > val1) {
                losses[one]++;
            }
        }
        int[] rank = new int[qtdCandicates];
        for (int i = 0; i < qtdCandicates; i++) {
            rank[i] = wins[i] - losses[i];
        }
        return rank;
    }
}

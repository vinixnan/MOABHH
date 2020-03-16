package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;
import com.google.common.primitives.Doubles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author vinicius
 */
public class Kemeny extends VotingMethod {

    private List<List<Integer>> allpermutation;

    @Override
    public int[] performVoting(double[][] metricValues) {
        //this.normalizeValues(metricValues);
        int qtdCandidates = metricValues[0].length;
        ArrayList<String> namesInComparision = this.generateComparisionNames(qtdCandidates);
        HashMap<String, Double> pairwiseComparision = this.generatePairWiseComparisionTable(namesInComparision, metricValues);
        double[] kemenyRank = generateKemenyRank(qtdCandidates, namesInComparision, pairwiseComparision);
        int maxPos = Doubles.indexOf(kemenyRank, Doubles.max(kemenyRank));
        List<Integer> best = this.allpermutation.get(maxPos);
        int[] toReturn = new int[qtdCandidates];
        int increment = qtdCandidates;
        for (Integer val : best) {
            toReturn[val] = increment;
            increment--;
        }
        return toReturn;
    }

    public double[] generateKemenyRank(int qtdCandicates, ArrayList<String> namesComparision, HashMap<String, Double> pairwiseComparision) {
        generateAllPossibleRankName(qtdCandicates);
        double[] allsums = new double[allpermutation.size()];
        for (int permutId = 0; permutId < allpermutation.size(); permutId++) {
            List<Integer> list = allpermutation.get(permutId);
            double sum = 0;
            for (int i = 0; i < list.size() - 1; i++) {
                int one = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    int other = list.get(j);
                    String key = one + "-" + other;
                    double ax = pairwiseComparision.getOrDefault(key, 0.0);
                    sum += ax;
                }
            }
            allsums[permutId] = sum;
        }
        return allsums;
    }

    protected void generateAllPossibleRankName(int qtdCandidates) {
        allpermutation = new ArrayList<>();
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < qtdCandidates; i++) {
            arr.add(i);
        }
        permute(arr, 0);
    }

    private void permute(List<Integer> arr, int k) {
        for (int i = k; i < arr.size(); i++) {
            java.util.Collections.swap(arr, i, k);
            permute(arr, k + 1);
            java.util.Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1) {
            allpermutation.add(new ArrayList<>(arr));
        }
    }

    public List<List<Integer>> getAllpermutation() {
        return allpermutation;
    }
}

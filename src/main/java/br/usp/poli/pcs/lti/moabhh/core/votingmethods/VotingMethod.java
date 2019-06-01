package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author vinicius
 */
public abstract class VotingMethod {

    protected void normalizeValues(double[][] valuesTable) {
        for (double[] valuesTable1 : valuesTable) {
            double sum = StatUtils.sum(valuesTable1);
            for (int idCandidate = 0; idCandidate < valuesTable1.length; idCandidate++) {
                valuesTable1[idCandidate] = (valuesTable1[idCandidate] / sum) * 100.0;
            }
        }
    }

    /**
     * Prepare comparision array list.
     *
     * @param qtdCandidates
     * @return the array list
     */
    public ArrayList<String> generateComparisionNames(int qtdCandidates) {
        ArrayList<String> toReturn = new ArrayList<>();
        for (int one = 0; one < qtdCandidates; one++) {
            for (int other = 0; other < qtdCandidates; other++) {
                if (one != other) {
                    String name1 = one + "-" + other;
                    String name2 = other + "-" + one;
                    if (!toReturn.contains(name1) && !toReturn.contains(name2)) {
                        toReturn.add(name1);
                        toReturn.add(name2);
                    }
                }
            }
        }
        return toReturn;
    }

    public HashMap<String, Double> generatePairWiseComparisionTable(ArrayList<String> namesInComparision, double[][] valueTable) {
        HashMap<String, Double> pairwiseComparision = new HashMap<>();
        HashMap<String, Double> tiedpairwiseComparision = new HashMap<>();//ver
        for (int idvoter = 0; idvoter < valueTable.length; idvoter++) {
            for (String str : namesInComparision) {
                String[] aux = str.split("-");
                int one = Integer.parseInt(aux[0]);
                int other = Integer.parseInt(aux[1]);
                if (valueTable[idvoter][one] > valueTable[idvoter][other]) {
                    pairwiseComparision.put(str, pairwiseComparision.getOrDefault(str, 0.0) + 1);
                } else if (valueTable[idvoter][one] == valueTable[idvoter][other]) {
                    tiedpairwiseComparision.put(str, pairwiseComparision.getOrDefault(str, 0.0) + 1);
                }
            }
        }
        return pairwiseComparision;
    }
    
    public double[][] returnAllowedMetricValues(double[][] metricValues, int[] qtdSolutions, ArrayList<Integer> allowed){
        ArrayList<ArrayList<Double>> metrics = new ArrayList<>();
        for (int idVoter = 0; idVoter < metricValues.length; idVoter++) {
            ArrayList<Double> aux = new ArrayList<>();
            for (int idCandidate = 0; idCandidate < qtdSolutions.length; idCandidate++) {
                if (qtdSolutions[idCandidate] > 0) {
                    aux.add(metricValues[idVoter][idCandidate]);
                    if (!allowed.contains(idCandidate)) {
                        allowed.add(idCandidate);
                    }
                }
            }
            metrics.add(aux);
        }
        double[][] metricValuesFixed=new double[metricValues.length][allowed.size()];
        for (int idVoter = 0; idVoter < metrics.size(); idVoter++) {
            for (int idCandidate = 0; idCandidate < allowed.size(); idCandidate++) {
                metricValuesFixed[idVoter][idCandidate]=metrics.get(idVoter).get(idCandidate);
            }
        }
        return metricValuesFixed;
    }
    
    public int[] remixAgain(int[] votingRersults, ArrayList<Integer> allowed, int previousSize){
        int[] toReturn=new int[previousSize];
        Arrays.fill(toReturn, Integer.MIN_VALUE);
        int pos=0;
        for(int idCandidate : allowed){
            toReturn[idCandidate]=votingRersults[pos++];
        }
        return toReturn;
    }

    public int[] votationMethod(double[][] metricValues, int[] qtdSolutions) {
        ArrayList<Integer> allowed = new ArrayList<>();
        double[][] metricValuesFixed=returnAllowedMetricValues(metricValues, qtdSolutions, allowed);
        int[] votingResults=performVoting(metricValuesFixed);
        return remixAgain(votingResults, allowed, qtdSolutions.length);
    }
    
    public abstract int[] performVoting(double[][] metricValues);
}

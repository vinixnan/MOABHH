package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

/**
 *
 * @author vinicius
 */
public class VotingOutcomeProcessing {

    public static double functionParticipation(int pos, int n) {
        if (pos == 0) {
            return Math.pow(2, n);
        } else if (pos == (n-1)) {
            return 0;
        } else {
            return Math.pow(2, n - pos);
        }
    }

    /**
     * Fix vector content.
     *
     * @param doubleQts
     * @param populationSize the population size
     * @param randomGenerator
     * @return
     */
    public static int[] discretizeArray(double[] doubleQts, int populationSize, JavaRandomGenerator randomGenerator) {
        int[] qtds = new int[doubleQts.length];
        ArrayList<Integer> allowedList = new ArrayList<>();
        for (int i = 0; i < qtds.length; i++) {
            qtds[i] = (int) Math.floor(doubleQts[i]);
            if (qtds[i] > 0) {
                allowedList.add(i);
            }
        }
        int sum = 0;
        for (int j = 0; j < qtds.length; j++) {
            qtds[j] = Math.min(populationSize, qtds[j]);//nao sei nem se vai usar
            sum += qtds[j];
        }

        int difference = (int) (populationSize - sum);
        //System.out.print(Arrays.toString(qtds) + " " + difference + " ");
        while (difference > 0) {
            int chosen = allowedList.get(randomGenerator.nextInt(0, allowedList.size()-1));
            qtds[chosen]++;
            difference--;
        }
        //System.out.println(Arrays.toString(qtds) + " " + difference);
        return qtds;
    }

    public static double[] generateParticipation(HashMap<Integer, ArrayList<Integer>> rankAlgs, int qtdAgents, double beta, int n) {
        ArrayList<Integer> rank = new ArrayList<>(rankAlgs.keySet());
        //System.out.println(" rank size " + n);
        double[] toReturn = new double[qtdAgents];
        double sum = 0;
        for (int pos : rank) {
            ArrayList<Integer> elements = rankAlgs.get(pos);
            for (Integer id : elements) {
                toReturn[id] = VotingOutcomeProcessing.functionParticipation(pos, n);
                if (toReturn[id] > 0) {
                    sum += toReturn[id];
                }
            }
        }
        for (int pos : rank) {
            ArrayList<Integer> elements = rankAlgs.get(pos);
            for (Integer id : elements) {
                toReturn[id] = toReturn[id] / sum;
            }
        }
        //System.out.println("Resulting from function " + Arrays.toString(toReturn));
        return toReturn;
    }

    public static void qtdUpdateMethod(double[] qtdSolutions, HashMap<Integer, ArrayList<Integer>> rankAlgs, double increment, int populationSize, int n) {
        double[] partOfIncrement = generateParticipation(rankAlgs, qtdSolutions.length, increment, n);
        ArrayList<Integer> rank = new ArrayList<>(rankAlgs.keySet());
        //find the total debt
        //System.out.println(increment);
        double debt = 0;
        for (int pos : rank) {
            ArrayList<Integer> elements = rankAlgs.get(pos);
            for (Integer id : elements) {
                if (partOfIncrement[id] == 0 && qtdSolutions[id] > 0) {
                    double valueToTake = Math.min(qtdSolutions[id] * increment, qtdSolutions[id]);//take a percentual
                    debt += valueToTake;
                    qtdSolutions[id] -= valueToTake;
                }
            }
        }
        //pay the debt
        for (int pos : rank) {
            ArrayList<Integer> elements = rankAlgs.get(pos);
            for (Integer id : elements) {
                if (partOfIncrement[id] > 0 && qtdSolutions[id] > 0) {
                    double valueToTake = partOfIncrement[id] * debt;
                    qtdSolutions[id] += valueToTake;
                }
            }
        }
        //System.out.println(Arrays.toString(qtdSolutions));
    }

    public static void votesProcessing(double[] qtdSolutions, int[] discreteQtdSolutions, int[] votationResults, double increment,
            int populationSize) {
        //FIND qtd of down algs
        //Sort elements 
        HashMap<Integer, ArrayList<Integer>> rankAlgs = new HashMap<>();
        int[] votes = Arrays.copyOf(votationResults, votationResults.length);
        int n=0;
        int max = Ints.max(votes);
        int lastPos = 0;
        double lastValue = max;
        ArrayList<Integer> currentPositionArray = new ArrayList<>();
        while (max != Integer.MIN_VALUE) {
            int maxPos = Ints.indexOf(votes, max);
            if (discreteQtdSolutions[maxPos] > 0) {
                if (lastValue != max) {
                    rankAlgs.put(lastPos, currentPositionArray);
                    lastPos++;
                    currentPositionArray = new ArrayList<>();
                }
                lastValue = max;
                currentPositionArray.add(maxPos);
                n++;
            }
            votes[maxPos] = Integer.MIN_VALUE;
            max = Ints.max(votes);
        }
        if (!currentPositionArray.isEmpty()) {
            rankAlgs.put(lastPos, currentPositionArray);
        }
        VotingOutcomeProcessing.qtdUpdateMethod(qtdSolutions, rankAlgs, increment, populationSize, rankAlgs.size());
    }
}

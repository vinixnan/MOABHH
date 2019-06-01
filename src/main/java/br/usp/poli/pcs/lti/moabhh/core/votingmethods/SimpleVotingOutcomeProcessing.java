package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import com.google.common.primitives.Doubles;
import java.util.Arrays;

/**
 *
 * @author vinicius
 */
public class SimpleVotingOutcomeProcessing {

    

    public static double[] generateParticipation(int[] votationResults, int[] discreteQtdSolutions, int qtdAgents) {
        double[] toReturn = new double[qtdAgents];
        int sum=0;
        for (int i = 0; i < votationResults.length; i++) {
            if(discreteQtdSolutions[i] > 0){
                sum+=Integer.max(votationResults[i], 0);
            }
        }
        for (int i = 0; i < votationResults.length; i++) {
            if(discreteQtdSolutions[i] > 0){
                toReturn[i]=((double)votationResults[i])/((double)sum) ;
            }
        }
        //System.out.println("Resulting from function " + Arrays.toString(toReturn));
        return toReturn;
    }

    public static void qtdUpdateMethod(int[] votationResults, double[] qtdSolutions, int[] discreteQtdSolutions, double increment, int populationSize) {
        double[] partOfIncrement = generateParticipation(votationResults, discreteQtdSolutions, qtdSolutions.length);
        
        //find the total debt
        //System.out.println(increment);
        double debt = 0;
        double minValueAllowed=Double.MAX_VALUE;
        for (int id = 0; id < discreteQtdSolutions.length; id++) {
            if (discreteQtdSolutions[id] > 0 && partOfIncrement[id] < minValueAllowed){
                minValueAllowed=partOfIncrement[id];
            }
        }
        
        for (int id = 0; id < qtdSolutions.length; id++) {
            if (qtdSolutions[id] > 0 && (partOfIncrement[id] < 0  ||  partOfIncrement[id]==minValueAllowed)) {
                if(partOfIncrement[id]==minValueAllowed && partOfIncrement[id] > 0){
                    //System.out.println("eu");
                }
                double valueToTake = Math.min(Math.abs(partOfIncrement[id]) * qtdSolutions[id] * increment, qtdSolutions[id]);//take a percentual
                debt += valueToTake;
                qtdSolutions[id] -= valueToTake;
            }
        }
        for (int id = 0; id < qtdSolutions.length; id++) {
            if (partOfIncrement[id] > 0 && discreteQtdSolutions[id] > 0 && partOfIncrement[id]!=minValueAllowed) {
                double valueToTake = partOfIncrement[id] * debt;
                qtdSolutions[id] += valueToTake;
            }
        }
    }

    public static void votesProcessing(double[] qtdSolutions, int[] discreteQtdSolutions, int[] votationResults, double increment,
            int populationSize) {
        SimpleVotingOutcomeProcessing.qtdUpdateMethod(votationResults, qtdSolutions, discreteQtdSolutions, increment, populationSize);
    }
}

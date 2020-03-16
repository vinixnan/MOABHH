package br.usp.poli.pcs.lti.moabhh.core.votingmethods;


import static org.junit.Assert.assertTrue;


import cartago.AgentId;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class CopelandTest {

    @Test
    public void threeVotersCopelandRank() {
        int qtdVoters = 3;
        int qtdMHAgent = 3;
        double[][] qualities = new double[qtdVoters][qtdMHAgent];
        qualities[0][0] = 0.45;
        qualities[0][1] = 0.51;
        qualities[0][2] = 0.48;
        qualities[1][0] = 0.998;
        qualities[1][1] = 0.688;
        qualities[1][2] = 0.788;
        qualities[2][0] = 0.999;
        qualities[2][1] = 0.966;
        qualities[2][2] = 0.988;

        AgentId[] voters = new AgentId[qtdVoters];
        Copeland cpm = new Copeland();
        int[] resp = cpm.performVoting(qualities);
        int[] expected = new int[]{2, -2, 0};
        Assert.assertArrayEquals(resp, expected);

    }

    @Test
    public void fiveVotersCopelandRank() {
        int qtdVoters = 5;
        int qtdMHAgent = 3;
        double[][] qualities = new double[qtdVoters][qtdMHAgent];
        qualities[0][0] = 0.45;
        qualities[0][1] = 0.51;
        qualities[0][2] = 0.48;
        qualities[1][0] = 0.998;
        qualities[1][1] = 0.688;
        qualities[1][2] = 0.788;
        qualities[2][0] = 0.999;
        qualities[2][1] = 0.966;
        qualities[2][2] = 0.988;

        qualities[3][0] = 1.0;
        qualities[3][1] = 1.0;
        qualities[3][2] = 0.99999;
        qualities[4][0] = 0.356;
        qualities[4][1] = 0.4549;
        qualities[4][2] = 0.3565;

        AgentId[] voters = new AgentId[qtdVoters];
        Copeland cpm = new Copeland();
        int[] resp = cpm.performVoting(qualities);
        int[] expected = new int[]{1, 1, -2};
        Assert.assertArrayEquals(resp, expected);

    }
/*
    @Test
    public void useCopelandRankToUpdate1() {
        double icrement = 3;
        boolean updateQtds = true;
        int populationSize = 100;
        double[] qtdsToall = new double[]{34, 33, 33};
        int[] votes = new int[]{2, 1, -2};
        Copeland cpm = new Copeland();
        VotingOutcomeProcessing.votesProcessing(qtdsToall, votes, icrement, populationSize);
        double[] expected = new double[]{36.0, 34.0, 30.0};
        assertTrue(Arrays.equals(expected, qtdsToall));
    }
*/
    @Test
    public void citiesExampleTest() {
        int qtdVoters = 4;

        HashMap<String, Double> pairwiseComparision = new HashMap<>();
        //0-Memphis, 1-Nashville, 2-Chattanooga, 3-Knoxville
        pairwiseComparision.put("0-0", 0.0);
        pairwiseComparision.put("0-1", 42.0);
        pairwiseComparision.put("0-2", 42.0);
        pairwiseComparision.put("0-3", 42.0);

        pairwiseComparision.put("1-0", 58.0);
        pairwiseComparision.put("1-1", 0.0);
        pairwiseComparision.put("1-2", 68.0);
        pairwiseComparision.put("1-3", 68.0);

        pairwiseComparision.put("2-0", 58.0);
        pairwiseComparision.put("2-1", 32.0);
        pairwiseComparision.put("2-2", 0.0);
        pairwiseComparision.put("2-3", 83.0);

        pairwiseComparision.put("3-0", 58.0);
        pairwiseComparision.put("3-1", 32.0);
        pairwiseComparision.put("3-2", 17.0);
        pairwiseComparision.put("3-3", 0.0);
        
        Copeland cpm = new Copeland();
        ArrayList<String> namesInComparision = cpm.generateComparisionNames(qtdVoters);
        int[] resp=cpm.generateCopelandRank(4, namesInComparision, pairwiseComparision);
        int[] expected = new int[]{-3, 3, 1, -1};
        assertTrue(Arrays.equals(expected, resp));
    }
}

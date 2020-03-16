package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Kemeny;
import com.google.common.primitives.Doubles;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.junit.Assert;

import org.junit.Test;

public class KemenyTest {

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
        
        Kemeny cpm = new Kemeny();
        int[] resp = cpm.performVoting(qualities);
        int[] expected = new int[]{3, 1, 2};
        Assert.assertArrayEquals(resp, expected);
    }
    
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

        Kemeny keme = new Kemeny();
        ArrayList<String> namesComparision = keme.generateComparisionNames(qtdVoters);
        double[] allsums = keme.generateKemenyRank(qtdVoters, namesComparision, pairwiseComparision);

        List<List<Integer>> allcombination = keme.getAllpermutation();
        for (int permutId = 0; permutId < allcombination.size(); permutId++) {
            List<Integer> list = allcombination.get(permutId);
            String name = Arrays.toString(list.toArray()).replace("0", "Memphis").replace("1", "Nashville").replace("2", "Chattanooga").replace("3", "Knoxville");
            System.out.println(name + " " + allsums[permutId]);
        }
        int maxPos = Doubles.indexOf(allsums, Doubles.max(allsums));
        System.out.println("The best is " + maxPos + " with " + allsums[maxPos]);
        assertEquals(allsums[maxPos], 393.0);
        
    }
}

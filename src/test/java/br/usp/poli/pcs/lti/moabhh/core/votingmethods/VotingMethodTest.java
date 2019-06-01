package br.usp.poli.pcs.lti.moabhh.core.votingmethods;


import java.util.ArrayList;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class VotingMethodTest {

    protected Copeland cpm = new Copeland();
    
    protected double[][] dataMass1(){
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
        return qualities;
    }
    
    protected double[][] dataMass2(){
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
        return qualities;
    }
    
    @Test
    public void threeVotersCopelandRank1() {
        double[][] qualities = dataMass1();
        ArrayList<Integer> allowed = new ArrayList<>();
        int[] qtdSolution=new int[qualities[0].length];
        qtdSolution[0]=50;
        qtdSolution[1]=0;
        qtdSolution[2]=50;
        double[][] resp=cpm.returnAllowedMetricValues(qualities, qtdSolution, allowed);
        double[][] expected = new double[3][2];
        expected[0][0] = 0.45;
        expected[0][1] = 0.48;
        expected[1][0] = 0.998;
        expected[1][1] = 0.788;
        expected[2][0] = 0.999;
        expected[2][1] = 0.988;
        Assert.assertArrayEquals(resp, expected);
    }

    @Test
    public void threeVotersCopelandRank2() {
        double[][] qualities = dataMass1();
        ArrayList<Integer> allowed = new ArrayList<>();
        int[] qtdSolution=new int[qualities[0].length];
        qtdSolution[0]=0;
        qtdSolution[1]=0;
        qtdSolution[2]=100;
        double[][] resp=cpm.returnAllowedMetricValues(qualities, qtdSolution, allowed);
        double[][] expected = new double[3][1];
        expected[0][0] = 0.48;
        expected[1][0] = 0.788;
        expected[2][0] = 0.988;
        Assert.assertArrayEquals(resp, expected);
    }
    
    @Test
    public void fiveVotersCopelandRank1() {
        double[][] qualities = dataMass2();
        ArrayList<Integer> allowed = new ArrayList<>();
        int[] qtdSolution=new int[qualities[0].length];
        qtdSolution[0]=50;
        qtdSolution[1]=0;
        qtdSolution[2]=50;
        double[][] resp=cpm.returnAllowedMetricValues(qualities, qtdSolution, allowed);
        double[][] expected = new double[5][2];
        expected[0][0] = 0.45;
        expected[0][1] = 0.48;
        expected[1][0] = 0.998;
        expected[1][1] = 0.788;
        expected[2][0] = 0.999;
        expected[2][1] = 0.988;
        expected[3][0] = 1.0;
        expected[3][1] = 0.99999;
        expected[4][0] = 0.356;
        expected[4][1] = 0.3565;
        Assert.assertArrayEquals(resp, expected);
    }
    
    @Test
    public void remixTest() {
        ArrayList<Integer> allowed = new ArrayList<>();
        allowed.add(0);
        allowed.add(1);
        allowed.add(3);
        allowed.add(4);
        allowed.add(6);
        allowed.add(7);
        int[] votingResults=new int[6];
        Arrays.fill(votingResults, 6);
        int[] resp= cpm.remixAgain(votingResults, allowed, 8);
        int[] expected=new int[8];
        Arrays.fill(expected, 6);
        expected[2]=Integer.MIN_VALUE;
        expected[5]=Integer.MIN_VALUE;
        Assert.assertArrayEquals(resp, expected);
    }
}

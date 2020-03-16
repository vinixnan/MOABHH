package br.usp.poli.pcs.lti.moabhh.core.votingmethods;



import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Borda;
import cartago.AgentId;

import java.util.Arrays;

import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BordaTest {

  @Test
  public void threeVotersBordaRank() {
    System.out.println("threeVotersBordaRank");
    int qtdVoters = 3;
    int qtdMHAgent = 3;
    double[][] qualities = new double[qtdVoters][qtdMHAgent];
    qualities[0][0] = 0.45; //1
    qualities[0][1] = 0.51; //3
    qualities[0][2] = 0.48; //2
    qualities[1][0] = 0.998; //3
    qualities[1][1] = 0.688; //1
    qualities[1][2] = 0.788; //2
    qualities[2][0] = 0.999; //3
    qualities[2][1] = 0.966; //1
    qualities[2][2] = 0.988; //2
            
            //0= 1+3+3=7, 1=3+1+1=5, 2=2+2+2=6

    AgentId[] voters = new AgentId[qtdVoters];
    Borda cpm = new Borda();
    int[] resp = cpm.performVoting(qualities);
    int[] expected = new int[]{7, 5, 6};
    Assert.assertArrayEquals(resp, expected);

  }

  @Test
  public void fiveVotersBordaRank() {
    System.out.println("fiveVotersBordaRank");
    int qtdVoters = 5;
    int qtdMHAgent = 3;
    double[][] qualities = new double[qtdVoters][qtdMHAgent];
    qualities[0][0] = 0.45;//1
    qualities[0][1] = 0.51;//3
    qualities[0][2] = 0.48;//2
    
    qualities[1][0] = 0.998;//3
    qualities[1][1] = 0.688;//1
    qualities[1][2] = 0.788;//2
    
    qualities[2][0] = 0.999;//3
    qualities[2][1] = 0.966;//1
    qualities[2][2] = 0.988;//2

    qualities[3][0] = 1.0;//3
    qualities[3][1] = 1.0;//2
    qualities[3][2] = 0.99999;//1
    
    qualities[4][0] = 0.356;//1
    qualities[4][1] = 0.4549;//3
    qualities[4][2] = 0.3565;//2
    
    //0= 1+3+3=+3+1=11, 1=3+1+1+2+3=10, 2=2+2+2+1+2=9

    AgentId[] voters = new AgentId[qtdVoters];
    Borda cpm = new Borda();
    int[] resp = cpm.performVoting(qualities);
    int[] expected = new int[]{11, 10, 9};
    Assert.assertArrayEquals(resp, expected);

  }
/*
  @Test
  public void useBordaRankToUpdate() {
    System.out.println("useBordaRankToUpdate");
    double icrement = 3;
    boolean updateQtds = true;
    int populationSize = 100;
    double[] qtdsToall = new double[]{34, 33, 33};
    int[] votes = new int[]{7, 5, 6};
    Borda cpm = new Borda();
    VotingOutcomeProcessing.votesProcessing(qtdsToall, votes, icrement, populationSize);
    double[] expected = new double[]{36.0, 30.0, 34.0};
    assertTrue(Arrays.equals(expected, qtdsToall));
  }
*/
}

package br.usp.poli.pcs.lti.moabhh.core.votingmethods;

import br.usp.poli.pcs.lti.moabhh.core.Item;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;


import java.util.ArrayList;
import java.util.Collections;

/**
 * The type Copeland method.
 */
public class Borda extends VotingMethod{


  /**
   * Votation method int [ ].
   *
   * @param metricValues the metric values [qtdVoters][qtdMHAgent]
   * @return the int [ ]
   */
  @Override
  public int[] performVoting(double[][] metricValues) {
    //this.normalizeValues(metricValues);
    int qtdVoters=metricValues.length;
    ArrayList<ArrayList<Item>> votingTable=new ArrayList<>();
    for(int i=0; i < qtdVoters; i++){
      ArrayList<Item> voting=new ArrayList<>();
      for(int j=0; j < metricValues[i].length; j++){
          Item it=new Item(String.valueOf(j), metricValues[i][j]);
          voting.add(it);
      }
      Collections.sort(voting);
      votingTable.add(voting);
    }
    int qtdCandidates=metricValues[0].length;
    int[] bordaCount=new int[qtdCandidates];
    for(int i=0; i < votingTable.size(); i++){
        ArrayList<Item> voting=votingTable.get(i);
        int increment=qtdCandidates;
        for(int j=0; j < voting.size(); j++){
            Item it=voting.get(j);
            int pos=Integer.parseInt(it.getName());
            bordaCount[pos]+=increment;
            increment--;
        }
    }
    return bordaCount;
  }
}

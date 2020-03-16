package br.usp.poli.pcs.lti.moabhh.artifacts;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.ExtraPseudoRandom;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;

import cartago.AgentId;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.HashMap;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * The type Copeland artifact.
 */
public class CopelandArtifact extends Artifact {

  private HashMap<AgentId, double[]> votation;
  private int[] votationResults;

  private int qtdVoted;
  private int qtdToVote;
  
  private VotingMethod votingmethod;
  
  /**
   * The Qtd solutions.
   */
  protected int[] qtdSolutions;
  private HashMap<AgentId, Integer> qtdSolutionsHash;
  
  private AgentId currentWinner;

  /**
   * Instantiates a new Copeland artifact.
   */
  public CopelandArtifact() {
    this.qtdVoted = 0;
    this.qtdToVote = 0;
    this.votation = new HashMap<>();
    this.votationResults = null;
    this.qtdSolutionsHash=new HashMap<>();
    currentWinner=null;
  }

  /**
   * Vote.
   *
   * @param resultList the result list
   * @param agid the agid
   * @param beta the beta
   * @param res the res
   */
  @OPERATION
  public void vote(double[] resultList, AgentId agid, double beta, OpFeedbackParam<Object> res) {
    if (this.votation.get(agid) == null) {
      this.votation.put(agid, resultList);
      this.qtdVoted++;
      if (this.qtdVoted == this.qtdToVote) {
        //AgentId[] agents = this.votation.keySet().toArray(new AgentId[votation.size()]);
        double[][] metrics = this.votation.values().toArray(new double[votation.size()][]);
        this.votationResults = votingmethod.votationMethod(metrics, qtdSolutions);
        //return its id number
        res.set(agid);
      } else {
        res.set(null);
      }
    }
  }

  /**
   * Generate already voted.
   *
   * @param agents the agents
   */
  @OPERATION
  public void generateAlreadyVoted(ArrayList<AgentId> agents) {
    this.qtdToVote = agents.size();
    for (AgentId agid : agents) {
      this.votation.put(agid, new double[0]);
    }
  }

  /**
   * Reset votation.
   */
  @OPERATION
  public void resetVotation() {
    AgentId[] indices = this.votation.keySet().toArray(new AgentId[votation.keySet().size()]);
    for (AgentId index : indices) {
      this.votation.put(index, null);
    }
    this.qtdVoted = 0;
  }

  /**
   * Gets votation results.
   *
   * @param res the res
   */
  @OPERATION
  public void getVotationResults(OpFeedbackParam<Object> res) {
    res.set(votationResults);
  }

  /**
   * Sets votation results.
   *
   * @param votationResults the votation results
   */
  @OPERATION
  public void setVotationResults(int[] votationResults) {
    this.votationResults = votationResults;
  }
  
  /**
   * Gets votation results.
   *
   * @param res the res
   */
  @OPERATION
  public void getQtdOfSolutions(OpFeedbackParam<Object> res) {
    res.set(qtdSolutions);
  }

  /**
   * Sets votation results.
   *
     * @param qtdSolutions
   */
  @OPERATION
  public void setQtdOfSolutions(int[] qtdSolutions) {
    this.qtdSolutions = qtdSolutions;
  }
  
  /**
   * Generate already voted.
   *
   * @param agents the agents
     * @param qtdSolutions
   */
  @OPERATION
  public void generateQtdSolutionsHash(ArrayList<AgentId> agents, int[] qtdSolutions) {
    int i=0;
    for (AgentId agid : agents) {
      this.qtdSolutionsHash.put(agid, qtdSolutions[i++]);
    }
  }
  
  @OPERATION
  public void getQtdOfSolutionsForId(AgentId id, OpFeedbackParam<Object> res) {
    res.set(qtdSolutionsHash.get(id));
  }
  
  /**
   * Sets votation results.
   *
     * @param votingmethod
   */
  @OPERATION
  public void setVotingMethod(VotingMethod votingmethod) {
    this.votingmethod = votingmethod;
  }
  
  @OPERATION
  public void getVotingMethod(OpFeedbackParam<Object> res) {
    res.set(votingmethod);
  }
  
    @OPERATION
    public void findTheWinner(ArrayList<AgentId> agents) {
        int posBest;
        if (votationResults == null) {
            posBest = ExtraPseudoRandom.getInstance().nextInt(0, agents.size()-1);
        } else {
            posBest = Ints.indexOf(votationResults, Ints.max(votationResults));
        }
        currentWinner = agents.get(posBest);
    }

    @OPERATION
    public void getCurrentWinner(OpFeedbackParam<Object> res) {
       res.set(currentWinner);
    }
    
    @OPERATION
  public void setCurrentWinner(AgentId currentWinner) {
    this.currentWinner=currentWinner;
  }
}

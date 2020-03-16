package br.usp.poli.pcs.lti.moabhh.artifacts;

import br.usp.poli.pcs.lti.moabhh.core.PopulationUtils;
import cartago.AgentId;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.uma.jmetal.solution.Solution;

/**
 * The type Population share artifact.
 *
 * @param <S> the type parameter
 */
public class PopulationShareArtifact<S extends Solution<?>> extends Artifact{

  private HashMap<AgentId, List<S>> populationShare;
  
  private HashMap<AgentId, List<S>> offspringShare;

  /**
   * Init.
   *
   * @param agents the agents
   */
  @OPERATION
  public void init(ArrayList<AgentId> agents) {
    this.populationShare = new HashMap<>();
    offspringShare=new HashMap<>();
    for (AgentId agid : agents) {
      this.populationShare.put(agid, new ArrayList<>());
      this.offspringShare.put(agid, new ArrayList<>());
    }
  }

  /**
   * Sets population share.
   *
   * @param agid the agid
   * @param share the share
   */
  @OPERATION
  public void setPopulationShare(AgentId agid, List<S> share) {
    //System.out.println("SET SHARE WITH NO CHANGES " 
    //+ agid.getAgentName() + " ENTER SIZE " + share.size());
    this.populationShare.put(agid, PopulationUtils.realClone(share));
  }

  /**
   * Gets population share.
   *
   * @param agid the agid
   * @param res the res
   */
  @OPERATION
  public void getPopulationShare(AgentId agid, OpFeedbackParam<Object> res) {
    List<S> share = PopulationUtils.realClone(this.populationShare.get(agid));
    res.set(share);
  }

  /**
   * Gets all population share.
   *
   * @param res the res
   */
  @OPERATION
  public void getAllPopulationShare(OpFeedbackParam<Object> res) {
    List<S> toReturn = new ArrayList<>();
    AgentId[] indices = this.populationShare.keySet()
        .toArray(new AgentId[populationShare.keySet().size()]);
    for (AgentId agid : indices) {
      List<S> share = PopulationUtils.realClone(this.populationShare.get(agid));
      toReturn.addAll(share);
    }
    res.set(toReturn);
  }
  
  
  /**
   * Sets population share.
   *
   * @param agid the agid
   * @param share the share
   */
  @OPERATION
  public void setOffspringShare(AgentId agid, List<S> share) {
    this.offspringShare.put(agid, PopulationUtils.realClone(share));
  }

  /**
   * Gets population share.
   *
   * @param agid the agid
   * @param res the res
   */
  @OPERATION
  public void getOffspringShare(AgentId agid, OpFeedbackParam<Object> res) {
    List<S> share = PopulationUtils.realClone(this.offspringShare.get(agid));
    res.set(share);
  }

  /**
   * Gets all population share.
   *
   * @param res the res
   */
  @OPERATION
  public void getAllOffspringShare(OpFeedbackParam<Object> res) {
    List<S> toReturn = new ArrayList<>();
    AgentId[] indices = this.offspringShare.keySet()
        .toArray(new AgentId[offspringShare.keySet().size()]);
    for (AgentId agid : indices) {
      List<S> share = PopulationUtils.realClone(this.offspringShare.get(agid));
      toReturn.addAll(share);
    }
    res.set(toReturn);
  }
  
}

package br.usp.poli.pcs.lti.moabhh.core;

import cartago.AgentId;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The type Agent comparator.
 */
public class AgentComparator implements Comparator<AgentId>, Serializable {

  @Override
  public int compare(AgentId o1, AgentId o2) {
    return o1.getAgentName().compareToIgnoreCase(o2.getAgentName());
  }

}

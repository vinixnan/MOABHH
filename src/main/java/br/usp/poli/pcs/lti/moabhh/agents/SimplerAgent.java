package br.usp.poli.pcs.lti.moabhh.agents;

import br.usp.poli.pcs.lti.moabhh.core.AgentComparator;

import cartago.AgentId;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.ICartagoController;
import cartago.Op;
import cartago.OpFeedbackParam;
import cartago.WorkspaceId;
import cartago.util.agent.Agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

/**
 * The type Simpler agent.
 */
public class SimplerAgent extends Agent {

    /**
     * The Ic.
     */
    protected ICartagoController ic;
    /**
     * The Id.
     */
    protected AgentId id;

    protected long uId;

    protected JavaRandomGenerator randomGenerator;

    /**
     * Instantiates a new Simpler agent.
     *
     * @param agentName the agent name
     * @param uId
     * @param seed
     */
    public SimplerAgent(String agentName, long uId, long seed) {
        super(agentName + "_" + uId);
        this.uId = uId;
        if (seed == -1) {
            this.randomGenerator = new JavaRandomGenerator();
        } else {
            this.randomGenerator = new JavaRandomGenerator(seed);
        }
        try {
            this.ic = CartagoService.getController("main");
        } catch (CartagoException ex) {
            Logger.getLogger(SimplerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Init.
     */
    public void init() {
        this.id = this.getAgentIdEquals(this.getAgentName());
    }

    /**
     * Gets attribute artifact.
     *
     * @param artifactId the artifact id
     * @param methodName the method name
     * @return the attribute artifact
     */
    protected Object getAttributeArtifact(ArtifactId artifactId, String methodName) {
        try {
            OpFeedbackParam<Object> res = new OpFeedbackParam<>();
            Op op = new Op(methodName, res);
            doAction(artifactId, op);
            return res.get();
        } catch (CartagoException ex) {
            Logger.getLogger(AlgorithmAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets agent ids according to ignore list.
     *
     * @param ignoreList the ignore list
     * @return the agent ids according to ignore list
     */
    protected ArrayList<AgentId> getAgentIdsAccordingToIgnoreList(ArrayList<String> ignoreList) {
        try {
            AgentId[] aux = ic.getCurrentAgents();
            ArrayList<AgentId> agents = new ArrayList<>(Arrays.asList(aux));
            Collections.sort(agents, new AgentComparator());
            ArrayList<AgentId> listAgents = new ArrayList<>();
            for (AgentId agent : agents) {
                boolean found = false;
                for (String ignore : ignoreList) {
                    if (agent.getAgentName().contains(ignore)) {
                        found = true;
                    }
                }
                if (!found && agent.getAgentName().contains(String.valueOf(uId))) {
                    listAgents.add(agent);
                }

            }
            return listAgents;
        } catch (CartagoException ex) {
            Logger.getLogger(HyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets agent id equals.
     *
     * @param str the str
     * @return the agent id equals
     */
    protected AgentId getAgentIdEquals(String str) {
        try {
            AgentId[] agents = ic.getCurrentAgents();
            for (AgentId agent : agents) {
                if (agent.getAgentName().equalsIgnoreCase(str)) {
                    return agent;
                }
            }
        } catch (CartagoException ex) {
            Logger.getLogger(HyperHeuristicAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets mh agent ids.
     *
     * @return the mh agent ids
     */
    protected ArrayList<AgentId> getMhAgentIds() {
        ArrayList<String> notInList = new ArrayList<>();
        notInList.add("ProblemManager");
        notInList.add("HH");
        notInList.add("iVoter");
        ArrayList<AgentId> toReturn = this.getAgentIdsAccordingToIgnoreList(notInList);
        Collections.sort(toReturn, new AgentComparator());
        return toReturn;
    }

    /**
     * Gets voter agent ids.
     *
     * @return the voter agent ids
     */
    protected ArrayList<AgentId> getVoterAgentIds() {
        ArrayList<String> notInList = new ArrayList<>();
        notInList.add("ProblemManager");
        notInList.add("HH");
        notInList.add("NSGAII");
        notInList.add("IBEA");
        notInList.add("SPEA2");
        notInList.add("GDE3");
        notInList.add("MOEAD");
        notInList.add("MOMBI");
        notInList.add("HypE");
        notInList.add("ThetaDEA");
        notInList.add("PSO");
        ArrayList<AgentId> toReturn = this.getAgentIdsAccordingToIgnoreList(notInList);
        Collections.sort(toReturn, new AgentComparator());
        return toReturn;
    }

    @Override
    protected ArtifactId makeArtifact(String artifactName, String templateName) throws CartagoException {
        return super.makeArtifact(artifactName + uId, templateName);
    }

    @Override
    protected ArtifactId makeArtifact(WorkspaceId id, String artifactName, String templateName) throws CartagoException {
        return super.makeArtifact(id, artifactName + uId, templateName);
    }

    @Override
    protected ArtifactId lookupArtifact(String artifactName) throws CartagoException {
        return super.lookupArtifact(artifactName + uId);
    }

    @Override
    protected ArtifactId lookupArtifact(WorkspaceId id, String artifactName) throws CartagoException {
        return super.lookupArtifact(id, artifactName + uId);
    }
}

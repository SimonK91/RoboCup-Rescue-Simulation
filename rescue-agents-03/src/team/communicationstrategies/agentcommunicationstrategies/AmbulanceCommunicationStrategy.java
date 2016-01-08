package team.communicationstrategies.agentcommunicationstrategies;


import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import team.AbstractAgent;
import team.AmbulanceAgent;
import team.AmbulanceCentre;
import team.communicationstrategies.CommunicationProtocol;

public class AmbulanceCommunicationStrategy extends AgentCommunicationStrategy {

	public AmbulanceCommunicationStrategy(AmbulanceAgent agent) {
		super(agent);
	}

	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD, String targetID) {
		
		if(_agent.getID().getValue() == getEntityID(iD).getValue())
		{
			EntityID target = getEntityID(targetID);
			switch(type)
			{
			case GO_RESCUE_BURIED:
				break;
			default:
				Logger.debug("Got wrong instruction in ambulance " + type.Short());
				break;
			}
		}
		
	}

	@Override
	protected void HandleInstruction(CommunicationProtocol instructionType, String agentID, String targetID, String positionOfTarget) {
		EntityID agent, target, position;
		
		switch(instructionType)
		{
		case GO_RESCUE_BURIED:
			/*
			agent = getEntityID(agentID);
			target = getEntityID(targetID);
			position = getEntityID(positionOfTarget);
			if(agent.getValue() == -1 || position.getValue() == -1 || target.getValue() == -1)
				return;
			if(agent.getValue() == _agent.getID().getValue())
				_agent.SetTarget(target, position);
			 */
			break;
		case GO_RESCUE_CIVILIAN: //TODO
			break;
		case GO_CLEAR_BLOCKADE:
		case GO_EXTINGUISH_FIRE:
			break;
		default:
		}
	}

	
	
}

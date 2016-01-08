package team.communicationstrategies.agentcommunicationstrategies;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import team.AbstractAgent;
import team.communicationstrategies.CommunicationProtocol;

public class PoliceForceCommunicationStrategy extends AgentCommunicationStrategy {

	public PoliceForceCommunicationStrategy(AbstractAgent<?> agent) {
		super(agent);
	}
	
	
	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD, String targetID) {
		
		if(_agent.getID().getValue() == getEntityID(iD).getValue())
		{
			EntityID target = getEntityID(targetID);
			switch(type)
			{
			case GO_CLEAR_BLOCKADE:
				break;
			default:
				Logger.debug("Got wrong instruction in police agent " + type.Short());
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
		case GO_RESCUE_CIVILIAN:
			break;
		case GO_CLEAR_BLOCKADE:
			/*
			agent = getEntityID(agentID);
			position = getEntityID(positionOfTarget);
			if(agent.getValue() == -1 || position.getValue() == -1)
				return;
			if(agent.getValue() == _agent.getID().getValue())
				_agent.SetTarget(null, position);
			*/
			break;
		case GO_EXTINGUISH_FIRE:
			break;
		default:
		}
	}

}

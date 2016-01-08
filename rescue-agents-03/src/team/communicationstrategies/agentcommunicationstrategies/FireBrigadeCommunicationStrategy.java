package team.communicationstrategies.agentcommunicationstrategies;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import team.AbstractAgent;
import team.communicationstrategies.CommunicationProtocol;

public class FireBrigadeCommunicationStrategy extends AgentCommunicationStrategy {

	public FireBrigadeCommunicationStrategy(AbstractAgent<?> agent) {
		super(agent);
	}

	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD, String targetID) {
		// TODO Auto-generated method stub
		if(_agent.getID().getValue() == getEntityID(iD).getValue())
		{
			EntityID target = getEntityID(targetID);
			switch(type)
			{
			case GO_EXTINGUISH_FIRE:
				
				break;
			default:
				Logger.debug("Got wrong instruction in firebrigade " + type.Short());
				break;
			}
		}
		
		
	}

	@Override
	protected void HandleInstruction(CommunicationProtocol instructionType,
			String agentID, String targetID, String positionOfTarget) {
		// TODO Auto-generated method stub
		
	}

	

	
}

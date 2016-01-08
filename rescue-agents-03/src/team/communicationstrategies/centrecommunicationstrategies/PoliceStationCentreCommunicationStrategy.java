package team.communicationstrategies.centrecommunicationstrategies;

import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import team.AbstractCentre;
import team.AmbulanceCentre;
import team.PoliceStationCentre;
import team.communicationstrategies.CommunicationProtocol;

public class PoliceStationCentreCommunicationStrategy extends CentreCommunicationStrategy {

	public PoliceStationCentreCommunicationStrategy(PoliceStationCentre centre) {
		super(centre);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD, String targetID) {
		EntityID agent, blockade, position;
		switch(type)
		{
		case BLOCKED_BY_BLOCKADE:
			/*
			position = getEntityID(iD);
			if(position.getValue() == -1)
				return;
			((PoliceStationCentre)_centre).AddImportantBlockade(null, position);
			*/
			break;
		case CLEARING_BLOCKADE:
			/*
			blockade = getEntityID(iD);
			position = getEntityID(targetID);
			if(blockade.getValue() == -1 || position.getValue() == -1)
				return;
			((PoliceStationCentre)_centre).RemoveHandledBlockade(blockade, position);
			*/
			break;
		case GOT_NOTHING_TO_DO:
			/*
			agent = getEntityID(iD);
			position = getEntityID(targetID);
			if(agent.getValue() == -1 || position.getValue() == -1)
				return;
			if(_centre.GetModel().getEntity(agent).getURN() != StandardEntityURN.POLICE_FORCE.toString())
				return;
			((PoliceStationCentre)_centre).AddFreePoliceforce(agent, position);
			*/
			break;
		default:
			break;
		}
	}

}

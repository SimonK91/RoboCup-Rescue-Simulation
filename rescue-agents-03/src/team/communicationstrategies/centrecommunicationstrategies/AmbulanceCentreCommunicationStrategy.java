package team.communicationstrategies.centrecommunicationstrategies;

import com.sun.istack.internal.logging.Logger;

import javafx.util.Pair;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import team.AbstractCentre;
import team.AmbulanceCentre;
import team.communicationstrategies.CommunicationProtocol;

public class AmbulanceCentreCommunicationStrategy extends CentreCommunicationStrategy {

	public AmbulanceCentreCommunicationStrategy(AmbulanceCentre centre) {
		super(centre);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD, String targetID) {		
		EntityID agent, position;
		switch(type)
		{
		case I_AM_BURIED:
			/*
			agent = getEntityID(iD);
			position = getEntityID(targetID);
			if(agent.getValue() == -1 || position.getValue() == -1)
				return;
			((AmbulanceCentre)_centre).AddBuriedAgent(agent, position);
			*/
			break;
		case RESCUED_BURIED:
			/*
			agent = getEntityID(iD);
			position = getEntityID(targetID);
			if(agent.getValue() == -1 || position.getValue() == -1)
				return;
			((AmbulanceCentre)_centre).RemoveHandledBuriedAgents(agent, position);
			*/
			break;
		case GOT_NOTHING_TO_DO:
			/*
			agent = getEntityID(iD);
			position = getEntityID(targetID);
			if(agent.getValue() == -1 || position.getValue() == -1)
				return;
			if(_centre.GetModel().getEntity(agent).getURN() != StandardEntityURN.AMBULANCE_TEAM.toString())
				return;
			((AmbulanceCentre)_centre).AddFreeAmbulance(agent, position);
			*/
			break;
		default:
			break;
		}
	}
}

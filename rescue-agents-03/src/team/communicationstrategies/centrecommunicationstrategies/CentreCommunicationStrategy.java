package team.communicationstrategies.centrecommunicationstrategies;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import team.AbstractCentre;
import team.communicationstrategies.AbstractCommunicationStrategy;
import team.communicationstrategies.CommunicationProtocol;

import java.io.UnsupportedEncodingException;
import java.util.Collection;


public abstract class CentreCommunicationStrategy extends AbstractCommunicationStrategy {

	protected AbstractCentre _centre;
	
	@Override
	protected StandardWorldModel getModel()
	{
		return _centre.GetModel();
	}
	
	public CentreCommunicationStrategy(AbstractCentre centre)
	{
		_centre = centre;
	}
	
	@Override
	protected void translateMessage(ChangeSet set, String msg)
    {
    	if(msg.length() == 0)
    	{
    		Logger.debug("Got faulty (empty) message");
    		return;
    	}
		String[] partsOfMsg = msg.split("\\ ");
		
		CommunicationProtocol type = CommunicationProtocol.GetProtocolType(partsOfMsg[0]);
		
		switch (type)
		{
		case FOUND_CIVILIAN:
			break;
		case FOUND_BURIED_CIVILIAN:
			if(partsOfMsg.length != 4)
				return;
			GetCivilianBuried(set, partsOfMsg[1], partsOfMsg[2], partsOfMsg[3]);
			break;
		case FOUND_HELPLESS_CIVILIAN:
			if(partsOfMsg.length != 3)
				return;
			GetCivilianHelpless(set, partsOfMsg[1], partsOfMsg[2]);
			break;
		case FOUND_DYING_CIVILIAN:
			if(partsOfMsg.length != 4)
				return;
			GetCivilianDying(set, partsOfMsg[1], partsOfMsg[2], partsOfMsg[3]);
			break;
		case FOUND_FIRE:
			if(partsOfMsg.length != 3)
				return;
			GetFire(set, partsOfMsg[1], partsOfMsg[2]);
			break;
		case FOUND_BLOCKADE:
			if(partsOfMsg.length != 3)
				return;
			GetBlockade(set, partsOfMsg[1], partsOfMsg[2]);
			break;
		case I_AM_BURIED:
			if(partsOfMsg.length != 3)
				return;
			Logger.debug("Center got someone is buried message");
			GetInstruction(CommunicationProtocol.I_AM_BURIED, partsOfMsg[1], partsOfMsg[2]);
			break;
		case RESCUED_BURIED:
			if(partsOfMsg.length != 3)
				return;
			Logger.debug("Center got rescued buried");
			GetInstruction(CommunicationProtocol.RESCUED_BURIED, partsOfMsg[1], partsOfMsg[2]);
			break;
		case GOT_NOTHING_TO_DO:
			if(partsOfMsg.length != 3)
				return;
			Logger.debug("Center got agent with nothing to do");
			GetInstruction(CommunicationProtocol.GOT_NOTHING_TO_DO, partsOfMsg[1], partsOfMsg[2]);
			break;
		case CLEARING_BLOCKADE:
			if(partsOfMsg.length != 3)
				return;
			Logger.debug("Center got agent clearing blockade");
			GetInstruction(CommunicationProtocol.CLEARING_BLOCKADE, partsOfMsg[1], partsOfMsg[2]);
			break;
		case BLOCKED_BY_BLOCKADE:
			if(partsOfMsg.length != 2)
				return;
			Logger.debug("Center got agent blocked by blockade");
			GetInstruction(CommunicationProtocol.BLOCKED_BY_BLOCKADE, partsOfMsg[1], null);
			break;
		case GO_RESCUE_CIVILIAN:			
		case GO_CLEAR_BLOCKADE:
		case GO_EXTINGUISH_FIRE:
		case GO_RESCUE_BURIED:	
		case EXTINGUISHING_FIRE:	
			break;
		case FAULTY_MESSAGE:
			break;
		default:
			Logger.debug("----------------------- SHOULD NOT HAPPEN type of message!!!! ----------------------------------");
		}
	}
	
    public void communicateChanges(int time, ChangeSet changed)
    {
    }
    
    public void handleMessages(int time, Collection<Command> heard)
    {
    	Logger.debug("----------------------Heard " + time + ": " + heard.size() + "----------------------");
    	ChangeSet set = new ChangeSet();
        for (Command next : heard) {
        		try {
            		byte[] content = ((AKSpeak)next).getContent();
            		String txt = new String(content, "UTF-8");
            		translateMessage(set, txt);
            	} catch(UnsupportedEncodingException uex)
            	{
            		Logger.error(uex.getMessage());
            	}
        }
        _centre.Merge(set);
    }
}
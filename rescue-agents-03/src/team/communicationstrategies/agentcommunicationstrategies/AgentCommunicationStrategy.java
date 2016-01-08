package team.communicationstrategies.agentcommunicationstrategies;

import team.communicationstrategies.AbstractCommunicationStrategy;
import team.communicationstrategies.CommunicationProtocol;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import team.AbstractAgent;

import java.io.UnsupportedEncodingException;
import java.util.Collection;


public abstract class AgentCommunicationStrategy extends AbstractCommunicationStrategy {
	
	protected AbstractAgent<?> _agent;
	
	public AgentCommunicationStrategy(AbstractAgent<?> agent) {	
		_agent = agent;
	}
	
	@Override
	protected StandardWorldModel getModel()
	{
		return _agent.GetModel();
	}
	
	private static boolean DEBUG = false;
	
	private void debug_text(String text)
	{
		if(DEBUG)
			Logger.debug(text);
	}
	
	protected abstract void HandleInstruction(CommunicationProtocol instructionType, String agentID, String targetID, String positionOfTarget);
	
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
		case GO_RESCUE_CIVILIAN:
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
		case GO_CLEAR_BLOCKADE:
			if(partsOfMsg.length != 3) //TODO: CHANGED 4 -> 3.
				return;
			Logger.debug("Agent got go clear blockade");
			HandleInstruction(CommunicationProtocol.GO_CLEAR_BLOCKADE, partsOfMsg[1], null, partsOfMsg[2]);//partsOfMsg[3]);
			break;
		case GO_EXTINGUISH_FIRE:		
		case GO_RESCUE_BURIED:
			if(partsOfMsg.length != 4)
				return;
			//Logger.debug("Agent got go rescue buried");
			HandleInstruction(CommunicationProtocol.GO_RESCUE_BURIED, partsOfMsg[1], partsOfMsg[2], partsOfMsg[3]);
			break;
		case CLEARING_BLOCKADE:
		case EXTINGUISHING_FIRE:
		case GOT_NOTHING_TO_DO:
		case BLOCKED_BY_BLOCKADE:
		case RESCUED_BURIED:
		case FAULTY_MESSAGE:
			break;
		default:
			Logger.debug("----------------------- SHOULD NOT HAPPEN type of message!!!! ----------------------------------");
		}
	}
	
	private byte[] createMessage(ChangeSet changed, CommunicationProtocol type, EntityID e) {
		String msg = type.Short() + " " + e.getValue() + " ";// + EntityURNProtocol.GetStringValue(changed.getEntityURN(e)) + " ";
		Property entityContainingE = _agent.GetModel().getEntity(e).getProperty(StandardPropertyURN.POSITION.toString());
		
		switch(type)
		{
		case FOUND_FIRE:
			Property fire = changed.getChangedProperty(e, StandardPropertyURN.FIERYNESS.toString());
			msg += fire.getValue();
			break;
		case FOUND_BURIED_CIVILIAN:
			msg += entityContainingE.getValue();
			msg += changed.getChangedProperty(e, StandardPropertyURN.BURIEDNESS.toString()).getValue();
			break;
		case FOUND_HELPLESS_CIVILIAN:
			msg += entityContainingE.getValue();
			break;
		case FOUND_DYING_CIVILIAN:
			Property hp = changed.getChangedProperty(e, StandardPropertyURN.HP.toString());
			Property dmg = changed.getChangedProperty(e, StandardPropertyURN.DAMAGE.toString());
			int health = (int)hp.getValue();
			int damage = (int)dmg.getValue();
			//amount of rounds until civilian dies.
			int rounds = (int)Math.ceil(((double)health)/damage);
			msg += entityContainingE.getValue() + " ";
			msg += rounds;
			break;
		case FOUND_BLOCKADE:
			msg += entityContainingE.getValue();
		case FOUND_CIVILIAN:
			break;
		default: 
			debug_text("Protocol unhandled: " + type.toString());
			break;
		}
		//Logger.debug("Created string to send: " + msg);
		//_agent.GetViewer().AddText(msg);
		return msg.getBytes(); 
	}
	
    public void communicateChanges(int time, ChangeSet changed)
    {
    	//_agent.GetViewer().Clear();
    	//_agent.GetViewer().AddText("Sent Time " + time + ":");
    	for (EntityID e : changed.getChangedEntities())
    	{
    		String urn = changed.getEntityURN(e);
    		
    		if(urn.equals(StandardEntityURN.BLOCKADE.toString()))
    		{
    			//_agent.communicateSpeak(time, createMessage(changed, CommunicationProtocol.FOUND_BLOCKADE, e));
    		}
    		else if(urn.equals(StandardEntityURN.BUILDING.toString()))
    		{

    			Property fire = changed.getChangedProperty(e, StandardPropertyURN.FIERYNESS.toString());
    			if(fire != null && (int)fire.getValue() > 0) //&& (int)fire.getValue() < 4)
    			{
    				_agent.communicateSpeak(time, createMessage(changed, CommunicationProtocol.FOUND_FIRE, e));
    			}
    		}
    		else if(urn.equals(StandardEntityURN.CIVILIAN.toString()))
    		{
    			Property hp = changed.getChangedProperty(e, StandardPropertyURN.HP.toString());
    			Property dmg = changed.getChangedProperty(e, StandardPropertyURN.DAMAGE.toString());
    			if(hp != null && dmg != null)
    			{
    				int damage = (int)dmg.getValue();
    				if(damage > 0)
    				{
    					_agent.communicateSpeak(time, createMessage(changed, CommunicationProtocol.FOUND_DYING_CIVILIAN, e));
    				}
    			}
    			else 
    			{
    				Property buried = changed.getChangedProperty(e, StandardPropertyURN.BURIEDNESS.toString());
        			if(buried != null)
        			{
        				_agent.communicateSpeak(time, createMessage(changed, CommunicationProtocol.FOUND_BURIED_CIVILIAN, e));
        			}
        			if(hp != null && (int)hp.getValue() < 10000) //mayhap
        			{
        				_agent.communicateSpeak(time, createMessage(changed, CommunicationProtocol.FOUND_HELPLESS_CIVILIAN, e));
        			}
    			}
    			
    			
    			//Property p = changed.getChangedProperty(e, StandardPropertyURN.)
    		}
    		else if(!e.equals(_agent.getID()))
    		{
    			debug_text("ENTITY: " + e + ": " + changed.getEntityURN(e) + ":");
    			for(Property p : changed.getChangedProperties(e))
    			{
    				debug_text(p.getURN());
    			}
    		}
    	}
    	//_agent.GetViewer().AddText("End of sent time " + time);
    }
    
    public void handleMessages(int time, Collection<Command> heard)
    {
    	//_agent.GetViewer().AddText("Heard time " + time + ":");
    	ChangeSet set = new ChangeSet();
        for (Command next : heard) {
        	try {
        		byte[] content = ((AKSpeak)next).getContent();
        		String txt = new String(content, "UTF-8");
        		debug_text("Heard " + next + txt);
        		translateMessage(set, txt);
        		//_agent.GetViewer().AddText(txt);
        	} catch(UnsupportedEncodingException uex)
        	{
        		Logger.error(uex.getMessage());
        	}
        }
        _agent.Merge(set);
        //_agent.GetViewer().AddText("End of heard time " + time + "\n");
    }
}

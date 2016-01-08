package team.communicationstrategies;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractCommunicationStrategy {
    /**
     * This function is called to broadcast observation
     */
	
    public abstract void communicateChanges(int time, ChangeSet changed);

    /**
     * This function is called with all the messages that are received by the agent
     */
    public abstract void handleMessages(int time, Collection<Command> heard);
	
    protected abstract StandardWorldModel getModel();
    
	protected EntityID getEntityID(String id)
	{
		try 
		{
			EntityID e = new EntityID(Integer.parseInt(id));
			return e;
		}
		catch(NumberFormatException e)
		{
			return new EntityID(-1);
		}
	}
	
	private StandardEntity GetCorrectEntity(String id, StandardEntityURN correctURN)
	{
		EntityID e = getEntityID(id);
		if(e.getValue() == -1)
		{
			Logger.debug("Got faulty message (faulty EntityID)");
			return null;
		}
		StandardEntity ent = getModel().getEntity(e);
		if(ent == null || ent.getStandardURN() != correctURN)
		{
			Logger.debug("Got faulty message (wrong kind of Entity)");
			return null;
		}
		return ent;
	}
	
	private EntityRefProperty GetRefProperty(String val, StandardPropertyURN correctURN)
	{
		try 
		{
			int value = Integer.parseInt(val);
			EntityID e = new EntityID(value);
			return new EntityRefProperty(correctURN, e);
		}
		catch(NumberFormatException e)
		{
			Logger.debug("Got faulty property value\n\n" + val + " " + correctURN + "\n\n");
			return null;
		}
	}
	
	private EntityRefListProperty GetRefListProperty(String val, StandardPropertyURN correctURN)
	{
		try 
		{
			int value = Integer.parseInt(val);
			EntityID e = new EntityID(value);
			List<EntityID> l = new ArrayList<EntityID>();
			l.add(e);
			return new EntityRefListProperty(correctURN, l);
		}
		catch(NumberFormatException e)
		{
			Logger.debug("Got faulty property value\n\n" + val + " " + correctURN + "\n\n");
			return null;
		}
	}
	
	private IntProperty GetIntProperty(String val, StandardPropertyURN correctURN)
	{
		try 
		{
			int value = Integer.parseInt(val);
			return new IntProperty(correctURN, value);
		}
		catch(NumberFormatException e)
		{
			Logger.debug("Got faulty int property");
			return null;
		}
	}
	
	private EntityID createEntityID(String sid)
	{
		try
		{
			int id = Integer.parseInt(sid);
			return new EntityID(id);
		}
		catch(NumberFormatException e)
		{
			Logger.debug("Couldn't create EntityID, probably corrupted communication");
			return null;
		}
	}
	
	protected void GetFire(ChangeSet set, String entityId, String propertyValue)
	{
		StandardEntity e = GetCorrectEntity(entityId, StandardEntityURN.BUILDING);
		if(e == null)
			return;
		IntProperty fire = GetIntProperty(propertyValue, StandardPropertyURN.FIERYNESS);
		if(fire == null)
			return;
		set.addChange(e, fire);
	}
	
	protected void GetCivilianHelpless(ChangeSet set, String civID, String position)
	{
		EntityID id = createEntityID(civID);
		if(id == null)
			return;
		StandardEntity civ = StandardEntityFactory.INSTANCE.makeEntity(StandardEntityURN.CIVILIAN, id);
		EntityRefProperty pos = GetRefProperty(position, StandardPropertyURN.POSITION);
		if(civ == null || pos == null)
			return;
		set.addChange(civ, pos);
	}
	
	protected void GetCivilianBuried(ChangeSet set, String civID, String position, String buriedness)
	{
		EntityID id = createEntityID(civID);
		if(id == null)
			return;
		StandardEntity civ = StandardEntityFactory.INSTANCE.makeEntity(StandardEntityURN.CIVILIAN, id);
		EntityRefProperty pos = GetRefProperty(position, StandardPropertyURN.POSITION);
		EntityRefProperty buri = GetRefProperty(buriedness, StandardPropertyURN.BURIEDNESS);
		if(civ == null || pos == null || buri == null)
			return;
		set.addChange(civ, pos);
		set.addChange(civ, buri);
	}
	
	protected void GetCivilianDying(ChangeSet set, String civID, String position, String srounds)
	{
		GetCivilianHelpless(set, civID, position);
		try
		{
			int rounds = Integer.parseInt(srounds);
			//TODO Use to actually do something...
		}
		catch(NumberFormatException e)
		{
			return;
		}
	}
	
	protected void GetBlockade(ChangeSet set, String block, String road)
	{
		EntityID blockID = createEntityID(block);
		EntityID roadID = createEntityID(road);
		if(blockID == null || roadID == null)
			return;
		StandardEntity roadEntity = getModel().getEntity(roadID);
		if(roadEntity == null) // all roads exists in all models, therefore if this happens message is corrupted
			return;
		
		Property blockadeList = GetRefListProperty(blockID.toString(), StandardPropertyURN.BLOCKADES);
		set.addChange(roadEntity, blockadeList);
	}
	
	protected abstract void GetInstruction(CommunicationProtocol type, String iD, String targetID);
	
	protected abstract void translateMessage(ChangeSet set, String msg);
	
}
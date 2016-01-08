package team;

import team.communicationstrategies.centrecommunicationstrategies.AmbulanceCentreCommunicationStrategy;
import team.viewers.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 A ambulance centre agent.
 */
public class AmbulanceCentre extends AbstractCentre {
	
	private CenterViewer view;
	
	public AmbulanceCentre(){		
	}
	
	 @Override
	 protected void postConnect() {
		 communicationStrategy = new AmbulanceCentreCommunicationStrategy(this);
		 super.postConnect();
		 //view = new CenterViewer(this.GetModel(), this.config);
         //view.postConnect();
	 }
	
    @Override
    public String toString() {
        return "Ambulance centre";
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        communicationStrategy.communicateChanges(time, changed);
        communicationStrategy.handleMessages(time, heard);
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channels 1 and 2
            sendSubscribe(time, 3);
        }
        //view.handleTimestep(time);
        sendRest(time);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
    }
}

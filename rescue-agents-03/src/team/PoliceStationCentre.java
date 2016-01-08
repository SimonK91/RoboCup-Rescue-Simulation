package team;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;
import team.communicationstrategies.centrecommunicationstrategies.AmbulanceCentreCommunicationStrategy;
import team.communicationstrategies.centrecommunicationstrategies.PoliceStationCentreCommunicationStrategy;

/**
 A sample centre agent.
 */
public class PoliceStationCentre extends AbstractCentre {
    @Override
    public String toString() {
        return "Police station centre";
    }
    
    @Override
	 protected void postConnect() {
		 communicationStrategy = new PoliceStationCentreCommunicationStrategy(this);
		 super.postConnect();
		 //view = new CenterViewer(this.GetModel(), this.config);
        //view.postConnect();
	 }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        communicationStrategy.communicateChanges(time, changed);
        communicationStrategy.handleMessages(time, heard);

        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channels 1 and 2
            sendSubscribe(time, 1);
        }
        sendRest(time);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
    }
}

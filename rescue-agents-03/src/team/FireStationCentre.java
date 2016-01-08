package team;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import team.communicationstrategies.centrecommunicationstrategies.FireStationCommunicationStrategy;
import team.prediction.FirePrediction;
import team.viewers.FirePredictViewer;

/**
 A sample centre agent.
 */
public class FireStationCentre extends AbstractCentre {
	private FirePrediction prediction;
	FirePredictViewer view;
    @Override
    public String toString() {
        return "Fire station centre";
    }
    
    @Override
	 protected void postConnect() {
		 communicationStrategy = new FireStationCommunicationStrategy(this);
		 super.postConnect();
		 
//         prediction = new FirePrediction(this);
//		 view = new FirePredictViewer(this, config);
//		 view.postConnect();
	 }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        communicationStrategy.communicateChanges(time, changed);
        communicationStrategy.handleMessages(time, heard);

        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channels 1 and 2
            sendSubscribe(time, 2);
        }
        sendRest(time);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_STATION);
    }

    public FirePrediction getPrediction(){
    	return prediction;
    }
}

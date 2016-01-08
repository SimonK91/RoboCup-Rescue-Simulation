package team.communicationstrategies;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;

public class DummyCommunicationStrategy extends AbstractCommunicationStrategy {

	public void communicateChanges(int time, ChangeSet changed)
    {

    }
    public void handleMessages(int time, Collection<Command> heard)
    {
        for (Command next : heard) {
            if (next instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)next;
                Logger.debug("Received " + speak.getContent().toString() + " from agent " + speak.getAgentID());
            }
        }

    }
	@Override
	protected StandardWorldModel getModel() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void GetInstruction(CommunicationProtocol type, String iD,
			String targetID) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void translateMessage(ChangeSet set, String msg) {
		// TODO Auto-generated method stub
		
	}

}

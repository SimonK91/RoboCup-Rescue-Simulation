package team;

import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import team.communicationstrategies.AbstractCommunicationStrategy;
import team.communicationstrategies.DummyCommunicationStrategy;
import team.communicationstrategies.centrecommunicationstrategies.CentreCommunicationStrategy;

/**
   A sample centre agent.
 */
public abstract class AbstractCentre extends StandardAgent<Building> {
   /**
    * Communication strategy
    */
   protected AbstractCommunicationStrategy communicationStrategy;
   
   protected void postConnect() {
      //communicationStrategy = new DummyCommunicationStrategy();
   }
   
   public void communicateSpeak(int time, int channel, byte[] data) {
	   sendSpeak(time, channel, data);
   }
   
   public void Merge(ChangeSet set) {
	   model.merge(set);
   }
   
   public StandardWorldModel GetModel() {
	   return model;
   }
   
}

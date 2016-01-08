package team;

import java.io.UnsupportedEncodingException;
import java.util.*;

import team.viewers.*;
import rescuecore2.messages.control.KASense;
import rescuecore2.messages.Command;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.Constants;
import rescuecore2.log.Logger;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.standard.kernel.comms.StandardCommunicationModel;
import team.communicationstrategies.AbstractCommunicationStrategy;

/**
   Abstract base class for sample agents.
   @param <E> The subclass of StandardEntity this agent wants to control.
 */
public abstract class AbstractAgent<E extends StandardEntity> extends StandardAgent<E> {
    private static final int RANDOM_WALK_LENGTH = 50;

    private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
    private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();

    private Map<EntityID, Set<EntityID>> neighbours_graph;
    
    /**
       The search algorithm.
    */
    protected AbstractPathPlanner search;

    /**
     * Obstacle detection algorithm
     */
    protected AbstractObstacleDetector obstacleDetector;

    /**
     * Communication strategy
     */
    protected AbstractCommunicationStrategy communicationStrategy;
    
    /**
     * Communication viewer
     */
    protected CommunicationViewer communicationViewer;
    
    /**
       Whether to use AKSpeak messages or not.
    */
    protected boolean useSpeak;

    /**
       Cache of building IDs.
    */
    protected List<EntityID> buildingIDs;

    /**
       Cache of road IDs.
    */
    protected List<EntityID> roadIDs;

    /**
       Cache of refuge IDs.
    */
    protected List<EntityID> refugeIDs;

    
    /** 
       Channel that the agent uses.
     */
    protected int Channel = 0;


    /**
       Construct an AbstractAgent.
    */
    protected AbstractAgent() {
    }
    
    public void communicateSpeak(int time, byte[] data)
    {
    	String speak = "Communicating: " + " time: " + time + ", channel: " + Channel + ", data: ";
    	try {
    		String txt = new String(data, "UTF-8");
    		speak += txt;

    	} catch(UnsupportedEncodingException uex)
    	{
    		Logger.error(uex.getMessage());
    		Logger.debug("\t\t" + speak + "couldn't read data");
    	}
    	
    	sendSpeak(time, Channel, data);
    }

    private void updateGraph() {
        neighbours_graph = new LazyMap<EntityID, Set<EntityID>>() {
            @Override
            public Set<EntityID> createValue() {
                return new HashSet<EntityID>();
            }
        };
        for (Entity next : model) {
            if (next instanceof Area) {
                Collection<EntityID> areaNeighbours = ((Area)next).getNeighbours();
                neighbours_graph.get(next.getID()).addAll(areaNeighbours);
            }
        }
    }
    
    protected void updateModel(Entity e) {
    	model.addEntity(e);
    }
    
    public void Merge(ChangeSet set) {
 	   model.merge(set);
    }
    
    public StandardWorldModel GetModel() {
    	return model;
    }
    
    public CommunicationViewer GetViewer() {
    	return communicationViewer;
    }
    
    /*
    @Override
    protected void processSense(KASense sense) {
        model.merge(sense.getChangeSet());
        Collection<Command> heard = sense.getHearing();
        think(sense.getTime(), sense.getChangeSet(), heard);
    }
    */
    @Override
    protected void postConnect() {
        super.postConnect();
        
        buildingIDs = new ArrayList<EntityID>();
        roadIDs = new ArrayList<EntityID>();
        refugeIDs = new ArrayList<EntityID>();
        for (StandardEntity next : model) {
            if (next instanceof Building) {
                buildingIDs.add(next.getID());
            }
            if (next instanceof Road) {
                roadIDs.add(next.getID());
            }
            if (next instanceof Refuge) {
                refugeIDs.add(next.getID());
            }
        }
        //communicationViewer = new CommunicationViewer(this, config, model);
        //communicationViewer.postConnect();
        obstacleDetector = new team.obstacledetectors.ObstacleDetector(model);
        updateGraph();
        search = new team.pathplanners.BreadthFirstSearchPathPlanner(neighbours_graph);
        useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
        Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
        Logger.debug(useSpeak ? "Using speak model" : "Using say model");
    }

    /**
       Construct a random walk starting from this agent's current location to a random building.
       @return A random walk.
    */
    protected List<EntityID> randomWalk() {
        List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
        Set<EntityID> seen = new HashSet<EntityID>();
        EntityID current = ((Human)me()).getPosition();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current);
            seen.add(current);
            List<EntityID> possible = new ArrayList<EntityID>(neighbours_graph.get(current));
            Collections.shuffle(possible, random);
            boolean found = false;
            for (EntityID next : possible) {
                if (seen.contains(next)) {
                    continue;
                }
                current = next;
                found = true;
                break;
            }
            if (!found) {
                // We reached a dead-end.
                break;
            }
        }
        return result;
    }
}

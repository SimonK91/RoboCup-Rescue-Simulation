package team.pathplanners;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class RoadPath {

	
	private List<EntityID> myPath = new ArrayList<EntityID>();
	
	private int length = 0;
	private StandardWorldModel world;
	
	private int cost = 0;
	
	
	public RoadPath(StandardWorldModel world) {
		this.world = world;
	}

	public void AddAreaToPath(Area newest){
		if(length > 1)
		{
			Area last = (Area)world.getEntity(myPath.get(length-1));
			cost += world.getDistance(last, newest);
		}
	
		myPath.add(newest.getID());
		length++;
	}

	public List<EntityID> getPath() {
		return myPath;
	}

	public int getCost() {
		return cost;
	}

	public void AddPath(RoadPath mypath2) {
		myPath.addAll(mypath2.getPath());
		
	}
	
	
	
}

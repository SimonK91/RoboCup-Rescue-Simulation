package team.prediction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import team.prediction.learning.Saveable;
import team.prediction.learning.Stringify;

public class FireBuilding{
	private Building building = null;
	private double temperature;
	private int fieryness;
	private int time;
	private EntityID ID;

	public FireBuilding(EntityID eID, StandardWorldModel world, int timeStamp){
		StandardEntity entity = world.getEntity(eID);
		time = timeStamp;
		if(entity instanceof Building){
			Building b = (Building)entity;
			setup(b);
		}
	}

	public FireBuilding(EntityID eID, StandardWorldModel world){
		StandardEntity entity = world.getEntity(eID);
		time = -1;
		if(entity instanceof Building){
			Building b = (Building)entity;
			setup(b);
		}
	}
	public FireBuilding(Building building, int timeStamp){
		time = timeStamp;
		setup(building);
	}
	public FireBuilding(Building building){
		time = -1;
		setup(building);
	}
	public FireBuilding(EntityID eID){
		this.ID = eID;
	}
	
	public FireBuilding(FireBuilding bd) {
		this.ID = bd.ID;
		this.building = bd.building;
		this.temperature = bd.temperature;
		this.fieryness = bd.fieryness;
		this.time = bd.time;
	}

	private void setup(Building building){
		this.building = building;
		if(building.isFierynessDefined()){
			this.fieryness = building.getFieryness();
		}else{
			this.fieryness = 0; // TODO: change to 0 (this is debug purpose)
		}
		if(building.isTemperatureDefined()){
			this.temperature = building.getTemperature();
		}else{
			this.temperature = 100;
		}
		this.ID = building.getID();
	}
	public int getFieryness(){
		return fieryness;
	}
	
	public int getTemperature(){
		return (int)temperature;
	}
	public Building getBuilding(){
		return building;
	}
	public boolean isValid(){
		return building != null;
	}
	public List<Double> getData(){
		List<Double> data = new ArrayList<Double>();
		data.add(new Double(getTemperature()));
		data.add(new Double(fieryness/5.0));
		return data;
	}
	public void increaseIntensity(double val){
		temperature += val;
	}
	public void setFieryness(double fieryness){
		this.fieryness = (int) (fieryness+0.5);
	}
	public void setTemperature(int temperature){
		this.temperature =temperature;
	}
	
	public EntityID getID(){
		return ID;
	}
	
	public Collection<StandardEntity> getBuildingsWithinAffection(StandardWorldModel world, int range){
		return world.getObjectsInRange(building.getID(), range);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof FireBuilding){
			return ((FireBuilding) o).ID.equals(ID);
		}
		return false;
	}
}
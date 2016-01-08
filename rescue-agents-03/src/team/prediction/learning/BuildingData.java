package team.prediction.learning;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;
import team.prediction.FireBuilding;

public class BuildingData extends Saveable{
	public Integer ID;
	public Integer temperature;
	public Integer fieryness;
	
	public BuildingData(BuildingData other){
		this.ID = new Integer(other.ID);
		this.fieryness = new Integer(other.fieryness);
		this.temperature = new Integer(other.temperature);
	}

	public BuildingData(FireBuilding other){
		this.ID = new Integer(other.getID().getValue());
		this.fieryness = new Integer(other.getFieryness());
		this.temperature = new Integer(other.getTemperature());
	}

	public BuildingData(int ID, int temperature, int fieryness){
		this.ID = new Integer(ID);
		this.temperature = new Integer(temperature);
		this.fieryness = new Integer(fieryness);
	}

	public BuildingData(EntityID eID){
		this.ID = new Integer(eID.getValue());
	}

	public BuildingData(Building b){
		this.ID = new Integer(b.getID().toString());
		this.temperature = new Integer(b.getTemperature());
		this.fieryness = new Integer(b.getFieryness());
	}
	
	public BuildingData(String str){
		super(str);
	}
	
	public List<Double> getData(){
		List<Double> data = new ArrayList<Double>();
		data.add(new Double(temperature));
		data.add(new Double(fieryness));
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BuildingData){
			BuildingData bd = (BuildingData)obj;
			return (bd.ID.equals(ID));
//			   	 &&	bd.temperature == temperature
//				 && bd.fieryness == fieryness);
		}
		return false;
	};
	
	@Override
	public String toString() {
		return Stringify.serialize(ID, temperature, fieryness);
	}

	@Override
	protected void loadFromString(List<String> strs) {
		ID = new Integer(strs.get(0));
		temperature = new Integer(strs.get(1));
		fieryness = new Integer(strs.get(2));
	};
}

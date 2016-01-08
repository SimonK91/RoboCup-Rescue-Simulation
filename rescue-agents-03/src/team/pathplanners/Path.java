package team.pathplanners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;


import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import team.AbstractObstacleDetector;
import team.obstacledetectors.DummyObstacleDetector;

public class Path implements Comparable<Path>{
	List<Area> area;
	Path parent;
	int hValue = 0;
	int gValue;
	

	Path(Area area, Path parent, StandardWorldModel world){
		this.area = new ArrayList<Area>();
		this.area.add(area);
		this.parent = parent;
		if(parent != null){
			this.gValue = parent.gValue + world.getDistance(area, parent.getArea());
		}
	}
	Path(Area current, Area goal, Path parent, StandardWorldModel world){
		this(current,parent, world);
		this.hValue = world.getDistance(current, goal);
	}

	Path(List<Area> area, int areaLength, Path parent, StandardWorldModel world){
		this.area = area;
		this.parent = parent;
		this.gValue = areaLength;
		if(parent != null){
			this.gValue += parent.gValue + world.getDistance(area.get(0), parent.getArea());
		}
	}
	Path(List<Area> area, int areaLength, Area goal, Path parent, StandardWorldModel world){
		this(area, areaLength,  parent, world);
		this.hValue = world.getDistance(area.get(area.size()-1), goal);
	}
	public List<Area> toList(){
		if(parent != null){
			List<Area> list = parent.toList();
			list.addAll(area);
			return list;
		}
		List<Area> retVal = new ArrayList<Area>();
		retVal.addAll(area);
		return retVal;
	}
	Area getArea(){
		return area.get(area.size()-1);
	}
	int fValue(){
		return hValue+gValue;
	}
	@Override
	public int compareTo(Path other) {
		return fValue() - (other).fValue();
	}

	public static Pair<Path, Set<EntityID>> getRawPath(Area from, Area to, StandardWorldModel world){
		PriorityQueue<Path> queue = new PriorityQueue<Path>();
		Set<EntityID> explored = new HashSet<EntityID>();
		Path root = new Path(from, to, null, world);
		queue.add(root);
		while(!queue.isEmpty()){
			Path current = queue.poll();
			explored.add(current.getArea().getID());
			List<EntityID> neighbours = current.getArea().getNeighbours();
			if(current.getArea().equals(to)){
				return new Pair<Path, Set<EntityID>>(current, explored);
			}
			for(EntityID eID : neighbours){
				Entity e = world.getEntity(eID);
				if(e instanceof Area && !explored.contains(eID)){
					Area area = (Area)e;

					// Keep expanding search on this path
					Path path = new Path(area,to, current, world);
					queue.add(path);

				}
			}
		}
		return new Pair<Path, Set<EntityID>>(null, explored);
	}

	private static List<Area> entityListToAreaList(LinkedList<EntityID> path,
			StandardWorldModel world) {
		List<Area> retVal = new ArrayList<Area>(path.size());
		for(EntityID eID : path){
			retVal.add((Area)world.getEntity(eID));
		}
		return retVal;
	}
	public static Pair<List<Area>, Set<EntityID>> getPath(Area from, Area to, StandardWorldModel world){
		Pair<Path, Set<EntityID>> raw = getRawPath(from,to, world);
		if(raw.first != null){
			return new Pair<List<Area>, Set<EntityID>>(raw.first.toList(),raw.second);
		}else{
			return new Pair<List<Area>, Set<EntityID>>(null, raw.second);
		}
	}
	public static int getCost(Area from, Area to, StandardWorldModel world){
		Pair<Path, Set<EntityID>> raw = getRawPath(from,to, world);
		return raw.first.gValue;
	}
	
	public int getGValue(){
		return gValue;
	}
	
	public static Pair<Path, Set<EntityID>> getRawPath(Area from, Area to, StandardWorldModel world, LongRoad longroad, AbstractObstacleDetector obsDetector){
		PriorityQueue<Path> queue = new PriorityQueue<Path>();
		Set<EntityID> explored = new HashSet<EntityID>();
		Path root = new Path(from, to, null, world);
		queue.add(root);
		while(!queue.isEmpty()){
			Path current = queue.poll();
			explored.add(current.getArea().getID());
			List<EntityID> neighbours = current.getArea().getNeighbours();
			if(current.getArea().equals(to)){
				return new Pair<Path, Set<EntityID>>(current, explored);
			}
			for(EntityID eID : neighbours){
				Entity e = world.getEntity(eID);
				if(e instanceof Area && !explored.contains(eID) && longroad.getAreas().contains(e) && notBlocked(current.getArea(), (Area)e, longroad, obsDetector) ){
					Area area = (Area)e;

					// Keep expanding search on this path
					Path path = new Path(area,to, current, world);
					queue.add(path);

				}
			}
		}
		return new Pair<Path, Set<EntityID>>(null, explored);
	}
	private static boolean notBlocked(Area from, Area to, LongRoad longroad, AbstractObstacleDetector obsDetector) {
		if(longroad.getBlockadesOnPath().contains(from) || longroad.getBlockadesOnPath().contains(to))
		{
			return obsDetector.isPassable(from, to);	
		}
		return true;
	}
}
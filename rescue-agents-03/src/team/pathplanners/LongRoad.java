package team.pathplanners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import team.AbstractObstacleDetector;


public class LongRoad{
	private static int maxID = 0;
	private Color roadColor;
	static StandardWorldModel world;
	int myID;
	private boolean isPathBlocked = false;
	
	private List<Area> blockadesOnPath = new ArrayList<Area>();
	private List<Area> blockadesNotOnPath = new ArrayList<Area>();
	

	private List<Pair<Area,Area>> neighbourRoadConnections = new ArrayList<Pair<Area,Area>>();
	static void setWorld(StandardWorldModel world){
		LongRoad.world = world;
	}
	
	/**
	 * List containing all Areas of the LongRoad
	 */
	List<Area> roadGroup= new ArrayList<Area>();

	Set<Area>  blockedAreas = new HashSet<Area>();
	/**
	 * List with all neighbouring longroads
	 */
	List<LongRoad> neighbours = new ArrayList<LongRoad>();
	
	/**
	 * A Linked list containing the path from one end to the other of the entire LongRoad
	 */

	LinkedList<EntityID> roadPath = new LinkedList<EntityID>();

	/**
	 * A Linked list containing the path from one end to the other of the entire LongRoad
	 */

	LinkedList<EntityID> reverseRoadPath = new LinkedList<EntityID>();

	/**
	 * The distance of the {@link #roadPath}
	 */
	int length;
	private AbstractObstacleDetector obsDetector;


	/**
	 * Constructor with no LongRoad connections established,
	 * Initializes ID, and road color
	 */
	public LongRoad(AbstractObstacleDetector obsDetector){
		myID = maxID++;
		length = 0;
		Random r = new Random();
		roadColor = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
		this.obsDetector = obsDetector;
	}
	/**
	 * Creating a longroad with connections to each longroad in the list
	 * @param connections
	 */
	public LongRoad(List<LongRoad> connections, AbstractObstacleDetector obsDetector){
		this(obsDetector);
		for(LongRoad c : connections){
			connect(c);
		}
	}
	/**
	 * Creating a longroad with one connection
	 * @param connection
	 */
	public LongRoad(LongRoad connection, AbstractObstacleDetector obsDetector){
		this(obsDetector);
		connect(connection);
	}

	/**
	 * Connects a longroad to another longroad, the connection is made for both longroads.
	 * @param c
	 */
	public void connect(LongRoad c){
		if(c != null && !neighbours.contains(c)){
			neighbours.add(c);
			c.neighbours.add(this);
		}
	}

	/**
	 * Connects a list of longroads to this longroad (the connection is made to both longroads).
	 * @param connections
	 */
	public void connect(List<LongRoad> connections){
		for(LongRoad c : connections){
			connect(c);
		}
	}

	/**
	 * Disconnect a list of longroads to this list.
	 * @param connections
	 */
	public void disconnect(List<LongRoad> connections){
		for(LongRoad c : connections){
			disconnect(c);
		}
	}
	/**
	 * Disconnecting a longroad from this longroads' neighbours
	 * @param c
	 */
	public void disconnect(LongRoad c){
		if(c != null && neighbours.contains(c)){
			neighbours.remove(c);
			c.neighbours.remove(this);
		}
	}

	/**
	 * Adds an area to this LongRoad.
	 * This area will not be added to {@link #roadPath}.
	 * @param newArea
	 * @return the old LongRoad connected to the area added, null if none. 
	 */
	public void addArea(Area newArea){
		if(newArea == null) return;
		roadGroup.add(newArea);
	}
	/**
	 * Adds a list of areas to this LongRoad, see {@link LongRoad#addArea(Area)}
	 * @param areas
	 */
	public void addAreas(List<? extends Area> areas){
		if(areas == null) return;
		for(Area a : areas){
			addArea(a);
		}
	}

	/**
	 * Function to get a list of all areas belonging to the LongRoad
	 * @return {@link #roadGroup}
	 */
	public List<Area> getAreas(){
		return roadGroup;
	}
	
	public Color getRoadColor(){
		return roadColor;
	}
	
	/**
	 * Adds a new LongRoad segment from the provided Area.
	 * Returns a Pair containing on first: all non-deadend Road neighbours, second: all deadend Road neighbours
	 * @param current
	 * @param world
	 * @return <b>Pair with two lists of areas</b><br>
	 * 			<i>pair.first:</i> non-deadend neighbours<br>
	 * 			<i>pair.second:</i> deadend neighbours
	 */
	public <T extends Area> Pair<List<T>, List<T>> createLongRoadSegment(T current, StandardWorldModel world) {
		if(roadGroup.size() != 0) return null;
		addArea(current);
		roadPath.add(current.getID());
		reverseRoadPath.add(current.getID());
		List<T> neighbours = getRoadNeighbours(current, world);

		List<T> deadEnds = getAllDeadEnds(neighbours, world, current);

		neighbours.removeAll(deadEnds);

		return new Pair<List<T>,List<T>>(neighbours, deadEnds);
	}

	/**
	 * Fetches all deadends from a inputed list
	 * @param neighbours
	 * @param world
	 * @param current
	 * @return List containing all deadend roads
	 */
	private <T extends Area> List<T> getAllDeadEnds(List<T> neighbours,
			StandardWorldModel world, T current) {
		List<T> retVal = new ArrayList<T>();
		for(T road : neighbours){
			List<Area> roadNeighbours = getRoadNeighbours(road, world);
			if(roadNeighbours.size() == 1){
				retVal.add(road);
			}
			if(roadNeighbours.size() == 2){
				if(triangle(roadNeighbours.get(0), roadNeighbours.get(1)) || square(roadNeighbours.get(0), roadNeighbours.get(1), road, world)){
					retVal.add(road);
				}
			}
		}
		return retVal;
	}

	/**
	 * Returns true if road and road2 creates a square together with init
	 * @param road
	 * @param road2
	 * @param init
	 * @param world
	 * @return
	 */
	private boolean square(Area road, Area road2, Area init, StandardWorldModel world) {
		for(Area first : getRoadNeighbours(road2, world)){
			if(road2.getNeighbours().contains(first) && !first.equals(init.getID())) return true;
		}
		return false;
	}

	/**
	 * returns true if road2 is a neighbour of road
	 * @param road
	 * @param road2
	 * @return
	 */
	private boolean triangle(Area road, Area road2) {
		return road.getNeighbours().contains(road2.getID());
	}

	@SuppressWarnings("unchecked")
	private <T extends Area> List<T> getRoadNeighbours(Area road, StandardWorldModel world) {
		List<EntityID> list = road.getNeighbours();
		List<T> neighbours = new ArrayList<T>();
		for(EntityID eID : list){
			Entity e = world.getEntity(eID);
			if(e instanceof Area && !neighbours.contains((T)e)) neighbours.add((T)e);
		}
		return neighbours;
	}
	/**
	 * A slow action that combines two LongRoads with each others.
	 * All LongRoads connected to {@code inputLongRoad} will instead be connected to the LongRoad executing the command
	 * The {@link #roadPath} list will be extended and the length of the {@link #roadPath} will be increased
	 * {@code inputLongRoad} has to be a neighbouring LongRoad to the LongRoad triggering the action
	 * @param neighbourLongRoad
	 * @param world
	 */
	public void combine(LongRoad neighbourLongRoad, StandardWorldModel world) {
		if(neighbours.contains(neighbourLongRoad)){
			disconnect(neighbourLongRoad);
			connect(neighbourLongRoad.neighbours);
			while(!neighbourLongRoad.roadGroup.isEmpty()){
				addArea(neighbourLongRoad.roadGroup.get(0));
				neighbourLongRoad.roadGroup.remove(0);
			}
			while(!neighbourLongRoad.neighbours.isEmpty()){
				neighbourLongRoad.disconnect(neighbourLongRoad.neighbours.get(0));
			}
			if(neighbourLongRoad.roadPath.size() != 0){
				int addedDistance;
				if(((Area)world.getEntity(roadPath.getFirst())).getNeighbours().contains(neighbourLongRoad.roadPath.getFirst())){ // Head connect with Head
					addedDistance = world.getDistance(roadPath.getFirst(), neighbourLongRoad.roadPath.getFirst());
					while(!neighbourLongRoad.roadPath.isEmpty()){
						roadPath.addFirst(neighbourLongRoad.roadPath.getFirst());
						reverseRoadPath.addLast(neighbourLongRoad.roadPath.removeFirst());
					}
				}else if(((Area)world.getEntity(roadPath.getFirst())).getNeighbours().contains(neighbourLongRoad.reverseRoadPath.getFirst())){ // Head connect with tail
					addedDistance = world.getDistance(roadPath.getFirst(), neighbourLongRoad.reverseRoadPath.getFirst());
					while(!neighbourLongRoad.reverseRoadPath.isEmpty()){
						roadPath.addFirst(neighbourLongRoad.reverseRoadPath.getFirst());
						reverseRoadPath.addLast(neighbourLongRoad.reverseRoadPath.removeFirst());
					}
				}else if(((Area)world.getEntity(reverseRoadPath.getFirst())).getNeighbours().contains(neighbourLongRoad.roadPath.getFirst())){ // Tail connect with head
					addedDistance = world.getDistance(reverseRoadPath.getFirst(), neighbourLongRoad.roadPath.getFirst());
					while(!neighbourLongRoad.roadPath.isEmpty()){
						roadPath.addLast(neighbourLongRoad.roadPath.getFirst());
						reverseRoadPath.addFirst(neighbourLongRoad.roadPath.removeFirst());
					}
				}else{ // Tail connect with tail
					addedDistance = world.getDistance(reverseRoadPath.getFirst(), neighbourLongRoad.reverseRoadPath.getFirst());
					while(!neighbourLongRoad.reverseRoadPath.isEmpty()){
						roadPath.addLast(neighbourLongRoad.reverseRoadPath.getFirst());
						reverseRoadPath.addFirst(neighbourLongRoad.reverseRoadPath.removeFirst());
					}
				}
				length += addedDistance + neighbourLongRoad.length;
			}
		}
	}

	/**
	 * Function returning true if the LongRoad is an intersection.
	 * A LongRoad is an intersection if it has two or more neighbouring LongRoads.
	 * @return {@code true} if the LongRoad is an intersection.
	 */
	public boolean isIntersection(){
		return neighbours.size() > 2;
	}
	/**
	 * Gets a List containing all neighbouring LongRoads
	 * @return
	 */
	public List<LongRoad> getNeighbours() {
		return neighbours;
	}
	public LinkedList<EntityID> getPath(Area a){
		if(roadPath.getFirst().equals(a.getID())){
			return roadPath;
		}if(reverseRoadPath.getFirst().equals(a.getID())){
			return reverseRoadPath;
		}
		return null;
	}

	public Pair<List<EntityID>, Integer> getReversePath(Area a){
		if(roadPath.getFirst().equals(a.getID())){
			return new Pair<List<EntityID>, Integer>(reverseRoadPath, length);
		}if(reverseRoadPath.getFirst().equals(a.getID())){
			return new Pair<List<EntityID>, Integer>(roadPath, length);
		}
		return null;
	}
	public LinkedList<EntityID> getPath(){
		return roadPath;
	}


	/**
	 * returns true if area exists in the roadPath.
	 * @param myArea
	 * @return
	 */
	public boolean pathContains(Area myArea) {
		return roadPath.contains(myArea.getID());
	}

	/**
	 * Returns all neighbour-connections to the neighbouring roads
	 * it is stored in a map with key as edge of current longroad, and value as the other road
	 * @return
	 */
	public List<Pair<Area, Area>> getNeighbourConnections(){
		return neighbourRoadConnections;
	}
	
	/**
	 * Function to generate the neighbourConnections table.
	 * The table contains a list of which two areas that separates two LongRoads
	 * @param roadMap
	 */
	public void generateNeighbourConnections(Map<Area, LongRoad>roadMap){
		if(neighbourRoadConnections.size() == 0){
			List<Pair<Area, Area>> retVal = new ArrayList<Pair<Area, Area>>();
			Set<Area> searched = new HashSet<Area>();
			for(EntityID eID : ((Area)world.getEntity(roadPath.getFirst())).getNeighbours()){
				Entity e = world.getEntity(eID);
				if(e instanceof Area){
					Area a = (Area)e;

					LongRoad lr = roadMap.get(a);				
					if(lr != null && !lr.equals(this) && lr.pathContains(a) && !searched.contains(a)){
						searched.add(a);
						retVal.add(new Pair<Area, Area>((Area)world.getEntity(roadPath.getFirst()), a));
					}
				}
			}


			for(EntityID eID : ((Area)world.getEntity(roadPath.getLast())).getNeighbours()){
				Entity e = world.getEntity(eID);
				if(e instanceof Area){
					Area a = (Area)e;
					LongRoad lr = roadMap.get(a);
					if(lr != null && !lr.equals(this) && lr.pathContains(a) && !searched.contains(a)){
						searched.add(a);
						retVal.add(new Pair<Area, Area>((Area)world.getEntity(roadPath.getLast()), a));
					}
				}
				neighbourRoadConnections = retVal;
			}
		}
	}

	/**
	 * Returns all the LongRoads surrounding the area
	 * The provided roadMap should be a map mapping each area to a LongRoad
	 * @param area, roadMap
	 * @return List containing unique LongRoads around the area.
	 */
	@Deprecated
	public static List<LongRoad> getNearbyLongRoads(Area area, Map<Area, LongRoad> roadMap) {
		List<LongRoad> neighbours = new ArrayList<LongRoad>();
		for(EntityID eID : area.getNeighbours()){
			Entity e = world.getEntity(eID);
			if(e instanceof Area){
				LongRoad lr = roadMap.get((Area)e);
				if(lr != null && !neighbours.contains(lr)) neighbours.add(lr);
			}
		}
		return neighbours;
	}


	public void setRoadColor(Color roadColor) {
		this.roadColor = roadColor;
	}
	
	/**
	 * Returns the length of the roadPath
	 * @return
	 */
	public int getLength() {
		if(isPathBlocked)
		{
			for( Area bl : blockadesOnPath)
			{
				List<EntityID> neighbours = bl.getNeighbours();
				for(EntityID eid : neighbours)
				{
					StandardEntity se = world.getEntity(eid);
					if(se instanceof Area && pathContains((Area)se))
					{
						if(!obsDetector.isPassable(bl, (Area)se))
						{
							return -1;
						}
					}
				}
			}
		}
		isPathBlocked = false;
		return length;
	}
	

	/**
	 * Debug print of a mouse click event on an area
	 * TODO: Remove debug data when finished debugging
	 */
	public static void debugClickCallback(String substring) {
		Entity e = world.getEntity(new EntityID(Integer.parseInt(substring)));
		if(e instanceof Area){
			if(start == null || goal != null){
				start = (Area)e;
				goal = null;
			}else{
				goal = (Area)e;
			}
		}
	}
	
	/**
	 * TODO: Remove debug data when finished debugging
	 */
	public static List<Pair<Area, Integer>> debugInfo;
	/**
	 * TODO: Remove debug data when finished debugging
	 */
	public static Area start = null;
	/**
	 * TODO: Remove debug data when finished debugging
	 */
	public static Area goal = null;
	
	public Pair<List<EntityID>, Integer> getPath(Area start, Area end, Map<Area, LongRoad> roadMap ) {
		if(!roadMap.get(start).equals(this) || !roadMap.get(end).equals(this))
		{
			return null;
		}
		//TODO Anna gör så att den kollar om det går att gå igenom och sen ändrar isblocked
		else if(roadPath.getFirst().equals(start) && roadPath.getLast().equals(end) && !isPathBlocked)
		{
			return new Pair<List<EntityID>, Integer>(roadPath, length);
		}
		
		else if(reverseRoadPath.getFirst().equals(start) && reverseRoadPath.getLast().equals(end) && !isPathBlocked)
		{
			return new Pair<List<EntityID>, Integer>(reverseRoadPath, length);
		}
		else
		{
			Pair<Path, Set<EntityID>> pairPath = Path.getRawPath(start, end, world, this, obsDetector);
			if(pairPath.first == null) return new Pair<List<EntityID>, Integer>(null, -1);
			int cost = pairPath.first.gValue;
			List<Area> tempPath = pairPath.first.toList();
			List<EntityID> path = new ArrayList<EntityID>();
			for(Area a : tempPath)
			{
				path.add(a.getID());
			}
			return new Pair<List<EntityID>, Integer>(path, cost);
		}
		
	}
	public void addBlockade(Area ae) {
		if(pathContains(ae))
		{
			isPathBlocked = true;
			blockadesOnPath.add(ae);
		}
		else{
			blockadesNotOnPath.add(ae);
			
		}
		
	}
	public void removeBlockade(Area ae) {
		blockadesNotOnPath.remove(ae);
		blockadesOnPath.remove(ae);
		if(isPathBlocked) isPathBlocked = blockadesOnPath.size() > 0;//If the path was blocked, it will now be blocked if, there still are blockades left
		
	}
	public List<Area> getBlockadesOnPath() {
		return blockadesOnPath;
	}
	public List<Area> getBlockadesNotOnPath() {
		return blockadesNotOnPath;
	}
	
}

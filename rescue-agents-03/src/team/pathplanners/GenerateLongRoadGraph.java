package team.pathplanners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import team.AbstractObstacleDetector;
import team.obstacledetectors.DummyObstacleDetector;
import team.obstacledetectors.ObstacleDetector;

public class GenerateLongRoadGraph {
	private StandardWorldModel world;

	private List<LongRoad> roadSegments = new ArrayList<LongRoad>();
	private AbstractObstacleDetector obsDetector;
	private AStar astar;

	Map<Area, LongRoad> roadMap = new HashMap<Area, LongRoad>();
	public GenerateLongRoadGraph(StandardWorldModel world){
		this.world = world;
		astar = new AStar(world);
		obsDetector = new ObstacleDetector(world);
		Generate();
		

	}

	/**
	 * Generates the entire LongRoad-network
	 */
	void Generate(){

		
		world.addWorldModelListener(new PathWorldModelListener());
		LongRoad.setWorld(world);
		// Getting the first starting point
		Road startingPoint = (Road)world.getEntitiesOfType(StandardEntityURN.ROAD).iterator().next();
		
		
		// Step 1: Make a net with LongRoad-connections,
		// All deadends are connected to the first LongRoad finding it.
		List<Area> deadEnds = generateLongRoadConnections(startingPoint);

		
		
		System.out.println(deadEnds);
		// Step 2: Combine all LongRoads with 2 or less neighbours, this step also add intersections to the head and tails of that road.
		connectDisconnectedRoads(deadEnds);
		
		//combineContinuousRoads();
		
		// Step 3: Add all of the disconnected short roads to the longroad network
		connectDeadEnds(deadEnds);
		
		// Step 4: Map distance from one intersection to another
		//connectBuildings();
		
		combineContinuousRoads();
		
		concatDeadEndRoads();
	
		// Step 5: Generate all neighbourLongRoads
		for(LongRoad lr : roadSegments){
			lr.generateNeighbourConnections(roadMap);
		}
		for(LongRoad lr : roadSegments){
			System.out.println("longroad neighbours: " + lr.neighbours.size());
		}
		

	}

	private void concatDeadEndRoads() {
		for(int i = 0 ; i < roadSegments.size() ;++i){
			LongRoad lr = roadSegments.get(i);
			//if()
		}
		
		// TODO Auto-generated method stub
		
	}

	private void connectBuildings() {
		Iterator<StandardEntity> it = world.getEntitiesOfType(StandardEntityURN.BUILDING).iterator();
		while(it.hasNext()){
			Building r = (Building) it.next();
			if(!roadMap.containsKey(r)){
				Pair<List<List<Area>>, List<LongRoad>> connection = getConnectionToNearestLongRoad(r);
				assert(!connection.first.isEmpty());
				assert(!connection.second.isEmpty());
				assert(connection.first.size() == connection.second.size());
				System.out.println("connection.first.size() == " + connection.first.size());
				System.out.println("connection.second.size() == " + connection.second.size());
				if(connection.first.size() == 1){
					//connection.second.get(0).addAreas(connection.first);
					List<Area> la = connection.first.get(0);
					for(Area a : la){
						connection.second.get(0).addArea(a);
						roadMap.put(a,  connection.second.get(0));
					}
				}else{
					for(List<Area> la : connection.first){
						boolean root = true;
						for(Area a : la){
							if(!roadMap.containsKey(a)){
								LongRoad lr = new LongRoad(getLongRoadsAroundArea(a), obsDetector);
								//lr.connect(connection.second.get(connection.first.indexOf(al)));
								lr.createLongRoadSegment(a, world);
								roadSegments.add(lr);
								roadMap.put(a, lr);
							}
						}
					}
				}
			}
		}
	}

	private void connectDeadEnds(List<Area> deadEnds) {
		for(Area a : deadEnds){
			if(!roadMap.containsKey(a)){
				List<LongRoad> longRoads = getNearbyLongRoads(a);
				longRoads.get(0).addArea(a);
				roadMap.put(a, longRoads.get(0));
			}
		}
		// TODO Auto-generated method stub
		
	}
	
	public LongRoad getLongRoadFromArea(Area a){
		return roadMap.get(a);
	}

//	private void createDistanceTables() {
//		for(LongRoad lr : roadSegments){
//			if(lr.isIntersection()){
//				lr.createDistanceTable(world);
//			}
//		}
//	}

	private void combineContinuousRoads() {
		for(int i = 0 ; i < roadSegments.size();){
			boolean combined = false;
			if(roadSegments.get(i).neighbours.size() <= 2){
				LongRoad currLR = roadSegments.get(i);
				
				// Combine all continuous roads
				for(int j = 0; j < currLR.neighbours.size();){
					LongRoad currNB = currLR.neighbours.get(j);
					if(currNB.neighbours.size() <= 2){
						List<Area> areas = currNB.getAreas();
						for(Area a : areas){
							roadMap.remove(a);
							roadMap.put(a, currLR);
						}
						currLR.combine(currNB, world);
						roadSegments.remove(currNB);
						//System.out.println("Combining");
						combined = true;
					}else ++j;
				}
				
				// The left neighbouring LongRoads should be intersections only
				//currLR.addIntersectionPoints(world);
			}
			if(!combined) ++i;
		}
	}
	/**
	 * Loops through all of the roads and connects all disconnected roads in the Road network.
	 * It tries to find a path to all nearby LongRoads and creates a road going through them.
	 * TODO: fix so that it connects correctly to dead-end roads
	 * Until it is fixed, this step should not be used
	 */
	private void connectDisconnectedRoads(List<Area> deadEnds) {
		Iterator<StandardEntity> it = world.getEntitiesOfType(StandardEntityURN.ROAD).iterator();
		while(it.hasNext()){
			Road r = (Road) it.next();
			if(!roadMap.containsKey(r) && !deadEnds.contains(r)){
				Pair<List<List<Area>>, List<LongRoad>> connection = getConnectionToNearestLongRoad(r);
				assert(!connection.first.isEmpty());
				assert(!connection.second.isEmpty());
				assert(connection.first.size() == connection.second.size());
				for(List<Area> la : connection.first){
					for(Area a : la){
						if(!roadMap.containsKey(a)){
							LongRoad lr = new LongRoad(getLongRoadsAroundArea(a), obsDetector);
							lr.createLongRoadSegment(a, world);
							roadSegments.add(lr);
							roadMap.put(a, lr);
						}
					}
				}
				//LongRoad lr = new LongRoad(connection.second);
				//lr.addAreas(connection.first);
				//roadSegments.add(lr);
			}
		}
	}
	/**
	 * Generates a network of all LongRoads, stores it in {@link #roadSegments}
	 * @param startRoad
	 * @return a List containing all the dead-end Roads.
	 */
	@SuppressWarnings("unchecked")
	private <T extends Area> List<T> generateLongRoadConnections(Road startRoad) {
		List<Area> deadEnds = new ArrayList<Area>();
		//Create a stack for all roads to expand
		Stack<Pair<Area,LongRoad>> stack = new Stack<Pair<Area,LongRoad>>();
		Pair<Area,LongRoad> rootGroup = new Pair<Area,LongRoad>(startRoad, null);
		stack.add(rootGroup);
		while(!stack.isEmpty())
		{
			Pair<Area,LongRoad> current = stack.pop();
			if(!roadMap.containsKey(current.first)){
				LongRoad lr = new LongRoad(current.second, obsDetector);
				roadMap.put(current.first, lr);
				Pair<List<Area>, List<Area>> neighbours = lr.createLongRoadSegment(current.first, world);
				//Adds all deadends to the longroad segment
				//lr.addAreas(neighbours.second);
				deadEnds.addAll(neighbours.second);
				for(Area r: neighbours.first){
					LongRoad olr = roadMap.get(r);
					if(olr == null){
						stack.add(new Pair<Area,LongRoad>(r, lr));
					}
					else{
						lr.connect(olr);
					}
				}
				roadSegments.add(lr);
			}
		}
		return (List<T>)deadEnds;
	}

	private Pair<List<List<Area>>, List<LongRoad>> getConnectionToNearestLongRoad(Area r) {
		PriorityQueue<Path> queue = new PriorityQueue<Path>();
		Set<EntityID> explored = new HashSet<EntityID>();
		Path root = new Path(r, null, world);
		queue.add(root);
		explored.add(r.getID());
		Pair<List<List<Area>>, List<LongRoad>> retVal = new Pair<List<List<Area>>, List<LongRoad>>(new ArrayList<List<Area>>(), new ArrayList<LongRoad>());
		while(!queue.isEmpty()){
			Path current = queue.poll();
			List<EntityID> neighbours = current.getArea().getNeighbours();
			for(EntityID eID : neighbours){
				Entity e = world.getEntity(eID);
				if(e instanceof Area && !explored.contains(eID)){
					explored.add(eID);
					Area area = (Area)e;
					LongRoad lr = roadMap.get(area);
					if(lr != null){
						// Add the connection
						retVal.first.add(current.toList());
						retVal.second.add(lr);
					}else{
						// Keep expanding search on this path
						Path path = new Path(area, current, world);
						queue.add(path);
					}
				}
			}
		}
		return retVal;
	}

	private List<LongRoad> getLongRoadsAroundArea(Area area){
		List<LongRoad> retVal = new ArrayList<LongRoad>();
		for(EntityID eID : area.getNeighbours()){
			Entity e = world.getEntity(eID);
			if(e instanceof Area){
				LongRoad lr = roadMap.get((Area)e);
				if(lr != null && !retVal.contains(lr)) retVal.add(lr);
			}
		}
		return retVal;
	}
	public List<LongRoad> getRoadSegments() {
		return roadSegments;
	}


	/**
	 * Returns all the LongRoads surrounding the area
	 * @param a
	 * @return List containing unique LongRoads around the area.
	 */
	private List<LongRoad> getNearbyLongRoads(Area a) {
		List<LongRoad> neighbours = new ArrayList<LongRoad>();
		for(EntityID eID : a.getNeighbours()){
			Entity e = world.getEntity(eID);
			if(e instanceof Area){
				LongRoad lr = roadMap.get((Area)e);
				if(lr != null && !neighbours.contains(lr)) neighbours.add(lr);
			}
		}
		return neighbours;
	}
	
	
	private class PathWorldModelListener implements WorldModelListener<StandardEntity>{
	
		@Override
		public void entityAdded(WorldModel<? extends StandardEntity> model, StandardEntity e) {
			if(e instanceof Blockade)
			{
				Blockade b = (Blockade)e;
				EntityID ei = b.getPosition();
				StandardEntity ent = world.getEntity(ei);
				if(ent instanceof Area )
				{
					Area ae = (Area)ent;
				LongRoad lr = getLongRoadFromArea(ae);
				
				lr.addBlockade(ae);
				}
				
				
			}
			
			
		}

		@Override
		public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
			if(e instanceof Blockade)
			{
				System.out.println("weop "+ e);
				Blockade b = (Blockade)e;
				EntityID ei = b.getPosition();
				StandardEntity ent = world.getEntity(ei);
				if(ent instanceof Area )
				{
					Area ae = (Area)ent;
				LongRoad lr = getLongRoadFromArea(ae);
				
				lr.removeBlockade(ae);
				}
				
				
			}
		}

	}

	public List<EntityID> getPath(Area start, Area goal)
	{
		return astar.FindPath(start, goal, this).first;
	}
	/**
	 * 
	 * @return the cost of the latest searched path
	 */
	public int getPathCost()
	{
		return astar.getTotalCost();
	}
	
	public int getPathCost(Area start, Area goal)
	{
		astar.FindPath(start, goal, this);
		return astar.getTotalCost();
	}
	/**
	 * 
	 * @return the latest searched path
	 */
	public List<EntityID> getPath()
	{
		return astar.getPath();
	}
	public AbstractObstacleDetector getObsDetector() {
		return obsDetector;
	}

}

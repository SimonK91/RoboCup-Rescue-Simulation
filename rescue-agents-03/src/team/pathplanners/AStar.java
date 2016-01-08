package team.pathplanners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

//TODO anna refactora all kod


public class AStar {
	private StandardWorldModel world;

	public AStar(StandardWorldModel world) {
		super();
		this.world = world;
	}
	private Map<LongRoad, Integer> fScore = new HashMap<LongRoad, Integer>();
	private Map<LongRoad, LongRoad> cameFrom = new HashMap<LongRoad, LongRoad>();
	private LongRoad LongRoadStart = null;
	private LongRoad LongRoadGoal = null;
	private Area goal;
	private Area start;
	private Set<LongRoad> closed = new HashSet<LongRoad>();
	private PriorityQueue<LongRoad> open = null;
	private int totalCost = 0;
	private List<EntityID> path = new ArrayList<EntityID>();

	public Pair<List<EntityID>, Set<LongRoad>> FindPath(Area start, Area goal, GenerateLongRoadGraph graph){
		totalCost = 0;
		LongRoadStart = graph.getLongRoadFromArea(start);
		LongRoadGoal = graph.getLongRoadFromArea(goal);
		this.goal = goal;
		this.start = start;

		open = new PriorityQueue<LongRoad>(30, longRoadComp);

		Map<LongRoad, Integer> gScore = new HashMap<LongRoad, Integer>();

		gScore.put(LongRoadStart, 0);
		int hCost = getDistance(LongRoadStart, goal);
		fScore.put(LongRoadStart, hCost);
		open.add(LongRoadStart);
		while(!open.isEmpty())
		{
			LongRoad current = open.poll();
			while(closed.contains(current))
			{
				current = open.poll();
			}
			if(current.equals(LongRoadGoal))
			{
				Pair<List<EntityID>, Set<LongRoad>> preliminaryPath = getPath(current, graph);
				if(preliminaryPath.first != null){
					path = preliminaryPath.first;
					return preliminaryPath;
				}
				else{
					continue;
				}
			}


			closed.add(current);
			for(Pair<Area, Area> pair :  current.getNeighbourConnections())
			{
				LongRoad neighbour = graph.getLongRoadFromArea(pair.second);
				if(!closed.contains(neighbour))
				{
					int neighbourLength = neighbour.getLength();
					//if it is possible to go through this longRoad
					if(neighbourLength > -1 && graph.getObsDetector().isPassable(pair.first, pair.second))
					{

						int newGScore = gScore.get(current) + neighbour.getLength() + world.getDistance(pair.first, pair.second);
						if(!open.contains(neighbour))
						{
							cameFrom.put(neighbour, current);
							gScore.put(neighbour, newGScore);
							int hScore = getDistance(neighbour, goal);
							fScore.put(neighbour, newGScore+hScore);
							open.add(neighbour);

						}
						else if(newGScore < gScore.get(neighbour))
						{
							cameFrom.put(neighbour, current);
							gScore.put(neighbour, newGScore);
							int hScore = getDistance(neighbour, goal);
							
							fScore.put(neighbour, newGScore+hScore);
							open.add(neighbour);
						}
					}
				}
			}

		}

		return new  Pair<List<EntityID>, Set<LongRoad>>(null, closed);
	}


	private Pair<List<EntityID>, Set<LongRoad>> getPath(LongRoad current, GenerateLongRoadGraph graph) {
		List<EntityID> path = new ArrayList<EntityID>();
		Area last = goal;
		while(cameFrom.get(current) != null )
		{
			for(Pair<Area, Area> pair: current.getNeighbourConnections())
			{
				//if this pair is the connection between current longroad and its parent
				if(graph.getLongRoadFromArea(pair.second).equals(cameFrom.get(current)))
				{
					last = pair.second;
					totalCost += world.getDistance(pair.first, pair.second);
					if(current.equals(LongRoadGoal))
					{
						Pair<List<EntityID>, Integer> result = current.getPath(goal,pair.first, graph.roadMap);
						totalCost+=result.second;
						path.addAll(result.first);
					}
					else
					{
						Pair<List<EntityID>, Integer> result = current.getReversePath(pair.first);
						totalCost+=result.second;
						path.addAll(result.first);
					}
					break;
				}

			}
			current = cameFrom.get(current);
		}
		//current is the first longRoad
		Pair<List<EntityID>, Integer> result = current.getPath(last, start, graph.roadMap);
		if(result.first == null)
		{
			return new Pair<List<EntityID>, Set<LongRoad>>(null, closed);
		}
		totalCost+=result.second;
		path.addAll(result.first);
		return new Pair<List<EntityID>, Set<LongRoad>>(path, closed);
	}


	private int getDistance(LongRoad neighbour, Area goal) {
		int distance = Integer.MAX_VALUE;
		for(Pair<Area, Area> pair: neighbour.getNeighbourConnections())
		{
			Area exit = pair.first;
			int temp = world.getDistance(exit.getID(), goal.getID());
			if(temp < distance)
			{
				distance = temp;
			}
		}
		return distance;
	}

	private Comparator<LongRoad> longRoadComp = new Comparator<LongRoad>() {

		@Override
		public int compare(LongRoad l1, LongRoad l2) {

			return fScore.get(l1) - fScore.get(l2);
		}
	};

	public int getTotalCost() {
		return totalCost;
	}


	public List<EntityID> getPath() {
		return path;
	}


}

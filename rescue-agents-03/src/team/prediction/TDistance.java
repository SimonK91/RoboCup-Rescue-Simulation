package team.prediction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class TDistance {
	private static Map<EntityID,Map<EntityID,Integer>> distanceMap = null;
	public TDistance(StandardWorldModel model){
		if(distanceMap != null) return;
		distanceMap = new HashMap<EntityID, Map<EntityID,Integer>>();
		Collection<StandardEntity> allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		int total = allBuildings.size();
		int current = 0;
		int nextPercentage = 0;
		int percentageStep = 10;
		
		// Generate the entire distance map
		for(StandardEntity first: allBuildings){
			// Print how much that have finished so far of the generation
			current++;
			int currPercentage = (current*100)/total;
			if(currPercentage >= nextPercentage){
				nextPercentage += percentageStep;
				System.out.println("Generating new distance map: " + currPercentage + "%");
			}
			
			
			for(StandardEntity second : allBuildings){
				// Add a new hash map with first or second as keys if they don't already exist
				if(!distanceMap.containsKey(first.getID())){
					distanceMap.put(first.getID(), new HashMap<EntityID, Integer>());
					
					
				}if(!distanceMap.containsKey(second.getID())){
					distanceMap.put(second.getID(), new HashMap<EntityID, Integer>());
				}
				// if it is the same building: distance is 0
				if(second.equals(first)){
					distanceMap.get(first.getID()).put(second.getID(), 0);
				}else{
					int distance = getClosestDistance((Building)first, (Building)second);
					distanceMap.get(first.getID()).put(second.getID(), distance);
					distanceMap.get(second.getID()).put(first.getID(), distance);
				}
			}
			// Sort the completed map
			distanceMap.put(first.getID(),MapUtil.sortByValue(distanceMap.get(first.getID())));
		}
	}
	public int distance(EntityID lhs, EntityID rhs){
		return distanceMap.get(lhs).get(rhs);
	}

	public Map<EntityID,Integer> getDistances(EntityID lhs){
		return distanceMap.get(lhs);
	}
	public Map<EntityID,Integer> getDistances(Area lhs){
		return distanceMap.get(lhs.getID());
	}
	
	/**
	 * Calculates the distance between two buildings
	 * returns the closest distance between the buildings instead of the center of both buildings
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private int getClosestDistance(Building lhs, Building rhs){
		List<Edge> A = lhs.getEdges();
		List<Edge> B = rhs.getEdges();
		
		double closest = -1;
		for(Edge a : A){
			for(Edge b : B){
				double candidate = getClosestTwoLines(a, b);
				if(closest == -1 || closest > candidate){
					closest = candidate;
				}
			}
		}
		return (int)closest;
		
	}
	/**
	 * Calculates the distance between two edges (two lines)
	 * returns the closest distance between the lines
	 * @param first
	 * @param second
	 * @return
	 */
	private double getClosestTwoLines(Edge first, Edge second){
		
		double closest = getClosestLinePoint(first.getStartX(), first.getStartY(), first.getEndX(), first.getEndY(), second.getStartX(), second.getStartY());
		double candidate = getClosestLinePoint(first.getStartX(), first.getStartY(), first.getEndX(), first.getEndY(), second.getEndX(), second.getEndY());
		if(candidate < closest) closest = candidate;
		candidate = getClosestLinePoint(second.getStartX(), second.getStartY(), second.getEndX(), second.getEndY(), first.getStartX(), first.getStartY());
		if(candidate < closest) closest = candidate;
		candidate = getClosestLinePoint(second.getStartX(), second.getStartY(), second.getEndX(), second.getEndY(), first.getEndX(), first.getEndY());
		if(candidate < closest) return candidate;
		return closest;	
	}
	
	
	/**
	 * Calculates the closest distance between the line between point (aX, aY) and (bX, bY) and the point (cX, cY)
	 * @param aX
	 * @param aY
	 * @param bX
	 * @param bY
	 * @param cX
	 * @param cY
	 * @return double of the closest distance
	 */
	private double getClosestLinePoint(double aX, double aY, double bX,
			double bY, double cX, double cY) {

		
		double[] AB = new double[2];
		AB[0] = bX-aX;
		AB[1] = bY-aY;

		double[] ABT = new double[2];
		ABT[0] = AB[1];
		ABT[1] = -AB[0];
		
		double[] AC = new double[2];
		AC[0] = cX-aX;
		AC[1] = cY-aY;
		
		double[] BC = new double[2];
		BC[0] = cX-bX;
		BC[1] = cY-bY;

		double dirAC =Math.atan((cY-aY)/(cX-aX));
		double dirCB =Math.atan((bY-cY)/(bX-cX));
		double dirBA =Math.atan((aY-bY)/(aX-bX));
		
		double aACB = dirCB-dirAC;
		if(aACB <-Math.PI/2) aACB += Math.PI;
		if(aACB > Math.PI/2) aACB -= Math.PI;
		aACB = Math.abs(aACB);
		
		double aCBA = dirBA-dirCB;
		if(aCBA <-Math.PI/2) aCBA += Math.PI;
		if(aCBA > Math.PI/2) aCBA -= Math.PI;
		aCBA = Math.abs(aCBA);
		
		double aBAC = dirAC-dirBA;
		if(aBAC <-Math.PI/2) aBAC += Math.PI;
		if(aBAC > Math.PI/2) aBAC -= Math.PI;
		aBAC = Math.abs(aBAC);
		
		
		double angleBAC = Math.atan2(BC[1], BC[0]);
		double angleABC = Math.atan2(-AC[1], -AC[0]);
		
		double ACdist = Math.sqrt(AC[0]*AC[0] + AC[1]*AC[1]);
		if(angleBAC > Math.PI/2 || angleBAC < -Math.PI/2){
				return ACdist;
		}
		if(angleABC > Math.PI/2 || angleABC < -Math.PI/2){
			return Math.sqrt(BC[0]*BC[0] + BC[1]*BC[1]);
		}
		
		return Math.abs(Math.sin(angleBAC)*ACdist);
	}

	public int distance(EntityID lhs, Area rhs){
		return distance(lhs, rhs.getID());
	}
	public int distance(Area lhs, Area rhs){
		return distance(lhs.getID(), rhs.getID());
	}
	public int distance(Area lhs, EntityID rhs){
		return distance(lhs.getID(), rhs);
	}
}

package team.pathplanners;

import java.util.Collection;
import java.util.List;

import rescuecore2.worldmodel.EntityID;
import team.AbstractPathPlanner;

public class AstarPathPlanner extends AbstractPathPlanner{

	@Override
	public List<EntityID> plan(EntityID start, Collection<EntityID> goals) {
		if(goals.size() != 1) return null;//TODO implementera stöd för flera goals
		
		
		
		return null;
	}

}

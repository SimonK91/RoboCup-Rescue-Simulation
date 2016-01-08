package team.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import team.SampleLayer;
import team.pathplanners.AStar;
import team.pathplanners.GenerateLongRoadGraph;
import team.pathplanners.LongRoad;
import team.pathplanners.Pair;
import team.pathplanners.Path;

public class RoadNetworkLayer extends SampleLayer {

	GenerateLongRoadGraph graph = null;
	public RoadNetworkLayer(){
		super();

	}

	@Override
	public Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform transform, int width, int height) {
		synchronized (entities) {
			Collection<RenderedObject> result = new ArrayList<RenderedObject>();


			for (Area next : entities) {
				if(next instanceof Area){
					for(EntityID neighbour_id : next.getNeighbours())
					{
						if(neighbour_id.getValue() < next.getID().getValue())
						{
							Entity e = world.getEntity(neighbour_id);
							if(e instanceof Area)
							{
								Area neighbour = (Area)e;
								Line2D shape = new Line2D.Double(
										transform.xToScreen(next.getX()), transform.yToScreen(next.getY()),
										transform.xToScreen(neighbour.getX()), transform.yToScreen(neighbour.getY()));
								g.setColor(Color.orange);
								g.draw(shape);
								result.add(new RenderedObject(next, shape));
							}
						}
					}
				}
			}
			if(graph == null && world != null){
				graph = new GenerateLongRoadGraph(world);
			}

			drawDebug(g, transform, result);
			drawAllLongRoadSegments(g, transform, result);
			drawPath(g, transform, result, false);
			drawAStarpath(g, transform, result, true);
			//drawAllLongRoadPaths(g, transform,result);
			//drawAllIntersectionConnections(g, transform, result);


			//drawDebug(g, transform, result);


			return result;
		}

	}

	private void drawDebug(Graphics2D g, ScreenTransform transform,
			Collection<RenderedObject> result) {
		if(LongRoad.debugInfo != null){
			for (Pair<Area, Integer> pair : LongRoad.debugInfo){
				int radius = 5;
				Ellipse2D shape = new Ellipse2D.Double(transform.xToScreen(pair.first.getX())-radius,
						transform.yToScreen(pair.first.getY())-radius, 2*radius-1, 2*radius-1);
				g.setColor(Color.RED);
				g.fill(shape);
				result.add(new RenderedObject(pair.first, shape));
			}
		}
	}

	private void drawAllLongRoadPaths(Graphics2D g,
			ScreenTransform transform, Collection<RenderedObject> result) {
		for (LongRoad lr : graph.getRoadSegments()){
			if(lr.getPath().size() > 1){
				for(int i = 1 ; i < lr.getPath().size(); ++i){
					Area first = (Area)world.getEntity(lr.getPath().get(i-1));
					Area second = (Area)world.getEntity(lr.getPath().get(i));
					Line2D shape = new Line2D.Double(
							transform.xToScreen(first.getX()), transform.yToScreen(first.getY()),
							transform.xToScreen(second.getX()), transform.yToScreen(second.getY()));

					Stroke oldSTroke = g.getStroke();
					g.setStroke(new BasicStroke(10));
					g.setColor(lr.getRoadColor());
					g.draw(shape);
					g.setStroke(oldSTroke);
					result.add(new RenderedObject(first, shape));

				}
			}
		}
	}

	private void drawAllLongRoadSegments(Graphics2D g, ScreenTransform transform, Collection<RenderedObject> result) {
		for (LongRoad lr : graph.getRoadSegments()){
			for(Area a : lr.getAreas())
			{
				int radius = 4;
				if(lr.isIntersection() && lr.pathContains(a)){
					radius = 10;
				}
				Ellipse2D shape = new Ellipse2D.Double(transform.xToScreen(a.getX())-radius,
						transform.yToScreen(a.getY())-radius, 2*radius-1, 2*radius-1);
				g.setColor(lr.getRoadColor());
				if(lr.pathContains(a)){
					g.fill(shape);
				}else{
					g.draw(shape);
				}
				result.add(new RenderedObject(a, shape));
			}	            	
		}



	}


	private void drawPath(Graphics2D g, ScreenTransform transform,
			Collection<RenderedObject> result, boolean includeSearched) {
		Area start = LongRoad.start; 
		Area goal = LongRoad.goal;
		if(start == null || goal == null) return;

		long startTime = System.currentTimeMillis();
		Pair<Path,Set<EntityID>> res = Path.getRawPath(start,  goal, world);
		long endTime = System.currentTimeMillis();
		System.out.println("drawpath time "+ (endTime - startTime));
		System.out.println("drawpath cost "+ res.first.getGValue());
		g.setColor(Color.BLUE);
		if(includeSearched){
			for(EntityID segmentID : res.second)
			{

				Area segment = (Area)world.getEntity(segmentID);
				Rectangle2D shape = new Rectangle2D.Double(transform.xToScreen(segment.getX()), 
						transform.yToScreen(segment.getY()), 5, 5);
				g.fill(shape);
				result.add(new RenderedObject(segment, shape));
			}
		}
		if(res.first == null) return;
		g.setColor(Color.GREEN);
		List<Area> foundPath = res.first.toList();
		for(int i = 1; i < foundPath.size(); ++i){
			Area first = foundPath.get(i-1);
			Area second = foundPath.get(i);
			Line2D shape = new Line2D.Double(
					transform.xToScreen(first.getX()), transform.yToScreen(first.getY()),
					transform.xToScreen(second.getX()), transform.yToScreen(second.getY()));

			Stroke oldSTroke = g.getStroke();
			g.setStroke(new BasicStroke(10));
			g.draw(shape);
			g.setStroke(oldSTroke);
			result.add(new RenderedObject(first, shape));

		}
	}
	private void drawAStarpath(Graphics2D g, ScreenTransform transform,
			Collection<RenderedObject> result, boolean includeSearched) {
		AStar astar = new AStar(world);
		List<LongRoad> lr = graph.getRoadSegments();

		Area start = LongRoad.start; 
		Area goal = LongRoad.goal;
		if(start == null || goal == null) return;
		System.out.println("start "+ start);
		System.out.println("goal "+goal);

		long startTime = System.currentTimeMillis();
		Pair<List<EntityID>, Set<LongRoad>> path = astar.FindPath(start, goal, graph);
		long endTime = System.currentTimeMillis();
		System.out.println("drawAstarpath time "+ (endTime - startTime));

		g.setColor(Color.RED);
		if(includeSearched){
			for(LongRoad searchedLR : path.second)
			{
				for(Area segment : searchedLR.getAreas()){
					Rectangle2D shape = new Rectangle2D.Double(transform.xToScreen(segment.getX()), 
							transform.yToScreen(segment.getY()), 10, 10);
					g.fill(shape);
					result.add(new RenderedObject(segment, shape));
				}
			}
		}
		g.setColor(Color.BLUE);
		if(path.first != null){ 

			for(int i = 1; i < path.first.size(); ++i){
				Area first = (Area)world.getEntity(path.first.get(i-1));
				Area second = (Area)world.getEntity(path.first.get(i));
				Line2D shape = new Line2D.Double(
						transform.xToScreen(first.getX()), transform.yToScreen(first.getY()),
						transform.xToScreen(second.getX()), transform.yToScreen(second.getY()));

				Stroke oldSTroke = g.getStroke();
				g.setStroke(new BasicStroke(10));
				g.draw(shape);
				g.setStroke(oldSTroke);
				result.add(new RenderedObject(first, shape));
			}
		}

		Rectangle2D shape = new Rectangle2D.Double(transform.xToScreen(start.getX()), 
				transform.yToScreen(start.getY()), 5, 5);
		g.setColor(Color.GREEN);
		g.fill(shape);
		result.add(new RenderedObject(start, shape));

		Rectangle2D shape2 = new Rectangle2D.Double(transform.xToScreen(goal.getX()), 
				transform.yToScreen(goal.getY()), 5, 5);
		g.setColor(Color.RED);
		g.fill(shape2);
		result.add(new RenderedObject(goal, shape2));           

		System.out.println("Astar cost = "+ astar.getTotalCost());

	}

}

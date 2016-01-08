package team.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.score.DiscoveryScoreFunction;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import team.AbstractCentre;
import team.AbstractObstacleDetector;
import team.FireStationCentre;
import team.obstacledetectors.ObstacleDetector;
import team.prediction.FireBuilding;
import team.prediction.FirePrediction;
import team.prediction.learning.Stringify;

public class FirePredictLayer extends StandardViewLayer {
	private static String REGEX = "Building \\(([0-9]*)\\).*";
	private static final Color OUTLINE_COLOUR = Color.gray;
	private static final Color KNOWN_COLOUR = new Color(0,0,255,40);
	private static final Stroke WALL_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	private static final Stroke ENTRANCE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	protected List<Area> entities;
	protected AbstractObstacleDetector detector;
	private FirePrediction prediction;

	private FirePredictLayer() {
		entities = new ArrayList<Area>();
	}

	public <T extends AbstractCentre> FirePredictLayer(T centre) {
		entities = new ArrayList<Area>();
		if(centre instanceof FireStationCentre){
			prediction = ((FireStationCentre) centre).getPrediction();
		}
	}

	@Override
	public Rectangle2D view(Object... objects) {
		synchronized (entities) {
			entities.clear();
			detector = null;
			Rectangle2D result = super.view(objects);
			return result;
		}
	}

	@Override
	protected void viewObject(Object o) {
		super.viewObject(o);
		if(o instanceof Area) {
			entities.add((Area)o);
		}
		if (o instanceof WorldModel) {
			WorldModel<? extends Entity> wm = (WorldModel<? extends Entity>)o;
			detector = new ObstacleDetector((StandardWorldModel)o);
			for (Entity next : wm) {
				viewObject(next);
			}
		}
	}

	@Override
	public Collection<RenderedObject> render(Graphics2D g,
			ScreenTransform transform, int width, int height) {
		synchronized (entities) {
			Collection<RenderedObject> result = new ArrayList<RenderedObject>();

			if(prediction == null)
				return result;
			for(FireBuilding se : prediction.getDiscoveredFires()){
				Shape areaShape = render(se.getBuilding(), g, transform);
				g.setColor(new Color(Math.min(255, se.getTemperature()), 
									 Math.max(0, Math.min(180, 435-se.getTemperature())), 
									 Math.max(0, 255-se.getTemperature())));
				g.fill(areaShape);
				result.add(new RenderedObject(se.getBuilding(),areaShape));	
			}
			Map<EntityID, Integer> debug = new HashMap<EntityID, Integer>();
			for(FireBuilding se : prediction.getPredictedFires(0)){
				Shape areaShape = render(se.getBuilding(), g, transform);
				if(debug.containsKey(se.getID())){
					debug.put(se.getID(), 1+debug.get(se.getID()));
				}else{
					debug.put(se.getID(), 1);
				}
				g.setColor(new Color(Math.min(255, se.getTemperature()), 
									 Math.max(0, Math.min(180, 435-se.getTemperature())), 
									 Math.max(0, 255-se.getTemperature())));
				g.fill(areaShape);
				result.add(new RenderedObject(se.getBuilding(),areaShape));	
			}
			for(Entry<EntityID, Integer> e : debug.entrySet()){
				System.out.println(e);
			}
			

			return result;
		}
	}


	@Override
	public String getName() {
		return "FirePredict";
	}

	public Shape render(Area area, Graphics2D g, ScreenTransform t) {
		List<Edge> edges = area.getEdges();
		if (edges.isEmpty()) {
			return null;
		}
		int count = edges.size();
		int[] xs = new int[count];
		int[] ys = new int[count];
		int i = 0;
		for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
			Edge e = it.next();
			xs[i] = t.xToScreen(e.getStartX());
			ys[i] = t.yToScreen(e.getStartY());
			++i;
		}
		Polygon shape = new Polygon(xs, ys, count);
		for (Edge edge : edges) {
			paintEdge(edge, g, t);
		}
		return shape;
	}
	protected void paintEdge(Edge e, Graphics2D g, ScreenTransform t) {
		g.setColor(OUTLINE_COLOUR);
		g.setStroke(e.isPassable() ? ENTRANCE_STROKE : WALL_STROKE);
		g.drawLine(t.xToScreen(e.getStartX()),
				t.yToScreen(e.getStartY()),
				t.xToScreen(e.getEndX()),
				t.yToScreen(e.getEndY()));
	}
	
	public void callback(String data){
		System.out.println("Data: " + data);
		Pattern p = Pattern.compile(REGEX);
		Matcher match = p.matcher(data);
		if(match.find() ){
			String s = match.group(1);
			if(s.length() > 0){
				EntityID eID = new EntityID(Integer.parseInt(s));
				if(!prediction.protectBuilding(eID)){
					if(!prediction.getDiscoveredFires().contains(new FireBuilding(eID)))
						prediction.FireDiscovered(eID);
				}
			}
		}else{
			System.out.println("Progressing simulation");
			prediction.simulateStep();
		}
	}
}

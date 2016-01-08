package team.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.nio.channels.GatheringByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.quantity.Temperature;

import rescuecore2.messages.control.KVTimestep;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
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
//import team.prediction.learning.BuildingData;
import team.prediction.learning.Stringify;

public class FirePredictPracticeLayer extends StandardViewLayer {
	private static String REGEX = "Building \\(([0-9]*)\\).*";
	private int cnt = 0;
	private static final Color OUTLINE_COLOUR = Color.gray;
	private static final Color KNOWN_COLOUR = new Color(0,0,255,40);
	private static final Stroke WALL_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	private static final Stroke ENTRANCE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	protected List<Area> entities;
	protected AbstractObstacleDetector detector;
	private FirePrediction prediction;
	private Map<Integer, List<FireBuilding>> practiceData = new HashMap<Integer, List<FireBuilding>>();
	private boolean createFP = true;

	public FirePredictPracticeLayer() {
		entities = new ArrayList<Area>();
	}

	public <T extends AbstractCentre> FirePredictPracticeLayer(T centre) {
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

			if(createFP)
			{
				createFP = false;
				prediction = new FirePrediction(world);
			}
			if(prediction.PRACTICE){
				if(prediction.practiceData != null){
					System.out.println("prediction.practiceData.size(): " + prediction.practiceData.size());
					List<FireBuilding> data = prediction.practiceData.get(++cnt);
					if(data != null){
						for(FireBuilding se : data){
							Building b = se.getBuilding();
							Shape areaShape = render(b, g, transform);
							g.setColor(new Color(Math.min(255, se.getTemperature()), 
									Math.max(0, Math.min(180, 435-se.getTemperature())), 
									Math.max(0, 255-se.getTemperature())));
							g.fill(areaShape);
							result.add(new RenderedObject(b,areaShape));	
						}
					}else{
						cnt = 0;
					}
				}
			}else{
				for(FireBuilding se : prediction.getDiscoveredFires()){
					Shape areaShape = render(se.getBuilding(), g, transform);
					g.setColor(new Color(Math.min(255, se.getTemperature()), 
							Math.max(0, Math.min(180, 435-se.getTemperature())), 
							Math.max(0, 255-se.getTemperature())));
					g.fill(areaShape);
					result.add(new RenderedObject(se.getBuilding(),areaShape));	
				}
				for(FireBuilding se : prediction.getPredictedFires(0)){
					Shape areaShape = render(se.getBuilding(), g, transform);
					g.setColor(new Color(Math.min(255, se.getTemperature()), 
							Math.max(0, Math.min(180, 435-se.getTemperature())), 
							Math.max(0, 255-se.getTemperature())));
					g.fill(areaShape);
					result.add(new RenderedObject(se.getBuilding(),areaShape));	
				}
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

	public void callback(Object input){
		if(input instanceof String){
			if(!prediction.startLearning()){
				String data = (String)input;
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
				}
			}else{
				System.out.println("Progressing simulation");
				prediction.simulateStep();
				//TODO: All below is for testing purpose
//				BuildingData bd = new BuildingData(19, 0, 0);
//				System.out.println(bd);
//				System.out.println(Stringify.parseString(Stringify.serialize(Stringify.serialize("hej", "hejsan"))));
//				System.out.println(Stringify.parseString(bd.toString()));
//				System.out.println(new BuildingData(bd.toString()));
			}
		}
		else if(input instanceof KVTimestep){
			KVTimestep t = (KVTimestep)input;
			System.out.println("new timestep: " + t.getTime());

			//			List<BuildingData> stepBuildingData = new ArrayList<BuildingData>();
			//			for(StandardEntity se : world.getEntitiesOfType(StandardEntityURN.BUILDING)){
			//				Building b = (Building)se;
			//				if(b.getFieryness() != 0 || b.getTemperature() > 0){
			//					BuildingData bd = new BuildingData(b);
			//					stepBuildingData.add(bd);
			//				}
			//			}
			if(prediction.PRACTICE){
				prediction.gatherDataOnStep();
			}else{
				prediction.simulateStep();
			}
			if(t.getTime() == 50){
				prediction.startLearning();
			}
			//			practiceData.put(t.getTime(), stepBuildingData);
		}
	}
}

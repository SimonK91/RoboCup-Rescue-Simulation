package team.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

//import rescuecore.objects.Road;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import team.AbstractObstacleDetector;
import team.obstacledetectors.ObstacleDetector;

public class ObstacleLayer extends StandardViewLayer {
    protected List<Area> entities;
    protected AbstractObstacleDetector detector;

    public ObstacleLayer() {
    	entities = new ArrayList<Area>();
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
            for (Area next : entities) {
            	
            	for(EntityID neighbour_id : next.getNeighbours())
            	{
            		if(neighbour_id.getValue() < next.getID().getValue())
            		{
            			Entity e = world.getEntity(neighbour_id);
            			if(e instanceof Area)
            			{
            				Area neighbour = (Area)e;     				
            				
            				Edge sharedEdge = next.getEdgeTo(neighbour.getID()); 
					    	int centerx = (sharedEdge.getEndX()-sharedEdge.getStartX())/2+sharedEdge.getStartX();
					    	int centery = (sharedEdge.getEndY()-sharedEdge.getStartY())/2+sharedEdge.getStartY();
            				
            				Polygon line = new Polygon();
            				line.addPoint(next.getX(), next.getY());
            				line.addPoint(centerx, centery);
            				
            				
            				
            				
            				Polygon line2 = new Polygon();
            				line2.addPoint(centerx,centery);
            				line2.addPoint(neighbour.getX(),neighbour.getY());
            				
            				Polygon rect = ((ObstacleDetector)detector).Rect(line, 500);
            				Polygon rect2 = ((ObstacleDetector)detector).Rect(line2, 500);
            				Boolean b = detector.isPassable(next, neighbour);
            				for(int i = 0; i<4; i++){
            					Line2D shape = new Line2D.Double(
                						transform.xToScreen(rect.xpoints[i]), transform.yToScreen(rect.ypoints[i]),
                						transform.xToScreen(rect.xpoints[(i+1)%4]), transform.yToScreen(rect.ypoints[(i+1)%4]));
            					Line2D shape2 = new Line2D.Double(
                						transform.xToScreen(rect2.xpoints[i]), transform.yToScreen(rect2.ypoints[i]),
                						transform.xToScreen(rect2.xpoints[(i+1)%4]), transform.yToScreen(rect2.ypoints[(i+1)%4]));
            					g.setColor(b?Color.green:Color.red);
                				g.draw(shape);
                				g.draw(shape2);
                				result.add(new RenderedObject(next, shape));
                				result.add(new RenderedObject(next, shape2));
            				}
            			}
            		}
            	}
            	
            	if(next instanceof Road){
            		Road r = (Road)next;
            		if(r.getBlockades()==null) continue;
            		for(EntityID id : r.getBlockades()){
            			Blockade b = ((Blockade)world.getEntity(id));
            			
            			Boolean x = true;
            			int temp = 0;
            			Polygon p = new Polygon();
            			for(int i : b.getApexes()){
            				if(x){
            					x=!x;
            					temp = i;
            					
            				} else {
            					x=!x;
            					p.addPoint(transform.xToScreen(temp), transform.yToScreen(i));
            				}
            			}

            			g.setColor(Color.blue);
            			g.draw(p);
            			result.add(new RenderedObject(next,p));	
            		}
            	}
            }
            return result;
        }
	}
	

	@Override
	public String getName() {
		return "Obstacle";
	}

}

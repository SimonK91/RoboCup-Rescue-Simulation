package team.obstacledetectors;

import java.awt.Polygon;
import com.sun.javafx.geom.Point2D;
import com.sun.javafx.geom.Line2D;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class ObstacleDetector extends team.AbstractObstacleDetector {
    StandardWorldModel model;

    public ObstacleDetector(StandardWorldModel m)
    {
        super(m);
        model = m;
    }

    public Boolean isPassable(Area _source, Area _destination)
    {
    	
    	Edge sharedEdge = _source.getEdgeTo(_destination.getID()); 
    	if (sharedEdge == null) return false;
    
    	int centerx = (sharedEdge.getEndX()-sharedEdge.getStartX())/2+sharedEdge.getStartX();
    	int centery = (sharedEdge.getEndY()-sharedEdge.getStartY())/2+sharedEdge.getStartY();
    	
    	Polygon startToCenter = new Polygon();
    	startToCenter.addPoint(_source.getX(), _source.getY());
    	startToCenter.addPoint(centerx, centery);
    	
    	Polygon centerToEnd = new Polygon();
    	centerToEnd.addPoint(centerx, centery);
    	centerToEnd.addPoint(_destination.getX(),_destination.getY());
		
		if (intersect(_source, Rect(startToCenter,500)) || intersect(_destination, Rect(centerToEnd,500))) return false;
        
        return true;
    }
    
    private Boolean intersect(Area area, Polygon poly){
    	if (area.getBlockades() == null) return false;

    	Line2D[] s = new Line2D[4];
    	for(int i = 0; i < 4; i++){
    		s[i]=new Line2D(new Point2D(poly.xpoints[i],poly.ypoints[i]),new Point2D(poly.xpoints[(i+1)%4],poly.ypoints[(i+1)%4]));
    	}
    	
    	for(EntityID blockid : area.getBlockades()){
    		Blockade b  = (Blockade)model.getEntity(blockid);
    		int temp = 0;
    		Boolean x = true;
    		Point2D lastPoint = null;
    		Point2D firstPoint = null;
    		if(b.getApexes()==null) continue;
    		for(int apex : b.getApexes()){
    			if(x){
    				x=!x;
    				temp = apex;
    			} else {
    				x=!x;
    				
    				Point2D p = new Point2D(temp,apex);
    				if(lastPoint==null) firstPoint = p;
    				for(Line2D l : s)
    					if(lastPoint!=null && intersect(lastPoint,p,l))
    						return true;
    				lastPoint = p;
    			}
    		}
			for(Line2D l : s)
				if(lastPoint!=null && intersect(lastPoint,firstPoint,l))
					return true;
    	} 	
    
    	return false;
    }
    
    public Polygon Rect(Polygon line, int width){
    	Polygon p = new Polygon();
    	int x1 = line.xpoints[0];
    	int x2 = line.xpoints[1];
    	int y1 = line.ypoints[0];
    	int y2 = line.ypoints[1];
    	int halfwidth = width/2;

		int vx = x2-x1;
		int vy = y2-y1;
		
		int px = vy;
		int py = -vx;
		
		double len = Math.sqrt(px * px + py * py);
		
		int nx = (int)(px * halfwidth / len);
		int ny = (int)(py * halfwidth / len);
		
		p.addPoint(x1 + nx, y1 + ny);
		p.addPoint(x1 - nx, y1 - ny);
		p.addPoint(x2 - nx, y2 - ny);
		p.addPoint(x2 + nx, y2 + ny);
    	
    	return p;    	
    }
    
    //If the line P (from p1 to p2) intersects Q the function returns true
    private Boolean intersect(Point2D p1, Point2D p2, Line2D q){
    	Line2D p = new Line2D(p1,p2);
    	return p.intersectsLine(q);
    }
}

package team.viewers;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


import rescuecore2.Constants;
import rescuecore2.Timestep;
import rescuecore2.config.Config;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.AnimatedWorldModelViewer;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import team.SampleLayer;

public class CenterViewer {
    private static final int DEFAULT_FONT_SIZE = 20;
    private static final int PRECISION = 3;

    private static final String FONT_SIZE_KEY = "viewer.font-size";
    private static final String MAXIMISE_KEY = "viewer.maximise";
    private static final String TEAM_NAME_KEY = "viewer.team-name";

    private ScoreFunction scoreFunction;
    private AnimatedWorldModelViewer viewer;
    private JLabel timeLabel;
    private JLabel scoreLabel;
    private JLabel teamLabel;
    private JLabel mapLabel;
    private NumberFormat format;

    private StandardWorldModel _model;
    private Config _config;
    private JFrame _jframe;
    
    public CenterViewer(StandardWorldModel m, Config c){
    	_model = m;
    	_config = c;
    }
    
    
    public void postConnect() {
      
        int fontSize = _config.getIntValue(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        String teamName = _config.getValue(TEAM_NAME_KEY, "");
       
        format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(PRECISION);
        
        _jframe = new JFrame("Center Viewer");// " + " (" + _model.getAllEntities().size() + " entities)");
        viewer = new AnimatedWorldModelViewer();
        viewer.initialise(_config);
        viewer.view(_model);
        // CHECKSTYLE:OFF:MagicNumber
        viewer.setPreferredSize(new Dimension(500, 500));
        viewer.addLayer(new SampleLayer());
        // CHECKSTYLE:ON:MagicNumber
     //   timeLabel = new JLabel("Time: Not started", JLabel.CENTER);
     //   teamLabel = new JLabel(teamName, JLabel.CENTER);
     //   scoreLabel = new JLabel("Score: Unknown", JLabel.CENTER);
        //String mapdir=_config.getValue("gis.map.dir").trim();
        /*
		String[] map_spl = mapdir.split("/");
		int index = map_spl.length-1;
		String mapname = map_spl[index].trim();
		if(mapname.equals(""))
			mapname = map_spl[--index].trim();
		if(mapname.equals("map"))
			mapname = map_spl[--index].trim();
			*/
        
        //String totalTime = _config.getValue("kernel.timesteps");
        //int channelCount = _config.getIntValue("comms.channels.count")-1;//-1 for say
        
       // mapLabel=new JLabel(mapname+" ("+totalTime+") | "+(channelCount==0? "No Comm":channelCount+" channels"), JLabel.CENTER);
      //  timeLabel.setBackground(Color.WHITE);
       // timeLabel.setOpaque(true);
       // timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, fontSize));
       // teamLabel.setBackground(Color.WHITE);
       // teamLabel.setOpaque(true);
       // teamLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, fontSize));
       // scoreLabel.setBackground(Color.WHITE);
       // scoreLabel.setOpaque(true);
       // scoreLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, fontSize));
        
      //  mapLabel.setBackground(Color.WHITE);
      //  mapLabel.setOpaque(true);
      //  mapLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, fontSize));
        
        _jframe.add(viewer, BorderLayout.CENTER);
        
        // CHECKSTYLE:OFF:MagicNumber
        // CHECKSTYLE:ON:MagicNumber
        
        _jframe.pack();
        if (_config.getBooleanValue(MAXIMISE_KEY, false)) {
        	_jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        _jframe.setVisible(true);

        viewer.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
                    for (RenderedObject next : objects) {
                        System.out.println(next.getObject());
                    }
                }

                @Override
                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
                }
            });
    }


    public void handleTimestep(final int t) {
   //     super.handleTimestep(t);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //timeLabel.setText("Time: " + t);
                    //scoreLabel.setText("Score: " + format.format(scoreFunction.score(_model, new Timestep(t.getTime()))));
                    viewer.view(_model);//, t.getCommands());
                    viewer.repaint();
                }
            });
    }

    @Override
    public String toString() {
        return "Center viewer";
    }


}

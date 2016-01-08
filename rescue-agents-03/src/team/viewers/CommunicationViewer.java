package team.viewers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import rescuecore2.config.Config;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.AnimatedWorldModelViewer;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import team.AbstractAgent;

public class CommunicationViewer{
    private static final int DEFAULT_FONT_SIZE = 20;
    private AnimatedWorldModelViewer viewer;

    private static final String FONT_SIZE_KEY = "viewer.font-size";
    private static final String MAXIMISE_KEY = "viewer.maximise";
    private static final String TEAM_NAME_KEY = "viewer.team-name";
    
    private JTextArea _textArea;
    private JFrame _frame;
	private AbstractAgent _agent;
    private Config _config;
    private StandardWorldModel _model;
    
    public CommunicationViewer(AbstractAgent agent, Config config, StandardWorldModel model)
    {
    	_agent = agent;
    	_config = config;
    	_model = model;
    }
    
	public void postConnect() {
		
        int fontSize = _config.getIntValue(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        String teamName = _config.getValue(TEAM_NAME_KEY, "");
        
        _frame = new JFrame("Viewer " + _agent.getName());

        /*
        viewer = new AnimatedWorldModelViewer();
        viewer.initialise(config);
        viewer.view(model);
        viewer.setVisible(true);
        */
        
        // CHECKSTYLE:OFF:MagicNumber
        JPanel labels = new JPanel(new GridLayout(1, 4));
        _textArea = new JTextArea();
        // CHECKSTYLE:ON:MagicNumber
        _frame.setSize(200, 200);
        _frame.add(labels, BorderLayout.NORTH);
        _frame.add(_textArea);
        _textArea.setEditable(false);
        /*
        if (_config.getBooleanValue(MAXIMISE_KEY, false)) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        */
        _frame.setVisible(true);
	}
	
	public void AddText(String text)
	{
		_textArea.append(text + "\n");
	}
	
	public void Clear()
	{
		_frame.remove(_textArea);
		_textArea = new JTextArea();
		_frame.add(_textArea);
	}
	
    @Override
    public String toString() {
        return "Communication viewer";
    }
}

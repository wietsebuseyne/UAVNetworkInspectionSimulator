package simulation;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JFrame;

import network.Node;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import uav.UAV;
import ui.control.ControlFrame;
import ui.portrayals.NodePortrayal2D;
import ui.portrayals.UAVPortrayal2D;

public class UAVNetworkSimulationGUI extends GUIState {
	
	public Display2D display;
	public JFrame displayFrame, controlFrame;
	ContinuousPortrayal2D mapPortrayal = new ContinuousPortrayal2D();
	NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();
	
	public static void main(String[] args) {
		UAVNetworkSimulationGUI vid = null;
		try {
			vid = new UAVNetworkSimulationGUI();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(-1);
		}
		//((UAVNetworkSimulation)vid.state).setNavigationBehaviour(new NavigationInitializationClass(CombinedLNIPathNavigation.class));
		//((UAVNetworkSimulation)vid.state).initiliazeCycleNavigation();
		//((UAVNetworkSimulation)vid.state).setNumUAVs(3);
		//NetworkSplitter splitter = new NetworkSplitter();
		//System.out.println(splitter.split(((UAVNetworkSimulation)vid.state).network, 5000));
		Console c = new Console(vid);
		c.setVisible(true);
	}

	public UAVNetworkSimulationGUI() throws IOException {
		super(new UAVNetworkSimulation(System.currentTimeMillis(), "configuration.json"));
	}
	
	public UAVNetworkSimulationGUI(SimState state) {
		super(state);
	}
	
	public static String getName() { return "UAV Network Simulation"; }
	
	public void start() {
		super.start();
		setupPortrayals();
	}
	
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}
	
	public void setupPortrayals() {
		UAVNetworkSimulation uavs = (UAVNetworkSimulation) state;
		
		networkPortrayal.setField(new SpatialNetwork2D(uavs.map, uavs.network));
		if(uavs.getNavigationBehaviourClass().getName().equals("ACONavigation"))
			networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D(Color.BLACK, Color.BLACK, Color.BLACK, new Font("Arial", Font.PLAIN, 10)));
		
		// tell the portrayals what to portray and how to portray them
		mapPortrayal.setField(uavs.map);
		//mapPortrayal.setPortrayalForAll(new OvalPortrayal2D());
		mapPortrayal.setPortrayalForClass(UAV.class, new UAVPortrayal2D());
		mapPortrayal.setPortrayalForClass(Node.class, new NodePortrayal2D());
				
		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);
		// redraw the display
		display.repaint();
	}
	
	public void init(Controller c) {
		super.init(c);
		display = new Display2D(600,600,this);
		//display.setClipping(false);
		displayFrame = display.createFrame();
		displayFrame.setTitle("UAV graph simulation");
		c.registerFrame(displayFrame);
		// so the frame appears in the "Display" list
		displayFrame.setVisible(true);
		display.attach(networkPortrayal, "Infrastructure Network");
		display.attach(mapPortrayal, "Map");
		

		controlFrame = new ControlFrame(this);
		controlFrame.setTitle("UAV Control Panel");
		controlFrame.setVisible(true);
		c.registerFrame(controlFrame);
		
	}
	
	public void quit() {
		super.quit();
		if (displayFrame!=null) displayFrame.dispose();
		if (controlFrame!=null) controlFrame.dispose();
		displayFrame = null;
		controlFrame = null;
		display = null;
	}

}

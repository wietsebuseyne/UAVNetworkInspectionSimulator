package simulation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import simulation.event.EventManager;
import sla.SingleInspectionSLA;
import sla.report.SLAManager;
import sla.report.SLAReporter;
import uav.UAVConfiguration;
import uav.UAVController;
import uav.navigation.ACONavigation;
import uav.navigation.CentralServerLNINavigation;
import uav.navigation.CycleNavigation;
import uav.navigation.NavigationInitializationClass;
import uav.navigation.UAVNavigationStrategy;
import util.algorithms.UndirectedGraphCycleFinder;
import util.algorithms.dijkstra.Vertex;
import util.parsers.configuration.Configuration;
import util.parsers.configuration.ConfigurationFileParser;
import util.parsers.network.NetworkParser;


public class UAVNetworkSimulation extends SimState {
	
	private static final long serialVersionUID = 1L;
	private int numUAVs = 1;
	public UAVNetwork network;
	public Continuous2D map;
	private UAVController controller = new UAVController(this);
	public SLAManager slaChecker;
	public SLAReporter reporter;
	
	private NetworkParser parser;
	private Configuration configuration;
	
	private NavigationInitializationClass navigationInitClass = new NavigationInitializationClass(CentralServerLNINavigation.class);
	private boolean useCycleNavigation = false;
	private CycleNavigation cycleNavigation;
	private List<Integer> cycle;
	private EventManager eventManager = new EventManager();
	private double safetyMultiplierRechargeNodes = 0.75;
	
	public UAVNetworkSimulation(long seed) throws IOException {
		this(seed, "configuration.json");
	}

	public UAVNetworkSimulation(long seed, String configurationFile) throws IOException {
		super(seed);
		
		ConfigurationFileParser configurationParser = new ConfigurationFileParser(configurationFile);
		this.configuration = configurationParser.parse();
		
		ACONavigation.ALPHA = configuration.alpha;
		ACONavigation.BETA = configuration.beta;
		
		parser = new NetworkParser(configuration.network, 
				configuration.uavConfiguration.getMaxFlyingDistance() * safetyMultiplierRechargeNodes, getConfiguration().addRechargeNodes);
		
		long cycle = 0;
		if(configuration.edgeInspectionSLAs == 0)
			cycle = configuration.nodeInspectionSLAs;
		else if(configuration.nodeInspectionSLAs == 0)
			cycle = configuration.edgeInspectionSLAs;
		else
			cycle = Math.min(configuration.nodeInspectionSLAs, configuration.edgeInspectionSLAs);
		if(cycle == 0)
			throw new IllegalArgumentException("Please add inspection parameters for the nodes, the edges or both");
		controller.setCycle(cycle, 1);
		
		parser.parse();
		network = parser.getNetwork();
		map = new Continuous2D(1.0, parser.getMaxX()*1.05, parser.getMaxY()*1.05);
		slaChecker = new SLAManager(this);
		slaChecker.setFlightTimeSLA(configuration.inspectionDaysPerMonth, configuration.inspectionMinutesPerDay);
		reporter = new SLAReporter(slaChecker);
		eventManager.addAll(getConfiguration().getEventGenerators(this));
	}
	
	public void initializeCycleNavigation() {
		if(cycle == null) {
			UndirectedGraphCycleFinder cpp = new UndirectedGraphCycleFinder(network.allNodes.size());
			
			for(int i = 0; i < network.allNodes.size()-1; i++) {
				for(int j = i+1; j < network.allNodes.size(); j++) {
					if(network.getAdjacencyMatrix()[i][j] != null) {
						cpp.addArc("a", i, j, (float)network.getAdjacencyMatrix()[i][j].getWeight());
					}
				}
			}
			cpp.solve();
	
			cycle = cpp.getCycle();
			cycleNavigation = new CycleNavigation(cpp.getCycle());
		}
		this.useCycleNavigation = true;
	}
	
	public void setCycleNavigation(boolean cycle) {
		if(this.cycle == null)
			throw new IllegalArgumentException("The cycle is not yet set. Initialize the cycle navigation before using this method.");
		this.useCycleNavigation = cycle;
	}
	
	public boolean usingCycleNavigation() {
		return useCycleNavigation;
	}
	
	public void unsetCycleNavigation() {
		useCycleNavigation = false;
	}
	
	public double getTotalDistanceOfNetwork() {
		if(parser.getTotalDistance() == 0)
			throw new IllegalStateException("The network is not yet initialized");
		return parser.getTotalDistance();
	}
	
	public Class<? extends UAVNavigationStrategy> getNavigationBehaviourClass() {
		if(usingCycleNavigation())
			return cycleNavigation.getClass();
		return navigationInitClass.getNavigationBehaviour();
	}
	
	public UAVNavigationStrategy getNavigationBehaviour() {
		if(usingCycleNavigation())
			return cycleNavigation;
		return navigationInitClass.newInstance();
	}
	
	public String getCurrentNavigationName() {
		return navigationInitClass.newInstance().toString();
	}

	public UAVController getUAVController() {
		return controller;
	}
	
	public int getNumUAVs() {
		return numUAVs;
	}

	public void setNumUAVs(int numUAVs) {
		if(numUAVs <= 0)
			throw new IllegalArgumentException("The number of UAVs must be positive");
		this.numUAVs = numUAVs;
	}

	public SLAManager getSlaChecker() {
		return slaChecker;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void start() {
		super.start();
		controller.clear();
		slaChecker.clear();
		parser = new NetworkParser(configuration.network, 
				configuration.uavConfiguration.getMaxFlyingDistance() * safetyMultiplierRechargeNodes, 
				!navigationInitClass.getNavigationBehaviour().getName().startsWith("uav.navigation.path"));
		try {
			parser.parse();
			network = parser.getNetwork();
			map = new Continuous2D(1.0, parser.getMaxX()*1.05, parser.getMaxY()*1.05);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bag nodes = network.getAllNodes();
		for(int i = 0; i < nodes.size(); i++) {
			Object node1 = nodes.get(i);
			map.setObjectLocation(node1, 
					((Node)node1).getLocation());
		}
		addUAVs();

		initializeSLAs();
	}
	
	public Vertex nodeToVertex(Node n) {
		return parser.getVertex(n);
	}
	
	public void addUAVs() {
		if(useCycleNavigation) {
			for(int i = 0; i < numUAVs; i++) {
				controller.addUAV(network.getNode(0), new CycleNavigation(cycle), (getStepsInNavCycle()/numUAVs)*i);
			}
		} else {
			int mul = network.allNodes.size() / numUAVs;
			for(int i = 0; i < numUAVs; i++) {
				try {
					controller.addUAV((Node)network.allNodes.get((mul*i)%network.allNodes.size()), navigationInitClass.newInstance());
				} catch (IllegalArgumentException ex) {
					System.out.println("Invalid navigation behaviour\nDetails: " + ex.getMessage());
				}
			}
		}
	}
		
	@SuppressWarnings("unchecked")
	public void initializeSLAs() {
		if(configuration.nodeInspectionSLAs > 0)
			for(Object o : network.allNodes)
				slaChecker.addInspectionSLA(new SingleInspectionSLA((Node)o,  configuration.nodeInspectionSLAs));
		
		Set<InspectableEdge> edges = new HashSet<>();
		for(Object o : network.allNodes) {
			edges.addAll(network.getEdgesIn((Node) o));
		}

		if(configuration.edgeInspectionSLAs > 0)
			for(InspectableEdge e : edges) {
				//e.setRiskLevelMultiplier(multipliers.get(i));
				slaChecker.addInspectionSLA(new SingleInspectionSLA(e, /*multipliers.get(i) **/ configuration.edgeInspectionSLAs));
				}
		
		slaChecker.setResponseTimeSLA(getConfiguration().responseTimeSLA);
	}
	
	public void addEvents(long firstStep, long lastStep) {
		eventManager.scheduleEvents(this, firstStep, lastStep);
	}
	
	@Override
	public void finish() {
	}

	public void addToDatasets(int nbOfUAVs, String strategy, long firstStep, long lastStep, boolean boxDS) {
		reporter.addToDatasets(nbOfUAVs, strategy);
		if(boxDS)
			reporter.addToBoxDatasets(nbOfUAVs, strategy, firstStep, lastStep);
	}
	
	public void setNavigationBehaviour(NavigationInitializationClass navigationInitClass) {
		if(navigationInitClass == null)
			throw new IllegalArgumentException("The navigation initialization class cannot be null");
		this.navigationInitClass = navigationInitClass;
	}
	
	public long getStepsInNavCycle() {
		if(cycleNavigation == null)
			throw new IllegalStateException("You must first initialize the cycle navigation before getting the steps in the cycle");
		return cycleNavigation.getStepsInCycle();
	}
	
	public void initializeNumUAVs() {
		this.numUAVs = configuration.startUAVs;
	}
	
	public double percentageFulfilledBetween(long firstStep, long lastStep) {
		return slaChecker.percentageFulfilledBetween(firstStep, lastStep);
	}
	
	public boolean SLAsFulfilled(long firstStep, long lastStep) {
		return slaChecker.SLAsFulfilled(configuration.averageCoverageGoal, configuration.coveragePerSLAGoal, firstStep, lastStep);
	}

	public boolean allSLAsFulfilled(long firstStep, long lastStep) {
		return slaChecker.percentageFulfilledBetween(firstStep, lastStep) >= configuration.averageCoverageGoal 
				&& slaChecker.allSLAsAbove(configuration.coveragePerSLAGoal, firstStep, lastStep);
	}
	
	public CycleNavigation getCycleNavigation() {
		return cycleNavigation;
	}
	
	public void setUAVConfiguration(UAVConfiguration conf) {
		//If battery life changed and cyclenavigation is used, the cycle needs to be reinitialized
		if(this.configuration.uavConfiguration.batteryLife != conf.batteryLife && getNavigationBehaviourClass().getSimpleName().equals("CycleNavigation")) {
			cycle = null;
			parser = new NetworkParser(configuration.network, 
					conf.getMaxFlyingDistance() * safetyMultiplierRechargeNodes, 
					!navigationInitClass.getNavigationBehaviour().getName().startsWith("uav.navigation.path"));
			try {
				parser.parse();
				network = parser.getNetwork();
				map = new Continuous2D(1.0, parser.getMaxX()*1.05, parser.getMaxY()*1.05);
			} catch (IOException e) {
				e.printStackTrace();
			}
			initializeCycleNavigation();
		}
		this.configuration.uavConfiguration = conf;
	}

	public long getStepsInSLACycle() {
		return getUAVController().getStepsInCycle();
	}
	
	public boolean isUAVFlyTime() {
		return slaChecker.isUAVFlyTime(schedule.getSteps());
	}

	public void resetDijkstra() {
		parser.resetDijkstra();
	}

}

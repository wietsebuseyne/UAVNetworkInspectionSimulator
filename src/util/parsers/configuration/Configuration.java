package util.parsers.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import simulation.UAVNetworkSimulation;
import simulation.event.generator.EventGenerator;
import simulation.event.generator.EventGeneratorFactory;
import uav.UAVConfiguration;
import uav.navigation.UAVNavigationStrategy;

/**
 * Configuration for a simulation of UAVs.
 * 
 * @author Wietse Buseyne
 *
 */
public class Configuration {
	
	public String network = "elia.json";
	public long steps = 14400;
	public int inspectionMinutesPerDay = 240, inspectionDaysPerMonth = 15;
	public int startUAVs = 1;
	public List<Class<? extends UAVNavigationStrategy>> navigationStrategies = new ArrayList<Class<? extends UAVNavigationStrategy>>();
	public long nodeInspectionSLAs = 0;
	public long edgeInspectionSLAs = 0;
	public long responseTimeSLA = 1440;
	public double averageCoverageGoal = 90;
	public double coveragePerSLAGoal = 90;
	public int numUAVsSteps = 5;
	public boolean slaCoverage_time_combined = false,
		slaCoverage_time_singles = false,
		slaCoverage_totalPercentage_combined = false,
		uavFlyingTime = false,
		cyclePercentageTest = false;
	public double alpha = 1, beta = 1;
	
	public UAVConfiguration uavConfiguration = new UAVConfiguration();
	
	public List<Map<String, Object>> eventGenerators = new ArrayList<Map<String,Object>>();
	public boolean addRechargeNodes = true;
	
	public List<EventGenerator> getEventGenerators(UAVNetworkSimulation sim) {
		List<EventGenerator> egs = new ArrayList<EventGenerator>();
		EventGeneratorFactory egf = new EventGeneratorFactory(sim);
		for(Map<String, Object> eg : eventGenerators) {
			egs.add(egf.createEventGenerator(eg));
		}
		return egs;
	}
	
}

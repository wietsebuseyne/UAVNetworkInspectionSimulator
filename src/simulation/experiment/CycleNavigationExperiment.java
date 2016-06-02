package simulation.experiment;

import network.Node;
import simulation.UAVNetworkSimulation;

/**
 * An experiment that initializes a cycle so that the simulation object is ready to simulate using the CycleNavigation class.
 * Returns the number of UAVs that is needed for a coverage of 100% (if no unexpected events or failures happen).
 * 
 * @author Wietse Buseyne
 *
 */
public class CycleNavigationExperiment extends Experiment {

	public CycleNavigationExperiment(UAVNetworkSimulation sim) {
		super(sim);
	}

	@Override
	public void execute() {
		System.out.println("Searching cycle in graph, this might take a while...");
		sim.initializeCycleNavigation();

		//start experiment
		sim.getUAVController().addUAV((Node)sim.network.allNodes.get(0), sim.getCycleNavigation());
		
		do {
			if (!sim.schedule.step(sim)) break;
		} while(sim.getStepsInNavCycle() == -1);
		
		int uavs = (int) (sim.getStepsInNavCycle() / sim.getStepsInSLACycle())+1;
		
		System.out.println("With " + uavs + " UAVs and no UAV failures or other events a coverage of 100% can be reached using cyclenavigation.");
		
		sim.finish();
	}

}

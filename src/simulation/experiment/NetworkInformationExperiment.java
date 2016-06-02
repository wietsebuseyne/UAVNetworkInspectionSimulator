package simulation.experiment;

import simulation.UAVNetworkSimulation;

/**
 * An experiment that prints information about the network on the console.
 * The information cosists of the number of nodes and edges, the size of the network 
 * and the number of recharge nodes needed for the provided UAV specification and network to guarantee the UAVs will not run out of battery while flying.
 * 
 * @author Wietse Buseyne
 *
 */
public class NetworkInformationExperiment extends Experiment {

	public NetworkInformationExperiment(UAVNetworkSimulation sim) {
		super(sim);
	}
	
	@Override
	public void execute() {
		System.out.println("Total nodes:\t" + sim.network.getNbNodes());
		System.out.println("Total edges:\t" + sim.network.getNbEdges());
		System.out.println("Total size:\t" + sim.getTotalDistanceOfNetwork() + "km");
		System.out.println("For the provided network and UAV constraints, " + sim.network.allNodes.size() + 
				" recharge nodes had to be added.");
	}

}

package uav.navigation;

import java.util.List;

import network.Inspectable;
import network.Node;
import simulation.UAVNetworkSimulation;
import uav.JobDescription;

/**
 * A basic navigation strategy that chooses a random starting node for the UAV.
 * Does not do anything when asked to inspect a node or edge and simply continues its navigation as before.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class SimpleStartLocationNavigation implements UAVNavigationStrategy {
	
	protected EdgeNodeLocation destination = null;
	
	@Override
	public EdgeNodeLocation getDestination() {
		return destination;
	}
	
	@Override
	public Node getStartLocation(UAVNetworkSimulation sim) {
		return (Node)sim.network.allNodes.get(sim.random.nextInt(sim.network.allNodes.size()));
	}
	
	@Override
	public void inspectionDoneOrStarted(Inspectable inspectable, long time){}
	
	@Override
	public void inspect(List<EdgeNodeLocation> nextLocations) {
	}
	
	@Override
	public JobDescription getJob() {
		return JobDescription.MONITORING;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}

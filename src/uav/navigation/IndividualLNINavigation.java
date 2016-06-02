package uav.navigation;

import java.util.HashMap;
import java.util.Map;

import network.InspectableEdge;
import network.Node;
import sim.util.Bag;
import simulation.UAVNetworkSimulation;

/**
 * A simple navigation strategy that will navigate over the longest not inspected edge while not sharing information with other UAVs.
 * The decision is solely made on the times the UAV itself inspected certain edges.
 * 
 * @author Wietse Buseyne
 *
 */
public class IndividualLNINavigation extends SimpleStartLocationNavigation {
	
	private Map<InspectableEdge, Long> inspectionTimes = new HashMap<InspectableEdge, Long>();
	private InspectableEdge currentlyInspecting;

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		inspectionTimes.put(currentlyInspecting, sim.schedule.getSteps());
		
		Bag edges = sim.network.getEdges(currentLocation, null);
		InspectableEdge nextEdge = null;
		long min = Long.MAX_VALUE;
		for(Object o : edges) {
			if(inspectionTimes.get(o) == null || inspectionTimes.get(o) < min) {
				min = inspectionTimes.get(o) == null ? 0 : inspectionTimes.get(o);
				nextEdge = (InspectableEdge) o;
			}				
		}
		currentlyInspecting = nextEdge;
		destination = new EdgeNodeLocation(nextEdge, (Node)nextEdge.getOtherNode(currentLocation));
		return destination;
	}

}

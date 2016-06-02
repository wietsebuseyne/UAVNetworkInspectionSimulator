package uav.navigation;

import java.util.HashMap;
import java.util.Map;

import network.Inspectable;
import network.InspectableEdge;
import network.Node;
import sim.util.Bag;
import simulation.UAVNetworkSimulation;

/**
 * A simple navigation strategy that chooses the longest not inspected edge and broadcasts its navigation information when starting to inspect a new edge.
 * When receiving information from other UAVs that they inspected an edge, this information will be joined and the strategy will keep it in mind when choosing the next edge.
 * 
 * @author Wietse Buseyne
 *
 */
public class InterUAVLNINavigation extends SimpleStartLocationNavigation {
	
	private Map<InspectableEdge, Long> inspectionTimes = new HashMap<InspectableEdge, Long>();
	private InspectableEdge currentlyInspecting;

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		sim.getUAVController().sendInspectionMessage(currentLocation.getLocation(), currentlyInspecting, sim.schedule.getSteps());
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
		//Send message for new edge we are going to inspect, even if we did not inspect it yet
		//Like this, we can prevent other UAVs from starting to inspect this edge
		sim.getUAVController().sendInspectionMessage(currentLocation.getLocation(), currentlyInspecting, sim.schedule.getSteps());
		destination = new EdgeNodeLocation(nextEdge, (Node)nextEdge.getOtherNode(currentLocation));
		return destination;
	}
	
	@Override
	public void inspectionDoneOrStarted(Inspectable inspectable, long time) {
		if(inspectable instanceof InspectableEdge && 
				(inspectionTimes.get((InspectableEdge) inspectable) == null || inspectionTimes.get((InspectableEdge) inspectable) < time))
			inspectionTimes.put((InspectableEdge) inspectable, time);
	}

}

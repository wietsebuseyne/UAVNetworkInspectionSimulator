package uav.navigation;

import java.util.ArrayList;
import java.util.List;

import network.InspectableEdge;
import network.Node;
import simulation.UAVNetworkSimulation;

/**
 * Uses the basic principle of choosing the edge that has not been inspected for the longest amount of time, in combination with a central server for communication.
 * If a request for inspection arrives, will fly over the requested path before continuing normal navigation behaviour.
 * 
 * @author Wietse Buseyne
 *
 */
public class CentralServerLNINavigation extends CentralServerNavigation {
	
	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation uavNetworkSimulation, Node currentLocation) {
		if(!inspectionQueue.isEmpty()) {
			destination = inspectionQueue.poll();
			if(!destination.getEdge().getFrom().equals(currentLocation) && ! destination.getEdge().getTo().equals(currentLocation))
				throw new IllegalStateException("Destination is not connected to current location");
			return destination;
		}
		
		List<InspectableEdge> edges = new ArrayList<InspectableEdge>();
		
		for(Object o : uavNetworkSimulation.network.getEdges(currentLocation, null)) {
			long lastInspectionStarted = ((InspectableEdge) o).getLastInspectionStartTime();
			if(edges.isEmpty() || (lastInspectionStarted < edges.get(0).getLastInspectionStartTime())) {
				edges.clear();
				edges.add((InspectableEdge) o);
			} else if(((InspectableEdge) o).getLastInspectionStartTime() == edges.get(0).getLastInspectionStartTime()) {
				edges.add((InspectableEdge) o);
			}
		}
		InspectableEdge nextEdge = null;
		for(InspectableEdge edge : edges)
			if(nextEdge == null || 
					edge.getLastInspectionTime() <
					nextEdge.getLastInspectionTime())
				nextEdge = edge;

		destination = new EdgeNodeLocation(nextEdge, (Node) nextEdge.getOtherNode(currentLocation));
		return destination;
	}

}

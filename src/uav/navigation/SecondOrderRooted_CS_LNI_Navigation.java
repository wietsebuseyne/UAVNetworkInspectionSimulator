package uav.navigation;

import java.util.ArrayList;
import java.util.List;

import network.InspectableEdge;
import network.Node;
import sim.util.Bag;
import simulation.UAVNetworkSimulation;

/**
 * @See SecondOrder_CS_LNI_Navigation
 * 
 * The only difference is we take the square root of all the inspection values before using them. 
 * By doing this, very small inspection times will have a bigger impact and are even more likely to be chosen.
 * 
 * @author Wietse Buseyne
 *
 */
public class SecondOrderRooted_CS_LNI_Navigation extends SecondOrder_CS_LNI_Navigation {

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		List<InspectableEdge> edges = new ArrayList<InspectableEdge>();
		for(Object o : sim.network.getEdges(currentLocation, null)) {
			long lastInspectionStarted = ((InspectableEdge) o).getLastInspectionStartTime();
			if(edges.isEmpty() || (lastInspectionStarted < edges.get(0).getLastInspectionStartTime() /*&& 
					lastInspection < edges.get(0).getLastInspectionStartTime()*/)) {
				edges.clear();
				edges.add((InspectableEdge) o);
			} else if(lastInspectionStarted == edges.get(0).getLastInspectionStartTime()) {
				edges.add((InspectableEdge) o);
			}
		}
		double min = Double.MAX_VALUE;
		InspectableEdge nextEdge = null;
		for(InspectableEdge edge : edges) {
			Node nextNode = (Node) edge.getOtherNode(currentLocation); //getTo().equals(currentLocation) ? (Node)edge.getFrom() : (Node)edge.getTo();
			Bag otherEdges = sim.network.getEdges(nextNode, null);
			if(otherEdges.size() > 1)
				otherEdges.remove(edge);
			
			double totalTimeSinceInspection = Math.sqrt(edge.getLastInspectionTime()) + Math.sqrt(getLastEdgeIT(sim, nextNode, edge));

			if(nextEdge == null || totalTimeSinceInspection < min) {
				nextEdge = edge;
				min = totalTimeSinceInspection;
			}
		}
		return new EdgeNodeLocation(nextEdge, (Node) nextEdge.getOtherNode(currentLocation));
	}

}

package uav.navigation;

import java.util.ArrayList;
import java.util.List;

import network.InspectableEdge;
import network.Node;
import sim.util.Bag;
import simulation.UAVNetworkSimulation;

/**
 * This variation of the CentralServerLNINavigation keeps the next possible move for the UAV in mind while deciding which edge to navigate over. 
 * To decide which edge will be chosen, each edge gets a value and the edge with the lowest value gets chosen. 
 * The value of edge is based on the last inspection time of that edge as well as the minimum of all inspection times of the edges of the node on the other side of the edge.
 * 
 * To explain more clearly, this variation also tries to optimize the node the UAV will navigate to when an edge is picked. 
 * One edge might not be a very good choice if we only look at the time it was last inspected, but the other side of that node might be connected to an edge that requires immediate inspection. 
 * This variation will take this information into account and thus navigate towards edges that need inspection. 
 * Important for this strategy is that it will not take intermediate nodes into account and skip those when looking for the node the UAV will navigate to, 
 * as these intermediate nodes will be inspected anyway when the UAV chooses this path.
 * 
 * @author Wietse Buseyne
 *
 */
public class SecondOrder_CS_LNI_Navigation extends SimpleStartLocationNavigation {

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
		long min = Long.MAX_VALUE;
		InspectableEdge nextEdge = null;
		for(InspectableEdge edge : edges) {
			Node nextNode = (Node) edge.getOtherNode(currentLocation); //getTo().equals(currentLocation) ? (Node)edge.getFrom() : (Node)edge.getTo();
			Bag otherEdges = sim.network.getEdges(nextNode, null);
			if(otherEdges.size() > 1)
				otherEdges.remove(edge);
			
			long totalTimeSinceInspection = (long) (edge.getLastInspectionTime() + getLastEdgeIT(sim, nextNode, edge));

			if(nextEdge == null || totalTimeSinceInspection < min) {
				nextEdge = edge;
				min = totalTimeSinceInspection;
			}
		}
		return new EdgeNodeLocation(nextEdge, (Node) nextEdge.getOtherNode(currentLocation));
	}
	
	protected long getLastEdgeIT(UAVNetworkSimulation sim, Node currentNode, InspectableEdge currentEdge) {
		Bag edges = sim.network.getEdges(currentNode, null);
		edges.remove(currentEdge);
		while (edges.size() == 1) {
			currentEdge = (InspectableEdge)edges.get(0);
			currentNode = (Node) currentEdge.getOtherNode(currentNode);
			edges = sim.network.getEdges(currentNode, edges);
			edges.remove(currentEdge);
		}
		CentralServerLNINavigation nav = new CentralServerLNINavigation();
		EdgeNodeLocation enl = nav.getNextDestination(sim, currentNode);
		return enl.getEdge().getLastInspectionTime();
	}

}

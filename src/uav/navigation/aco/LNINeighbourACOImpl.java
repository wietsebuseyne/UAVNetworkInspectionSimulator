package uav.navigation.aco;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;
import sim.util.Bag;

/**
 * An ACO implementation that takes the neighbours of the edges into account. 
 * 
 * The heuristic of an edge is the longest of the times without inspection of all its neighbouring edges (except the edges that have the starting node as one of their nodes), powered to beta.
 * The pheromone is calculated through following formula: [1 + (1/(pheromones+1))^2]^alpha
 * 
 * @author Wietse Buseyne
 *
 */
public class LNINeighbourACOImpl implements ACOImpl {
	
	public double computeHeuristicValue(UAVNetwork network, Node node, InspectableEdge edge, long currentStep, double beta) {
		double heuristic = 0;
		Bag edges = network.getEdges(edge.getOtherNode(node), null);
		for(Object o : edges) {
			InspectableEdge e = (InspectableEdge)o;
			long lit = e.getLIT();
			double newH = (currentStep - lit);
			if(newH > heuristic)
				heuristic = newH;
		}
		return Math.pow((currentStep - edge.getLIT()) + heuristic, beta);
	}
	
}

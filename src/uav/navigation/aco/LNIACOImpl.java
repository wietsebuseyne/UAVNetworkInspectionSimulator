package uav.navigation.aco;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;

/**
 * An ACO implementation that takes the neighbours of the edges into account. 
 * 
 * The heuristic of an edge is calculated based on its last inspection time: (time - edge.getLIT())^beta
 * The pheromone is calculated through following formula: [1 + (1/(pheromones+1))^2]^alpha
 * 
 * @author Wietse Buseyne
 *
 */
public class LNIACOImpl implements ACOImpl {
	
	public double computeHeuristicValue(UAVNetwork network, Node node, InspectableEdge edge, long currentStep, double beta) {
		return Math.pow((currentStep - edge.getLIT()), beta);
	}
}

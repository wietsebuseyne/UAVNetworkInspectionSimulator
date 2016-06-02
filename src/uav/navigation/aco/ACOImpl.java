package uav.navigation.aco;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;

/**
 * An interface that provides the necessary methods for the ACONavigation to work. 
 * The implementation of the pheromone and heuristic function is done by implementing this interface.
 * 
 * @author Wietse Buseyne
 *
 */
public interface ACOImpl {
	
	/**
	 * Returns the heuristic value for navigating from 'node' over 'edge' in the given network at the specified step with the provided alpha value.
	 * @param network The network in which 
	 * @param node The node from which the navigation is coming
	 * @param edge The edge over which the navigation will happen
	 * @param currentStep The current time of the simulator
	 * @param alpha The alpha value of the ACO algorithm
	 * @return The heuristic value calculated by the algorithm
	 */
	double computeHeuristicValue(UAVNetwork network, Node node, InspectableEdge edge, long currentStep, double alpha);
	
	/**
	 * Returns the pheromone value of the edge with the current alpha at the given step.
	 * @param currentStep The step for which to check the pheromone level at
	 * @param edge The edge to get the pheromone level from
	 * @param beta The value of beta for the algorithm
	 * @return The pheromone level, by default calculated by this formula: (1+(1/(1+edge.pher()))^2)^alpha
	 */
	default double computePheromone(long currentStep, InspectableEdge edge, double alpha) {
		return Math.pow(1+Math.pow(1.0/(edge.getPheromoneLevel()+1), 2), alpha);
	}
	
}

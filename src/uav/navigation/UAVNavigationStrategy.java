package uav.navigation;
import java.util.List;

import network.Inspectable;
import network.Node;
import simulation.UAVNetworkSimulation;
import uav.JobDescription;

/**
 * Provides all methods needed to implement a navigation behaviour class that routes a UAV throug a network.
 * 
 * @author Wietse Buseyne
 *
 */
public interface UAVNavigationStrategy {
		
	/**
	 * Returns a start location for a UAV.
	 * @param sim The simulation for which the start location needs to be retrieved. Information about the network and other UAVs' locations can be retrieved from here.
	 * @return A start location for the UAV represented by a node in the network of the simulation.
	 */
	public Node getStartLocation(UAVNetworkSimulation sim);
	
	/**
	 * Requests the current destination, based on the last time the next destination was requested from this navigation behaviour.
	 * @return The current destination, defined by an edge over which the UAV is moving and the node that he is moving towards.
	 */
	public EdgeNodeLocation getDestination();
	
	/**
	 * Returns the next destination, based on implemented the navigation algorithm, when the UAV is currently at the specified location.
	 * @param sim The simulation for which the decision needs to be made. Information about the network and other UAVs' locations can be retrieved from here.
	 * @param currentLocation The location at which the UAV is currently located. From this node he has the choice between all edges incoming in this node.
	 * @return The next destination defined by an edge over which the UAV should move and the node that he will move towards.
	 */
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation);
	
	/**
	 * Requests this behaviour to inspect the specified locations before doing anything else.
	 * These locations should fit after the current destination of the navigation behaviour or else the UAV using this class will not be able to navigate correctly anymore.
	 * This request for inspection can be ignored if the navigation algorithm thinks this is the best choice. 
	 * If the request is not ignored, the job of this class is set to 'inspection on demand'.
	 * @param nextLocations The locations that should be the next destinations to fulfill the request for inspection.
	 */
	public void inspect(List<EdgeNodeLocation> nextLocations);
	
	/**
	 * Informs the algorithm that an inspection of the specified element has been done or started at the specified time.
	 * The algorithm can take this information into account when making navigation decisions.
	 * @param inspectable The element of which the inspection has finished or started.
	 * @param time The time at which the inspection finished or started.
	 */
	public void inspectionDoneOrStarted(Inspectable inspectable, long time);
	
	/**
	 * Returns the current job of this navigation algorithm.
	 * @See JobDescription for the different possibilities.
	 * @return The current job of the navigation algorithm
	 */
	public JobDescription getJob();

}

package uav.navigation.path;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import network.InspectableEdge;
import network.Node;
import simulation.UAVNetworkSimulation;
import uav.navigation.EdgeNodeLocation;
import uav.navigation.SimpleStartLocationNavigation;

/**
 * An abstract class for path navigation algorithms. These algorithms generate paths from one recharge station to another and pick one path from these based on an algorithm.
 * This abstract class contains a method that generates a list of possible paths between recharge stations 
 * and the mechanism that returns the next destination from the current path or chooses a new path if the path has been completely traversed.
 * 
 * Subclassing classes must override the getBestPath method and implement their algorithm to choose the next path from the given list of paths.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class PathNavigation extends SimpleStartLocationNavigation {

	private Queue<EdgeNodeLocation> path = new LinkedList<EdgeNodeLocation>();
	
	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		if(path.isEmpty())
			path = getBestPath(sim, currentLocation);
		if(path == null)
			throw new IllegalStateException("No next recharge nodes can reached from " + currentLocation + ".\nTry adding more recharge nodes or increasing the UAV specifications.");
		
		destination = path.poll();
		return destination;
	}

	/**
	 * Returns all paths from the given location to other recharge nodes while not including paths that are too long for the UAV to safely traverse without crashing.
	 * @param sim The current simulation 
	 * @param currentLocation The current location to start from
	 * @return A list of all possible paths from the given start location.
	 */
	protected LinkedList<LinkedList<EdgeNodeLocation>> getPaths(UAVNetworkSimulation sim, Node currentLocation) {
		return getPaths(sim, currentLocation, null, 0);
	}
	
	private LinkedList<LinkedList<EdgeNodeLocation>> getPaths(UAVNetworkSimulation sim, Node currentLocation, Node previousLocation, double distance) {
		LinkedList<LinkedList<EdgeNodeLocation>> paths = new LinkedList<LinkedList<EdgeNodeLocation>>();
		
		double maxDistance = sim.getUAVController().getRandomUAV().getMaxFlyingDistance()*0.9;
		for(Object o : sim.network.getEdges(currentLocation, null)) {
			InspectableEdge e = (InspectableEdge) o;
			Node n = (Node) e.getOtherNode(currentLocation);

			double newDistance = distance + e.length();
			if(newDistance > maxDistance)
				break;
			
			if(n.isRechargeNode()) { 
				LinkedList<EdgeNodeLocation> path = new LinkedList<EdgeNodeLocation>();
				path.add(new EdgeNodeLocation(e, n));
				paths.add(path);
			} else {
				for(List<EdgeNodeLocation> otherPath : getPaths(sim, n, currentLocation, newDistance)) {
					LinkedList<EdgeNodeLocation> path = new LinkedList<EdgeNodeLocation>();
					path.add(new EdgeNodeLocation(e, n));
					path.addAll(otherPath);
					paths.add(path);
				}
			}
		}
		return paths;
	}
	
	/**
	 * A method that returns the best 
	 * @param sim
	 * @param currentLocation
	 * @return
	 */
	protected abstract LinkedList<EdgeNodeLocation> getBestPath(UAVNetworkSimulation sim, Node currentLocation);

}

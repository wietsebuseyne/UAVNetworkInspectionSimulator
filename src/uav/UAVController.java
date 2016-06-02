package uav;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import network.Inspectable;
import network.InspectableEdge;
import network.Node;
import sim.util.Double2D;
import simulation.UAVNetworkSimulation;
import uav.navigation.EdgeNodeLocation;
import uav.navigation.UAVNavigationStrategy;
import util.algorithms.dijkstra.Dijkstra;
import util.algorithms.dijkstra.Vertex;

/**
 * A controller class that manages all the UAVs currently in a simulation.
 * Statistics about the UAV flying time can be gathered from this class as well.
 * 
 * @author Wietse Buseyne
 *
 */
public class UAVController {
	
	private UAVNetworkSimulation sim;
	private List<UAV> uavs = new ArrayList<>();
	
	private long stepsInCycle = 1;
	private double cyclePercentageToBeFulfilled = 0;
	
	public UAVController(UAVNetworkSimulation sim){
		this.sim = sim;
	}
	
	/**
	 * Calculates and returns the average standby time of the UAV managed by this class.
	 * @return The average standby time of the UAV managed by this class.
	 */
	public long getAverageStandByTime() {
		long standby = 0;
		for(UAV uav : uavs)
			standby += uav.getStandByTime();
		return standby / uavs.size();
	}
	
	public long getStepsInCycle() {
		return stepsInCycle;
	}
	
	public void setCycle(long stepsInCycle) {
		setCycle(stepsInCycle, 100);
	}

	public void setCycle(long stepsInCycle, double cyclePercentageToBeFulfilled) {
		if(stepsInCycle <= 0)
			throw new IllegalArgumentException("Steps in cycle must be strictly positive.");
		this.stepsInCycle = stepsInCycle;
		setCyclePercentageToBeFulfilled(cyclePercentageToBeFulfilled);
	}
	
	public double getCyclePercentageToBeFulfilled() {
		return cyclePercentageToBeFulfilled;
	}
	
	public long getStepsInNextCycleToBeFulfilled() {
		return (long) (stepsInCycle * getCyclePercentageToBeFulfilled() / 100.0 + 1);
	}
	
	public void setCyclePercentageToBeFulfilled(double percent) {
		if(percent < 0 || percent > 100)
			throw new IllegalArgumentException("The percent must be between 0 and 100 inclusive.");
		this.cyclePercentageToBeFulfilled = percent;
	}
	
	/**
	 * Broadcasts a message from the specified location to all UAVs in reach informing them that an inspection of the specified element happened at the specified step.
	 * @param location The location to broadcast the message from 
	 * @param inspectable the element that was inspected
	 * @param step The time at which the element was inspected
	 */
	public void sendInspectionMessage(Double2D location, Inspectable inspectable, long step) {
		for(UAV uav2 : uavs) {
			Double2D uav2Location = sim.map.getObjectLocation(uav2);
			if(location.distance(uav2Location) < sim.getConfiguration().uavConfiguration.broadcastRadius) {
				uav2.getNavigationBehaviour().inspectionDoneOrStarted(inspectable, step);
			}
		}
	}

	
	/**
	 * Adds a UAV with the given strategy at the given node and puts it under control of this class.
	 * The specifications of the UAV are determined by the configuration of the simulation this class belongs to.
	 * @param startNode The node the UAV start at
	 * @param navBehaviour The navigation strategy of the UAV.
	 */
	public void addUAV(Node startNode, UAVNavigationStrategy navBehaviour) {
		UAV uav = new UAV(startNode, navBehaviour, sim.getConfiguration().uavConfiguration);
		uavs.add(uav);
		sim.map.setObjectLocation(uav, startNode.getLocation());
		sim.schedule.scheduleRepeating(uav);
	}

	/**
	 * Adds a UAV with the given strategy at the given node at the given time and puts it under control of this class.
	 * The UAV will only start navigating at the specified time.
	 * The specifications of the UAV are determined by the configuration of the simulation this class belongs to.
	 * @param startNode The node the UAV start at
	 * @param navBehaviour The navigation strategy of the UAV.
	 * @param time The time the UAV should start navigating at.
	 */
	public void addUAV(Node startNode, UAVNavigationStrategy navBehaviour, double time) {
		UAV uav = new UAV(startNode, navBehaviour, sim.getConfiguration().uavConfiguration);
		uavs.add(uav);
		sim.map.setObjectLocation(uav, startNode.getLocation());
		sim.schedule.scheduleRepeating(time, uav);
	}

	/**
	 * Returns a random UAV under control of this class.
	 * @return A random UAV under control of this class.
	 */
	public UAV getRandomUAV() {
		for(int i = 0; i < uavs.size() && getUAV(i).hasCrashed(); i++)
			if(i == uavs.size()-1 && getUAV(i).hasCrashed())
				throw new IllegalStateException("All UAVs have crashed");
		UAV uav = null;
		do {
			uav = getUAV(sim.random.nextInt(uavs.size()));
		} while(uav.hasCrashed());
		return uav;
	}
	
	public UAV getUAV(int uavNr) {
		return uavs.get(uavNr);
	}
	
	/**
	 * Indicates that an edge requested inspection.
	 * Will instruct the nearest UAV to fly to that edge to inspect it.
	 * @param e The edge for which inspection was requested.
	 */
	public void inspectionRequested(InspectableEdge e) {
		if(!e.isUnderInspection()) {
			Node node1 = (Node) e.getFrom(),
					node2 = (Node) e.getOtherNode(node1);
			sim.resetDijkstra();
			
			Dijkstra dijkstra = new Dijkstra(sim.nodeToVertex(node1));
					
			UAV closestUAV = null;
			double minDist = Double.MAX_VALUE;
			Node node = node1,
					lastNode = node2;
			for(UAV uav : uavs) {
				if(!uav.hasCrashed() && uav.getJobDescription() != JobDescription.INSPECTING_ON_COMMAND) {
					double dist = dijkstra.getDistance(sim.nodeToVertex(uav.getNavigationBehaviour().getDestination().getNode()));
					if(dist < minDist) {
						minDist = dist;
						closestUAV = uav;
					}
				}
			}
			sim.resetDijkstra();
			dijkstra = new Dijkstra(sim.nodeToVertex(node2));
			for(UAV uav : uavs) {
				if(!uav.hasCrashed() && uav.getJobDescription() != JobDescription.INSPECTING_ON_COMMAND) {
					double dist = dijkstra.getDistance(sim.nodeToVertex(uav.getNavigationBehaviour().getDestination().getNode()));
					if(dist < minDist) {
						minDist = dist;
						closestUAV = uav;
						node = node2;
						lastNode = node1;
					}
				}
			}
					
			if(closestUAV != null) {
				LinkedList<EdgeNodeLocation> nextLocations = new LinkedList<EdgeNodeLocation>();
				Node n = closestUAV.getNavigationBehaviour().getDestination().getNode();
				List<Vertex> vertices = dijkstra.getShortestPath(sim.nodeToVertex(n));
				nextLocations.addFirst(new EdgeNodeLocation((InspectableEdge)sim.network.getEdge(node, lastNode), node));
				for(int i = 0; i < vertices.size()-1; i++) {
					Vertex v1 = vertices.get(i),
							v2 = vertices.get(i+1);
					nextLocations.addFirst(new EdgeNodeLocation((InspectableEdge)sim.network.getEdge(v2.mapsTo, v1.mapsTo), v1.mapsTo));
				}
				closestUAV.inspect(nextLocations);
			}
		}
	}

	/**
	 * Indicates that a node requested inspection.
	 * Will instruct the nearest UAV to fly to that node to inspect it.
	 * @param node The node for which inspection was requested.
	 */
	public void inspectionRequested(Node node) {
		if(node == null)
			throw new IllegalArgumentException("The node cannot be null");
		
		sim.resetDijkstra();
		Dijkstra dijkstra = new Dijkstra(sim.nodeToVertex(node));
				
		UAV closestUAV = null;
		double minDist = Double.MAX_VALUE;
		for(UAV uav : uavs) {
			if(!uav.hasCrashed() && uav.getJobDescription() != JobDescription.INSPECTING_ON_COMMAND) {
				double dist = dijkstra.getDistance(sim.nodeToVertex(uav.getNavigationBehaviour().getDestination().getNode()));
				if(dist < minDist) {
					minDist = dist;
					closestUAV = uav;
				}
			}
		}
		
		if(closestUAV != null) {
			LinkedList<EdgeNodeLocation> nextLocations = new LinkedList<EdgeNodeLocation>();
			Node n = closestUAV.getNavigationBehaviour().getDestination().getNode();
			List<Vertex> vertices = dijkstra.getShortestPath(sim.nodeToVertex(n));
			for(int i = 0; i < vertices.size()-1; i++) {
				Vertex v1 = vertices.get(i),
						v2 = vertices.get(i+1);
				nextLocations.addFirst(new EdgeNodeLocation((InspectableEdge)sim.network.getEdge(v2.mapsTo, v1.mapsTo), v1.mapsTo));
			}
			if(!nextLocations.isEmpty())
				closestUAV.inspect(nextLocations);
		}
	}

	/**
	 * Removes all UAVs under control of this class.
	 */
	public void clear() {
		uavs.clear();
	}

	public int getNbOfUAVs() {
		return uavs.size();
	}

	public List<UAV> getUAVs() {
		return Collections.unmodifiableList(uavs);
	}
	
}

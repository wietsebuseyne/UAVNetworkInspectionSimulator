package uav.navigation;
import java.util.ArrayList;
import java.util.List;

import network.InspectableEdge;
import network.Node;
import simulation.UAVNetworkSimulation;

/**
 * A navigation strategy that keeps flying over the specified cycle.
 * 
 * @author Wietse Buseyne
 *
 */
public class CycleNavigation extends SimpleStartLocationNavigation {
	
	private List<Integer> nodes;
	private int currentPosition = 0;
	private long stepsInCycle = -1;
	private EdgeNodeLocation destination;
	
	public CycleNavigation(List<Integer> nodes) {//Node startNode, List<InspectableEdge> edges) {
		if(nodes == null || nodes.size() < 2)
			throw new IllegalArgumentException("The list of nodes must contain at least two integers");
		this.nodes = new ArrayList<Integer>(nodes);
	}

	@Override
	public Node getStartLocation(UAVNetworkSimulation sim) {
		return (Node) sim.network.allNodes.get(nodes.get(0));
	}

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		Node n = (Node) sim.network.getNode(nodes.get(currentPosition++));
		currentPosition = currentPosition % nodes.size();
		if(stepsInCycle == -1 && currentPosition == 0)
			setStepsInCycle(sim.schedule.getSteps());
		Node n2 = sim.network.getNode(nodes.get(currentPosition));

		if(sim.network.getEdge(n, n2) == null)
			throw new IllegalStateException("The cycle is not a valid cycle: no edge between " + n + " and  " + n2);
		destination = new EdgeNodeLocation((InspectableEdge) sim.network.getEdge(n, n2), n2);
		return destination;
	}

	@Override
	public EdgeNodeLocation getDestination() {
		return destination;
	}
	
	/**
	 * Returns the number of steps that are needed to fly over the cycle that is used by this navigation strategy.
	 * @return The number of steps that are needed to fly over the cycle that is used by this navigation strategy.
	 */
	public long getStepsInCycle() {
		return stepsInCycle;
	}

	private void setStepsInCycle(long stepsInCycle) {
		this.stepsInCycle = stepsInCycle;
	}

}

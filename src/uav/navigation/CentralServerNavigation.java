package uav.navigation;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uav.JobDescription;

/**
 * Provides an abstract class for all navigation strategies which use a central server for communication.
 * If a request for inspection of a certain edge or node comes in, the navigation strategy keep the flying instructions in a seperate queue.
 * The implementating strategy can choose whether or not to follow the flying instructions.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class CentralServerNavigation extends SimpleStartLocationNavigation {

	protected Queue<EdgeNodeLocation> inspectionQueue = new LinkedList<EdgeNodeLocation>();
	
	@Override
	public void inspect(List<EdgeNodeLocation> nextLocations) {
		if(!inspectionQueue.isEmpty())
			throw new IllegalStateException("Queue not empty");

		if(!nextLocations.get(0).getEdge().getFrom().equals(destination.getNode())
				&& !nextLocations.get(0).getEdge().getTo().equals(destination.getNode()))
			throw new IllegalStateException("First location does not start at the destination of the UAV");
		
		inspectionQueue.addAll(nextLocations);
	}

	@Override
	public JobDescription getJob() {
		return inspectionQueue.isEmpty() ? JobDescription.MONITORING : JobDescription.INSPECTING_ON_COMMAND;
	}

}

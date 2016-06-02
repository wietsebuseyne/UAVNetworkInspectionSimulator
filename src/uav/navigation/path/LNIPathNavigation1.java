package uav.navigation.path;

import java.util.LinkedList;
import java.util.Queue;

import network.Node;
import simulation.UAVNetworkSimulation;
import uav.navigation.EdgeNodeLocation;

public class LNIPathNavigation1 extends PathNavigation {
	
	private Queue<EdgeNodeLocation> path = new LinkedList<EdgeNodeLocation>();

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		if(path.isEmpty())
			path = getBestPath(sim, currentLocation);
		if(path == null)
			return new EdgeNodeLocation(null, currentLocation);
		
		destination = path.poll();
		return destination;
	}
	
	protected LinkedList<EdgeNodeLocation> getBestPath(UAVNetworkSimulation sim, Node currentLocation) {
		LinkedList<LinkedList<EdgeNodeLocation>> paths = getPaths(sim, currentLocation);
		if(paths.isEmpty())
			return null;
		LinkedList<EdgeNodeLocation> best = paths.getFirst();
		double min = Double.MAX_VALUE;
		for(LinkedList<EdgeNodeLocation> path : paths) {
			double m = Double.MAX_VALUE;
			for(EdgeNodeLocation enl : path) {
				double lit = enl.getEdge().getLastInspectionTime();
				if(lit < m)
					m = lit;
			}
			if(m < min) {
				min = m;
				best = path;
			}
		}
		return best;
	}

}

package uav.navigation.path;

import java.util.LinkedList;

import network.Node;
import simulation.UAVNetworkSimulation;
import uav.navigation.EdgeNodeLocation;

/**
 * PathNavigation strategies generate paths between two recharge stations and choose what they think is the best path.
 * This simple path navigation strategy will choose the path where the value of all last inspection times summed up is the lowest.
 * If a path contains an edge that is already being inspected by another UAV, this path will not be chosen.
 * If all paths contain edges being inspected by other UAVs, the path with lowest sum will be chosen.
 * 
 * @author Wietse Buseyne
 *
 */
public class CombinedLNIPathNavigation extends PathNavigation {
		
	protected LinkedList<EdgeNodeLocation> getBestPath(UAVNetworkSimulation sim, Node currentLocation) {
		LinkedList<LinkedList<EdgeNodeLocation>> paths = getPaths(sim, currentLocation);
		if(paths.isEmpty())
			return null;
		LinkedList<EdgeNodeLocation> best = paths.getFirst();
		double min = Double.MAX_VALUE;
		for(LinkedList<EdgeNodeLocation> path : paths) {
			double m = 0;
			for(EdgeNodeLocation enl : path) {
				if(enl.getEdge().getLastInspectionStartTime() != 0) {
					m = Double.MAX_VALUE;
					break;
				}
				m += Math.pow(enl.getEdge().getLastInspectionTime(), 2);
			}
			if(m < Double.MAX_VALUE && m / path.size() < min) {
				min = m / path.size();
				best = path;
			}
		}
		
		if(min == Double.MAX_VALUE) {
			for(LinkedList<EdgeNodeLocation> path : paths) {
				double m = 0;
				for(EdgeNodeLocation enl : path) {
					m += enl.getEdge().getLastInspectionTime();
				}
				if(m < min) {
					min = m;
					best = path;
				}
			}
		}
		
		return best;
	}

}

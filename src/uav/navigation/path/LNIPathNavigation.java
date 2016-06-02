package uav.navigation.path;

import java.util.LinkedList;

import network.Node;
import simulation.UAVNetworkSimulation;
import uav.navigation.EdgeNodeLocation;

/**
 * PathNavigation strategies generate paths between two recharge stations and choose what they think is the best path.
 * This simple path navigation strategy will choose the path that contains the longest not inspected edge amongst all edges in any of the paths.
 * If a path contains an edge that is already being inspected by another UAV, this path will not be chosen.
 * If all paths contain edges being inspected by other UAVs, the path with the edge that has not been inspected the longest will still be chosen.
 * 
 * @author Wietse Buseyne
 *
 */
public class LNIPathNavigation extends PathNavigation {
	
	protected LinkedList<EdgeNodeLocation> getBestPath(UAVNetworkSimulation sim, Node currentLocation) {
		LinkedList<LinkedList<EdgeNodeLocation>> paths = getPaths(sim, currentLocation);
		if(paths.isEmpty())
			return null;
		LinkedList<EdgeNodeLocation> best = paths.getFirst();
		double min = Double.MAX_VALUE;
		for(LinkedList<EdgeNodeLocation> path : paths) {
			double m = Double.MAX_VALUE;
			for(EdgeNodeLocation enl : path) {
				if(enl.getEdge().getLastInspectionStartTime() != 0) {
					m = Double.MAX_VALUE;
					break;
				}
				double lit = enl.getEdge().getLastInspectionTime();
				if(lit < m)
					m = lit;
			}
			if(m < min) {
				min = m;
				best = path;
			}
		}
		
		if(min == Double.MAX_VALUE) {
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
		}
		
		return best;
	}

}

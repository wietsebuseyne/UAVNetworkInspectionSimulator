package util.parsers.network;

import java.util.ArrayList;
import java.util.List;

import network.Node;

/**
 * A class that represents a network in an way so that it can be easily written to JSON. 
 * @author Wietse Buseyne
 *
 */
public class JsonGraph {
	
	public List<Node> nodes = new ArrayList<Node>();
	public List<JsonEdge> edges = new ArrayList<JsonEdge>();
	
	public long getDistance() {
		long distance = 0;
		for(JsonEdge edge : edges)
			distance += nodes.get(edge.source).getLocation().distance(nodes.get(edge.target).getLocation());
		return distance;
	}

}

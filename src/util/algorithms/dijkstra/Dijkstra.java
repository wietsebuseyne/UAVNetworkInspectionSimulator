package util.algorithms.dijkstra;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A class for finding the shortest path through a graph using the Dijkstra's shortest path algorithm. 
 * @author Wietse Buseyne
 */
public class Dijkstra {
	
	/**
	 * Searches for the shortest path from the given start vertex to all other vertices in the graph.
	 * If two paths have the same cost, the algorithm will prefer the shortest path.
	 * @param start The vertex to start the search from.
	 */
	public Dijkstra(Vertex start) {
		start.distance = 0;
		start.previous = null;
		
		PriorityQueue<Vertex> unvisited = new PriorityQueue<>();
		unvisited.add(start);
		
		Vertex current;
		while (!unvisited.isEmpty()) {
			//Explore all paths from start and don't give end to stop at, so shorter path with same cost can be found
			current = unvisited.poll();
			
			for(Edge e : current.edges) {
				Vertex v = e.getTarget();
				if(current.distance + e.getWeight() < v.distance) {
					unvisited.remove(v);
					v.distance = current.distance + e.getWeight();
					v.previous = current;
					unvisited.add(v);
				} 
			}
		}
	}
	
	/**
	 * Returns the shortest path from the start of this object to the given end vertex.
	 * @param end The vertex to navigate to.
	 * @return The shortest path from the start vertex to the given end vertex.
	 */
	public List<Vertex> getShortestPath(Vertex end) {
		LinkedList<Vertex> path = new LinkedList<>();

		for(Vertex v = end; v != null; v = v.previous) {
			path.addFirst(v);
		}
		
		return path;
	}
	
	/**
	 * Returns the length of the shortest path from the start of this object to the given end vertex.
	 * @param end The vertex to navigate to.
	 * @return The length of the shortest path from the start vertex to the given end vertex.
	 */
	public double getDistance(Vertex end) {
		return end.distance;
	}
	
	/**
	 * Returns the positions of the vertices in the shortest path from the start to the given end vertex.
	 * @param end The vertex to navigate to.
	 * @return The positions of the vertices in the shortest path from the start to the given end vertex.
	 */
	public List<Position> positions(Vertex end) {
		List<Position> positions = new ArrayList<>();
		for(Vertex v : getShortestPath(end))
			positions.add(v.position);
		return positions;
	}
	
}

package util.algorithms.dijkstra;

import java.util.LinkedList;
import java.util.List;

import network.Node;

/**
 * Represents a vertex inside a graph.
 * @author Wietse Buseyne
 * @version 1.0
 *
 */
public class Vertex implements Comparable<Vertex>{
	
	public double distance = Double.MAX_VALUE;
	public Position position;
	public List<Edge> edges;
	public Vertex previous = null;
	public Node mapsTo = null;
	
	public Vertex(double x, double y, Node mapsTo) {
		this(new Position(x, y), mapsTo);
	}
	
	public Vertex(Position position, Node mapsTo) {
		this.position = position;
		edges = new LinkedList<>();
		this.mapsTo = mapsTo;
	}

	/**
	 * Adds an edge to this vertex.
	 * @param edge The edge to add to the list of edges of this vertex.
	 */
	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	/**
	 * Adds an edge to this vertex and the vertex its pointing to.
	 * @param edge The edge to add to the list of edges of this vertex.
	 */
	public void addBidirectionalEdge(Edge edge) {
		addEdge(edge);
		edge.getTarget().edges.add(new Edge(this, edge.getWeight()));
	}
	
	public void addBidirectionalEdge(Vertex to, double weight) {
		addEdge(new Edge(to, weight));
		to.addEdge(new Edge(this, weight));
	}
	
	public void addBidirectionalEdge(Vertex to) {
		addEdge(new Edge(to, position.distanceTo(to.getPosition())));
		to.addEdge(new Edge(this, position.distanceTo(to.getPosition())));
	}
	
	/**
	 * Returns the list of edges of this vertex.
	 * @return The list of edges of this vertex.
	 */
	public List<Edge> getEdges() { 
		return edges;
	}
	
	/**
	 * Returns the length of the path starting from this vertex to the start (previous == null)
	 * @return The length of the path starting from this vertex to the start.
	 */
	public int getLength() {
		int i = 1;
		Vertex v = this;
		while(v.previous != null) {
			v = v.previous;
			i++;
		}
		return i;
	}

	@Override
	public int compareTo(Vertex other) {
		return Double.compare(distance, other.distance);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public boolean isOdd() {
		return edges.size() % 2 != 0;
	}

	@Override
	public String toString() {
		return "(" +  position.getX() + ", " + position.getY() + ")";
	}

}

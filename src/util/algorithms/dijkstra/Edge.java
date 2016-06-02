package util.algorithms.dijkstra;

/**
 * Represents an edge in a graph. The edge connects two vertices: the source and the target.
 * @author Wietse Buseyne
 *
 */
public class Edge {
	
	private Vertex target;
	private double weight;

	/**
	 * Initializes this object.
	 * @param target The vertex where this edge points to.
	 * @param weight The weight of this edge
	 */
	public Edge(Vertex target, double weight) {
		this.target = target;
		this.weight = weight;
	}

	/**
	 * Returns the vertex where this edge points to.
	 * @return The vertex where this edge points to.
	 */
	public Vertex getTarget() {
		return target;
	}

	/**
	 * Sets the target of this edge to the given vertex.
	 * @param target The new target of this edge.
	 */
	public void setTarget(Vertex target) {
		this.target = target;
	}

	/**
	 * Returns the weight of this vertex.
	 * @return The weight of this vertex.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Sets the weight of this edge to the given weight.
	 * @param weight The new weight of this edge.
	 */
	public void setWeight(long weight) {
		this.weight = weight;
	}

}

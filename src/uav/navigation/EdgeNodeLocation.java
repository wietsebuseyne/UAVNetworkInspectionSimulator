package uav.navigation;

import network.InspectableEdge;
import network.Node;

/**
 * A class used to indicate to which node one will navigation and over which edge this will be achieved.
 * The edge and the node should always be connected, except when the edge is null (this means no next location, simply staying at node currently).
 * 
 * @author Wietse Buseyne
 *
 */
public class EdgeNodeLocation {
	
	private InspectableEdge edge;
	private Node node;
	
	public EdgeNodeLocation(InspectableEdge edge, Node node) {
		this.edge = edge;
		this.node = node;
		if(edge != null && !edge.getFrom().equals(node) && !edge.getTo().equals(node))
			throw new IllegalArgumentException("The specified node should be one of the two nodes of the specified edge, or the edge must be null");
	}

	public InspectableEdge getEdge() {
		return edge;
	}

	public void setEdge(InspectableEdge edge) {
		this.edge = edge;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
	public String toString() {
		return edge.getOtherNode(node).toString() + " - " + node.toString();
	}

}

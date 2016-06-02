package network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sim.field.network.Edge;
import sim.field.network.Network;

/**
 * Represents a network of inspectable edges and nodes.
 * The network has specific intervals at which edges and nodes need to be inspected.
 * @author Wietse Buseyne
 *
 */
public class UAVNetwork extends Network {
	
	private static final long serialVersionUID = 1L;
	private List<InspectableEdge> edges = new ArrayList<InspectableEdge>();
	private long edgeInspectionInterval = 0, nodeInspectionInterval = 0;
	
	public UAVNetwork() {
		super();
	}
	
	public UAVNetwork(boolean directed) {
		super(directed);
	}
	
	public UAVNetwork(Network other) {
		super(other);
	}

	public long getEdgeInspectionInterval() {
		return edgeInspectionInterval;
	}

	public void setEdgeInspectionInterval(long edgeInspectionInterval) {
		if(edgeInspectionInterval < 0)
			throw new IllegalArgumentException("The edge inspection interval must be positive");
		this.edgeInspectionInterval = edgeInspectionInterval;
	}

	public long getNodeInspectionInterval() {
		return nodeInspectionInterval;
	}

	public void setNodeInspectionInterval(long nodeInspectionInterval) {
		if(edgeInspectionInterval < 0)
			throw new IllegalArgumentException("The node inspection interval must be positive");
		this.nodeInspectionInterval = nodeInspectionInterval;
	}

	public Node getNode(int index) {
		return (Node)allNodes.get(index);
	}

	public Node getRandomNode() {
		Random rnd = new Random();
		return getNode(rnd.nextInt(allNodes.size()));
	}

	public List<Inspectable> getInspectables() {
		List<Inspectable> inspectables = new ArrayList<Inspectable>();
		
		for(Object o : allNodes) {
			Node n = (Node) o;
			inspectables.add(n);
			for(Object o2 : getEdges(n, null)) {
				inspectables.add((InspectableEdge)o2);
			}
		}
		return inspectables;
	}

	public int getNbNodes() {
		return allNodes.size();
	}
	
	@Override
	@Deprecated
	public void addEdge(Edge edge) {
		if(!(edge instanceof InspectableEdge))
			throw new IllegalArgumentException("The edge must be an InspectableEdge instance");
		super.addEdge(edge);
		edges.add((InspectableEdge)edge);
	}
	@Override
	public void addEdge(Object from, Object to, Object info) {
		addEdge(from, to, 1);
	}
	
	public void addEdge(Object from, Object to, double riskLevelMultiplier) {
		InspectableEdge edge = new InspectableEdge(from, to, riskLevelMultiplier);
		super.addEdge(edge);
		edges.add((InspectableEdge)edge);
	}
	
	public List<InspectableEdge> getEdges() {
		return edges;
	}
	
	public int getNbEdges() {
		return edges.size();
	}

	public InspectableEdge getEdge(int i) {
		return edges.get(i);
	}
	
	public InspectableEdge getRandomEdge() {
		Random rnd = new Random();
		return getEdge(rnd.nextInt(edges.size()));
	}

	public List<Node> getNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for(Object o : getAllNodes())
			nodes.add((Node)o);
		return nodes;
	}

}

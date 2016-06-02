package util.parsers.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Double2D;
import sim.util.MutableDouble2D;
import util.algorithms.dijkstra.Vertex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class to parse and write JSON network files for use in the simulator. 
 * 
 * @author Wietse Buseyne
 *
 */
public class NetworkParser {
	
	protected Gson gson;
	protected double totalDistance, distanceBetweenRechargeNodes;
	protected String filename;
	protected double maxX, maxY;
	
	protected List<Vertex> vertices = new ArrayList<Vertex>();
	protected Map<Node, Vertex> vertexMap = new HashMap<Node, Vertex>();
	protected UAVNetwork network;
	private boolean allRNs = false;
	
	public NetworkParser(String filename, double distanceBetweenRechargeNodes, boolean allRNs) {
		this.filename = filename;
		this.distanceBetweenRechargeNodes = distanceBetweenRechargeNodes;
		GsonBuilder gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
		this.allRNs = allRNs;
	}
	
	public void parse() throws IOException {
		Reader br = new BufferedReader(new FileReader(filename));
		graphDataToNetwork(gson.fromJson(br, JsonGraph.class), allRNs);
	}
	
	public UAVNetwork getNetwork() {
		return network;
	}
	
	public Vertex getVertex(Node node) {
		return vertexMap.get(node);
	}
	
	public double getMaxX() {
		return maxX;
	}
	
	public double getMaxY() {
		return maxY;
	}
	
	public double getTotalDistance() {
		return totalDistance;
	}
	
	public void writeToFile(UAVNetwork network) {
		JsonGraph graph = new JsonGraph();
		List<Node> nodes = network.getNodes();
		for(Object o : network.allNodes)
			graph.nodes.add((Node)o);
		for(InspectableEdge e : network.getEdges()) {
			graph.edges.add(new JsonEdge(nodes.indexOf(e.getFrom()), nodes.indexOf(e.getTo()), e.getRiskLevelMultiplier()));
		}
		try (Writer writer = new FileWriter(filename)){  
			writer.write(gson.toJson(graph));  
			writer.close();
		} catch (IOException e) {} 
	}
		
	protected void graphDataToNetwork(JsonGraph graphData, boolean allRNs) {
		vertexMap.clear();
		network = new UAVNetwork(false);
		List<Node> nodes = new ArrayList<Node>();
		Map<Node, Node> realNodeMap = new HashMap<Node, Node>();
		for(Node n : graphData.nodes) {
			
			if(n.getLocation().getX() > maxX)
				maxX = n.getLocation().getX();
			if(n.getLocation().getY() > maxY)
				maxY = n.getLocation().getY();
			boolean add = true;
			Node mappedOnto = null;
			for(Node n2 : nodes)
				if(n.distance(n2) < 5) {
					add = false;
					mappedOnto = n2;
					break;
				}
			if(add) {				
				//MASON Graph
				n.initialize();
				if(allRNs)
					n.setRechargeNode(true);
				//n.setLocation(new Double2D(n.getLocation().x*3, n.getLocation().y*3));
				network.addNode(n);
				nodes.add(n);
				realNodeMap.put(n, n);
				
				//Dijkstra Graph
				Vertex v = new Vertex(realNodeMap.get(n).getLocation().x, realNodeMap.get(n).getLocation().y, realNodeMap.get(n));
				vertices.add(v);
				vertexMap.put(realNodeMap.get(n), v);
			} else
				realNodeMap.put(n, mappedOnto);
			

		}
		totalDistance = 0;
		for(JsonEdge e : graphData.edges) {
			/*Node startNode = (Node)network.allNodes.get(e.source);
			Node endNode = (Node)network.allNodes.get(e.target);*/
			
			//MASON Graph			
			Node startNode = realNodeMap.get(graphData.nodes.get(e.source));
			Node endNode = realNodeMap.get(graphData.nodes.get(e.target));
			if(!startNode.equals(endNode) && network.getEdge(startNode, endNode) == null) {
				
				Double2D n1 = startNode.getLocation();
				Double2D n2 = endNode.getLocation();
				
				double distance = n1.distance(n2);
				totalDistance += distance;
				int nbOfIntermediateNodes = (int) Math.ceil(distance / distanceBetweenRechargeNodes);
				double realDistanceBetweenNodes = distance / nbOfIntermediateNodes;
				nbOfIntermediateNodes--;
				
				MutableDouble2D start = new MutableDouble2D(n1);
				Node end = null;
				
				List<Node> nodesToAdd = new ArrayList<Node>();
				nodesToAdd.add(startNode);
				
				if(allRNs) //Only add intermediate recharge nodes if requested
					for(int i = 0; i < nbOfIntermediateNodes; i++) {
						MutableDouble2D sumForces = new MutableDouble2D();
						sumForces.addIn(new Double2D((n2.x-start.x), (n2.y-start.y)));
						sumForces.normalize();
						sumForces.setX(sumForces.getX()*realDistanceBetweenNodes);
						sumForces.setY(sumForces.getY()*realDistanceBetweenNodes);
						sumForces.addIn(start);
						
						if(allRNs)
							end = new Node(new Double2D(sumForces), true, 0);
						else
							end = new Node(new Double2D(sumForces), 0);
						end.setInspectionTime(0);
						network.addNode(end);
						nodesToAdd.add(end);
						start = sumForces;
						startNode = end;
						
						//Dijkstra Graph
						Vertex v = new Vertex(sumForces.getX(), sumForces.getY(), end);
						vertices.add(v);
						vertexMap.put(end, v);
					}
				nodesToAdd.add(endNode);
				for(int i = 0; i < nodesToAdd.size()-1; i++) {
					network.addEdge(nodesToAdd.get(i), nodesToAdd.get(i+1), null);
					//Dijkstra Graph
					vertexMap.get(nodesToAdd.get(i)).addBidirectionalEdge(
							vertexMap.get(nodesToAdd.get(i+1)));
				}
			}
		}
		
		//Tests for Dijkstra Graph
		for(Object o : network.allNodes) {
			Node n = (Node) o;
			if(vertices.size() != network.allNodes.size()) {
				System.out.println("Error: Not the same amount of nodes (" + network.allNodes.size() + ") and vertices (" + vertices.size() + ")");
				System.exit(-1);
			}
			if(!vertexMap.containsKey(n)) {
				System.out.println("Error: vertexMap does not contain " + n);
				System.exit(-1);
			}
			if(network.getEdges(n, null).size() != vertexMap.get(n).getEdges().size()) {
				System.out.println("Error: node " + n + " has " + network.getEdges(n, null).size() + " edges, its vertex has " + vertexMap.get(n).getEdges().size());
				System.exit(-1);
			}
		}
		
		//Remove nodes that are not connected to main graph
		
		for(Node currentNode : nodes) {
			if(nbOfNodes(new Network(network), currentNode) < network.allNodes.size() / 2)
				network.removeNode(currentNode);
		}
	}
	
	public void resetDijkstra() {
		for(Vertex v : vertices) {
			v.previous = null;
			v.distance = Double.MAX_VALUE;
		}
	}
	
	private int nbOfNodes(Network network, Node n) {
		int nbOfNodes = 1 + network.getEdges(n, null).size();
		for(Object o : network.getEdges(n, null)) {
			Edge edge = (Edge) o;
			Node other = (Node) edge.getOtherNode(n);
			network.removeEdge(edge);
			nbOfNodes += nbOfNodes(network, other);
		}
		return nbOfNodes;
	}

}

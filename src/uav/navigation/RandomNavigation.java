package uav.navigation;
import network.InspectableEdge;
import network.Node;
import simulation.UAVNetworkSimulation;

/**
 * A navigation strategy that chooses randomly which edge to navigate over next.
 * Mainly provided for comparison reasons.
 * 
 * @author Wietse Buseyne
 *
 */
public class RandomNavigation extends SimpleStartLocationNavigation {

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation uavNetworkSimulation, Node currentLocation) {
		/*for(Object o : uavNetworkSimulation.network.getEdges(currentLocation, null)) {
			InspectableEdge edge =  (InspectableEdge)o;
			if(edge.needsInspection()){
				return new EdgeNodeLocation(edge, edge.getTo().equals(currentLocation) ? (Node)edge.getFrom() : (Node)edge.getTo());
			}
		}*/
		InspectableEdge edge =  (InspectableEdge)uavNetworkSimulation.network.getEdges(currentLocation, null).get(
						uavNetworkSimulation.random.nextInt(uavNetworkSimulation.network.getEdges(currentLocation, null).size()));
		destination = new EdgeNodeLocation(edge, edge.getOtherNode(currentLocation));
		return destination;
	}

}

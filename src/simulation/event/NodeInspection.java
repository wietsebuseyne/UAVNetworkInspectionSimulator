package simulation.event;

import network.Node;
import sim.engine.SimState;
import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;

/**
 * A steppable class that will request inspection on a random node of the network.
 * The UAVController will be notified so that the necessary actions can be taken.
 * If the node is already in need of inspection, nothing will happen.
 * 
 * @author Wietse Buseyne
 *
 */
public class NodeInspection implements Steppable {
	
	private static final long serialVersionUID = 1L;
	private Node node;
	
	public NodeInspection() {}
	
	public NodeInspection(Node node) {
		this.node = node;
	}

	@Override
	public void step(SimState simState) {
		UAVNetworkSimulation sim = (UAVNetworkSimulation) simState;
		Node n = null;
		if(node != null) {
			n = node;
		} else {
			n = sim.network.getRandomNode();
			/*boolean available = false;
			for(int i = 0; i < sim.network.getNbNodes() && !available; i++) {
				if(!sim.network.getNode(i).needsInspection())
					available = true;
			}
			if(available) {
				Node r = sim.network.getRandomNode();
				while(r.needsInspection()) {
					r = sim.network.getRandomNode();
				}
				n = r;
			}*/
		}
		if(n != null && !n.needsInspection()) {
			n.inspectionNeeded(sim.schedule);
			sim.getUAVController().inspectionRequested(n);
		}
	}

}

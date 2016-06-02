package simulation.event;

import network.InspectableEdge;
import sim.engine.SimState;
import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;

/**
 * A steppable class that will request inspection on a random edge of the network.
 * The UAVController will be notified so that the necessary actions can be taken.
 * If the edge is already in need of inspection, nothing will happen.
 * 
 * @author Wietse Buseyne
 *
 */
public class EdgeInspection implements Steppable {
	
	private static final long serialVersionUID = 1L;
	private InspectableEdge edge;
	
	public EdgeInspection() {}
	
	public EdgeInspection(InspectableEdge edge) {
		this.edge = edge;
	}

	@Override
	public void step(SimState simState) {
		UAVNetworkSimulation sim = (UAVNetworkSimulation) simState;
		InspectableEdge e = null;
		if(edge != null) {
			e = edge;
		} else {
			e = sim.network.getRandomEdge();
			/*boolean available = false;
			for(int i = 0; i < sim.network.getNbEdges() && !available; i++) {
				if(!sim.network.getEdge(i).needsInspection())
					available = true;
			}
			if(available) {
				InspectableEdge r = sim.network.getRandomEdge();
				while(r.needsInspection()) {
					r = sim.network.getRandomEdge();
				}
				e = r;
			}*/
		}
		if(e != null && !e.needsInspection()) {
			e.inspectionNeeded(sim.schedule);
			sim.getUAVController().inspectionRequested(e);
		}
	}

}

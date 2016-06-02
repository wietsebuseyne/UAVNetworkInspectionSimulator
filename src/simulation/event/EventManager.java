package simulation.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.UAVNetworkSimulation;
import simulation.event.generator.EventGenerator;

/**
 * A manager that holds event generators and can schedule all their events easily.
 * 
 * @author Wietse Buseyne
 *
 */
public class EventManager {
	
	private List<EventGenerator> generators = new ArrayList<EventGenerator>();
	
	public void scheduleEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		for(EventGenerator eg : generators) {
			for(Event e : eg.getEvents(sim, firstStep, lastStep)) {
				e.scheduleIn(sim.schedule);
			}
		}
	}
	
	public void addEventGenerator(EventGenerator eventGenerator) {
		if(eventGenerator == null)
			throw new IllegalArgumentException("EventGenerator cannot be null");
		generators.add(eventGenerator);
	}

	public boolean addAll(Collection<? extends EventGenerator> c) {
		return generators.addAll(c);
	}

}

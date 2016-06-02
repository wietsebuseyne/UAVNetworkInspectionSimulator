package simulation.event.generator;

import java.util.ArrayList;
import java.util.List;

import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;
import simulation.event.Event;

/**
 * Generates events based on static times provided by the user.
 * Will always return the same set of events.
 * 
 * @author Wietse Buseyne
 *
 */
public class StaticEventGenerator extends EventGenerator {
	
	private long[] times;
	private List<Event> events = new ArrayList<Event>();

	public StaticEventGenerator(Class<? extends Steppable> steppableClass, long... times) {
		super(steppableClass);
		this.times = times;
	}

	@Override
	public List<Event> getEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		if(events.isEmpty())
			createEvents(sim, firstStep, lastStep);
		return events;
	}

	@Override
	protected void createEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		for(int i = 0; i < times.length && times[i] <= lastStep ; i++) {
			if(times[i] > firstStep) {
				events.add(createEvent(times[i]));
			}
		}
	}

}

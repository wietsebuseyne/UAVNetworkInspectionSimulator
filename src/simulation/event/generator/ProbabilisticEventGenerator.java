package simulation.event.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.engine.Steppable;
import simulation.event.Event;
import ec.util.MersenneTwisterFast;

/**
 * An abstract class to generate events in a probabilistic way, using a likelihood and an interval between possible times an event can happen.
 * The event generation is based on the number of elements. If later on events are requested for the same number of elements, the same set of events will be returned
 * 
 * The getEvents and createEvents methods are provided for subclasses so they can call these methods with the correct number of elements for their specific need.
 * @author Wietse Buseyne
 *
 */
public abstract class ProbabilisticEventGenerator extends EventGenerator {

	public int intervalBetween;
	public double likelihood ;
	protected Map<Integer, List<Event>> events = new HashMap<Integer, List<Event>>();

	public ProbabilisticEventGenerator(Class<? extends Steppable> steppableClass, int intervalBetween, double likelihood) {
		super(steppableClass);
		this.intervalBetween = intervalBetween;
		this.likelihood = likelihood;
	}

	protected List<Event> getEvents(int nbOfElements, long firstStep, long lastStep) {
		if(events.get(nbOfElements) == null)
			createEvents(nbOfElements, firstStep, lastStep);
		return events.get(nbOfElements);
	}

	protected void createEvents(int nbOfElements, long firstStep, long lastStep) {
		List<Event> events = new ArrayList<Event>();
		MersenneTwisterFast random = new MersenneTwisterFast();
		
		for(long i = firstStep; i <= lastStep; i += intervalBetween) {
			for(int j = 0; j < nbOfElements; j++)
				if(likelihood > random.nextDouble()) {
					events.add(createEvent(i));
				}
		}
		this.events.put(nbOfElements, events);
	}

}

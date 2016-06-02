package simulation.event.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;
import simulation.event.Event;

/**
 * An EventGenerator can be used to generate events of a specified type. 
 * Each event generator will have its own generation pattern that defines how the events are generated.
 * @author Wietse Buseyne
 *
 */
public abstract class EventGenerator {
	
	protected Class<? extends Steppable> steppableClass;
	
	public EventGenerator(Class<? extends Steppable> steppableClass) {
		this.steppableClass = steppableClass;
	}
	
	public abstract List<Event> getEvents(UAVNetworkSimulation sim, long firstStep, long lastStep);
	
	protected abstract void createEvents(UAVNetworkSimulation sim, long firstStep, long lastStep);
	
	protected Event createEvent(long time) {
		try {
			return Event.class.getConstructor(Steppable.class, long.class).newInstance(steppableClass.getConstructor().newInstance(), time);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("The steppableClass must have a no-args constructor to use this function");
		}
	}
	
}

package simulation.event.generator;

import java.lang.reflect.InvocationTargetException;

import sim.engine.Steppable;
import simulation.event.Event;
import simulation.event.Failure;
import ec.util.MersenneTwisterFast;

/**
 * A static event generator for UAV failures. Will generate UAV failure events at the specified times with random revive times between the minimum and maximum specified.
 * Will always return the same set of events.
 * 
 * @author Wietse Buseyne
 *
 */
public class StaticFailureGenerator extends StaticEventGenerator {

	public long minTimeToRevive, maxTimeToRevive;

	public StaticFailureGenerator(long[] times, long minTimeToRevive, long maxTimeToRevive) {
		super(Failure.class, times);
		this.minTimeToRevive = minTimeToRevive;
		this.maxTimeToRevive = maxTimeToRevive;
	}
	
	@Override
	protected Event createEvent(long time) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		try {
			return Event.class.getConstructor(Steppable.class, long.class).newInstance(
					steppableClass.getConstructor(long.class).newInstance(random.nextLong(maxTimeToRevive-minTimeToRevive+1)+minTimeToRevive),
					time);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("This should never happen");
		}
	}

}

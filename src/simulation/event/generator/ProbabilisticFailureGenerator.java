package simulation.event.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;
import simulation.event.Event;
import simulation.event.Failure;
import ec.util.MersenneTwisterFast;

/**
 * A probabilistic event generator for generating UAV failures.
 * A special class is provided for generating UAV failures because UAV failures need additional parameters like the time of revival.
 * 
 * @author Wietse Buseyne
 *
 */
public class ProbabilisticFailureGenerator extends ProbabilisticEventGenerator {

	public long minTimeToRevive, maxTimeToRevive;
	
	public ProbabilisticFailureGenerator(int intervalBetween, double likelihood, long minTimeToRevive, long maxTimeToRevive) {
		super(Failure.class, intervalBetween, likelihood);
		this.minTimeToRevive = minTimeToRevive;
		this.maxTimeToRevive = maxTimeToRevive;
	}

	@Override
	public List<Event> getEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		if(events.get(sim.getNumUAVs()) == null)
			createEvents(sim, firstStep, lastStep);
		return events.get(sim.getNumUAVs());
	}

	@Override
	protected void createEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		List<Event> failures = new ArrayList<Event>();
		MersenneTwisterFast random = new MersenneTwisterFast();
		
		PriorityQueue<Long> timesOfRevival = new PriorityQueue<Long>();
		
		//likelihood is per UAV ==> we need number of UAVs not crashed at that time...
		int nb = sim.getNumUAVs();
		for(long i = firstStep; i <= lastStep; i += intervalBetween) {
			int uavs = nb;
			if(!timesOfRevival.isEmpty() && timesOfRevival.peek() < i) {
				timesOfRevival.poll();
				nb++;
			}
			for(int j = 0; j < uavs; j++) {
				if(likelihood > random.nextDouble()) {
					failures.add(createEvent(i));
					timesOfRevival.add(i+((Failure) failures.get(failures.size()-1).getSteppable()).getTimeUntilRevive());
					nb--;
				}
			}
		}
		this.events.put(sim.getNumUAVs(), failures);
	}
	
	protected Event createEvent(long time) {
		MersenneTwisterFast random = new MersenneTwisterFast();
		try {
			return Event.class.getConstructor(Steppable.class, long.class).newInstance(
					new Failure(random.nextLong(maxTimeToRevive-minTimeToRevive+1)+minTimeToRevive),
					time);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

}

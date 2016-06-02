package simulation.event.generator;

import java.util.List;

import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;
import simulation.event.Event;

/**
 * A simple probabilistic event generator that can be used for any event. The number of elements is static and thus subsequent calls to getEvents will return the same set of events.
 * 
 * @author Wietse Buseyne
 *
 */
public class SimpleProbabilisticEventGenerator extends ProbabilisticEventGenerator {
	
	private int nbOfElements;

	public SimpleProbabilisticEventGenerator(Class<? extends Steppable> steppableClass, int intervalBetween, double likelihood, int nbOfElements) {
		super(steppableClass, intervalBetween, likelihood);
		this.nbOfElements = nbOfElements;
	}
	
	@Override
	public List<Event> getEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		return super.getEvents(this.nbOfElements, firstStep, lastStep);
	}

	@Override
	protected void createEvents(UAVNetworkSimulation sim, long firstStep, long lastStep) {
		super.createEvents(this.nbOfElements, firstStep, lastStep);
	}
}

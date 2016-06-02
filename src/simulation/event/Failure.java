package simulation.event;

import sim.engine.SimState;
import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;

/**
 * A steppable class that will crash a random UAV for the specified amount of time.
 * After the time has elapsed the UAV will be revived.
 * If all UAVs have already crashed at the time this steppable is executed, nothing will happen.
 * 
 * @author Wietse Buseyne
 *
 */
public class Failure implements Steppable {

	private static final long serialVersionUID = 1L;
	private long timeUntilRevive;

	public Failure(long timeUntilRevive) {
		if(timeUntilRevive <= 0)
			throw new IllegalArgumentException("The time until revive must be strictly positive");
		this.timeUntilRevive = timeUntilRevive;
	}

	public long getTimeUntilRevive() {
		return timeUntilRevive;
	}

	@Override
	public void step(SimState sim) {
		try {
			((UAVNetworkSimulation) sim).getUAVController().getRandomUAV().crash(timeUntilRevive);
		} catch (IllegalStateException ex) {}
	}

}

package simulation.event;

import sim.engine.Schedule;
import sim.engine.Steppable;

/**
 * An event that can be scheduled on the specified time. 
 * What will happen is specified by the steppable provided to the constructor.
 * 
 * @author Wietse Buseyne
 *
 */
public class Event {
	
	private Steppable event;
	private long time;
	
	public Event(Steppable steppable, long time) {
		super();
		if(steppable == null)
			throw new IllegalArgumentException("Event cannot be null");
		if(time < 0)
			throw new IllegalArgumentException("Time cannot be negative");
		this.event = steppable;
		this.time = time;
	}

	public Steppable getSteppable() {
		return event;
	}

	public long getTime() {
		return time;
	}

	public void scheduleIn(Schedule schedule) {
		schedule.scheduleOnce(time, event);
	}

}

package uav;

/**
 * A counter that can tick when asked to and will execute an action when it runs out of time.
 * Can be reset to the original count or a new one. If autoreset is turned on, it will reset itself after the action has executed.
 * 
 * @author Wietse Buseyne
 *
 */
public class Counter {

	private long maxCount = 0;
	private long count = 0;
	private Runnable action;
	private boolean autoReset = false;
	
	/**
	 * Constructs a new empty counter
	 */
	public Counter() {
		this(-1, new Runnable() {
			
			@Override
			public void run() {
			}
		});
	}
	
	/**
	 * Constructs a new counter with the given steps and action.
	 * When the tick function is called 'steps' times, the action will be executed.
	 * Autoreset will be set to false.
	 * @param steps The amount of times before the action should be run
	 * @param action The action that runs when the time has passed.
	 */
	public Counter(long steps, Runnable action) {
		this(steps, action, false);
	}
	/**
	 * Constructs a new counter with the given steps and action.
	 * When the tick function is called 'steps' times, the action will be executed.
	 * @param steps The amount of times before the action should be run
	 * @param action The action that runs when the time has passed.
	 * @param autoReset Whether or not the counter should be reset immediately after the action has ran.
	 */
	public Counter(long steps, Runnable action, boolean autoReset) {
		this.maxCount = steps;
		this.count = steps;
		this.action = action;
		this.autoReset = autoReset;
	}
	
	public long getMax() {
		return maxCount;
	}

	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public void reset() {
		count = maxCount;
	}
	
	public void reset(long newCount) {
		maxCount = newCount;
		reset();
	}

	/**
	 * Removes one from the amount of ticks that is needed before the action is ran.
	 * If the amount is already zeor, the action is ran and the amount of ticks reset if autoreset is True for this instance.
	 * @return True if the counter is still ticking, False if the action was ran or the counter is not ticking anymore.
	 */
	public boolean tick() {
		if(count == 0) {
			count--;
			action.run();
			if(autoReset)
				count = maxCount;
		} else if(count > 0) {
			count--;
			return true;
		}
		return false;
	}

}

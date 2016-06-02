package sla;

/**
 * An abstract class representing a SLA that in any point of time is either fulfilled or not fulfilled.
 * Because of this property one can also ask the percentage that the SLA has been fulfilled between two certain times.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class TimeBoundedSLA {
	
	/**
	 * Tests whether or not the SLA was fulfilled at the specified step
	 * @param step The step at which the SLA's status should be checked.
	 * @return True if the SLA was fulfilled at the specified step, False otherwise
	 */
	public abstract boolean isFulfilled(long step);
	
	/**
	 * Requests the average percentage of which the SLA was fulfilled during the period between the given start an end steps.
	 * @param startStep The start of the period
	 * @param endStep The end of the period
	 * @param stepInterval The interval of steps at which the percentage should be checked. If this is 1, all percentages will be checked. 
	 * 						Can be increased to increase performance, but comes at the price of lost accuracy.
	 * @return A number between 0 and 100 inclusive that represents the percentage of the time the SLA was in the fulfilled state.
	 */
	public double getPercentage(long startStep, long endStep, long stepInterval){
		double percent = 0;
		int denom = 0;
		for(long i = startStep; i <= endStep; i += stepInterval) {
			denom++;
			if (isFulfilled(i))
				percent++;
		}
		return percent * 100 / denom;
	}
	
}

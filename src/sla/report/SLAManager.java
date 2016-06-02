package sla.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import simulation.UAVNetworkSimulation;
import sla.FlightTimeSLA;
import sla.ResponseTimeSLA;
import sla.TimeBoundedSLA;

/**
 * A class that manages different SLAs and can be used to get the results of these SLAs.
 *  
 * @author Wietse Buseyne
 *
 */
public class SLAManager {

	private UAVNetworkSimulation sim;
	private int nbDataPoints = 1825;
	
	private List<TimeBoundedSLA> timeSLAs = new ArrayList<TimeBoundedSLA>();
	private ResponseTimeSLA responseTimeSLA;
	private FlightTimeSLA flightTimeSLA = new FlightTimeSLA();
	
	
	public SLAManager(UAVNetworkSimulation sim) {
		this.sim = sim;
	}
	
	//Response time SLA
	public boolean isUAVFlyTime(long time) {
		return flightTimeSLA.isUAVFlyTime(time);
	}
	
	public void setFlightTimeSLA(int inspectionDaysPerMonth, int inspectionMinutesPerDay) {
		flightTimeSLA.setInspectionDaysPerMonth(inspectionDaysPerMonth);
		flightTimeSLA.setInspectionMinutesPerDay(inspectionMinutesPerDay);
	}

	public void setResponseTimeSLA(long goal) {
		responseTimeSLA = new ResponseTimeSLA(goal);
	}
	
	public List<Long> getResponseTimes() {
		if(responseTimeSLA == null)
			throw new IllegalStateException("No response time SLA has been set");
		return responseTimeSLA.getInspectionResponseTimes(sim.network);
	}
	
	public double getAverageResponseTime() {
		if(responseTimeSLA == null)
			throw new IllegalStateException("No response time SLA has been set");
		return responseTimeSLA.getAverageResponseTime(sim.network);
	}

	public long getResponseTimeGoal() {
		if(responseTimeSLA == null)
			throw new IllegalStateException("No response time SLA has been set");
		return responseTimeSLA.getGoal();
	}
	
	public void addInspectionSLA(TimeBoundedSLA sla) {
		timeSLAs.add(sla);
	}
	
	public List<TimeBoundedSLA> getInspectionSLAs() {
		return Collections.unmodifiableList(timeSLAs);
	}
	
	public int getNbOfDataPoints() {
		return nbDataPoints;
	}
	
	/**
	 * Returns an array of doubles where each double is a number between 0 and 1 that represents how many of the SLAs managed by this class where fulfilled at that time.
	 * A different element in the array represents a different time at which the average coverage of the SLAs was checked.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return An array of doubles representing the global coverages of the SLAs on different steps in the interval specified by the given steps.
	 */
	public double[] getTimeData(long firstStep, long lastStep) {
		long stepSize = (lastStep-firstStep) / nbDataPoints;
		if(stepSize == 0)
			stepSize = 1;
		int size = ((lastStep-firstStep+1)/stepSize) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ((lastStep-firstStep+1)/stepSize);
		double[] pfd = new double[size];
		for(TimeBoundedSLA sla : timeSLAs)
			for(int i = 0; i < size; i++) {
				if(sla.isFulfilled((i*stepSize)+firstStep))
					pfd[i]++;
			}
		for(int i = 0; i < size; i++)
			pfd[i] = pfd[i]/timeSLAs.size();
		return pfd;
	}
	
	/**
	 * Returns an array of doubles where each double is a number between 0 and 100 that represents the percentage of the SLAs managed by this class where fulfilled at that time.
	 * A different element in the array represents a different time at which the average coverage of the SLAs was checked.
	 * @see SLAManager.getFulfilledData() for more details. The values in the array will be the same, except they are multiplied by 100.
	 */
	public double[] getTimeComplianceData(long firstStep, long lastStep) {
		long stepSize = (lastStep-firstStep) / nbDataPoints;
		if(stepSize == 0)
			stepSize = 1;
		int size = ((lastStep-firstStep+1)/stepSize) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ((lastStep-firstStep+1)/stepSize);
		double[] pfd = new double[size];
		for(TimeBoundedSLA sla : timeSLAs)
			for(int i = 0; i < size; i++) {
				if(sla.isFulfilled((i*stepSize)+firstStep))
					pfd[i]++;
			}
		for(int i = 0; i < size; i++)
			pfd[i] = pfd[i]/timeSLAs.size()*100;
		return pfd;
	}	
	
	/**
	 * Returns an array of doubles where each double is a number between 0 and 100 that represents the coverage (%) of an SLA managed by this class.
	 * A different element in the array represents a different SLA.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return An array of doubles representing the coverages of the different SLAs.
	 */
	public double[] getSLAComplianceData(long firstStep, long lastStep) {
		long stepSize = (lastStep-firstStep) / nbDataPoints;
		if(stepSize == 0)
			stepSize = 1;
		int size = ((lastStep-firstStep+1)/stepSize) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ((lastStep-firstStep+1)/stepSize);
		double[] pfd = new double[timeSLAs.size()];
		for(int sla = 0; sla < timeSLAs.size(); sla++) {
			long percentage = 0;
			for(int i = 0; i < size; i++) {
				if(timeSLAs.get(sla).isFulfilled((i*stepSize)+firstStep))
					percentage++;
			}
			pfd[sla] = percentage * 100.0 / size;
		}
		return pfd;
	}
	
	/**
	 * Returns the variance on the global coverages. This gives an indication of how far the coverages over time are spread out from the average coverage.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return The variance of the global coverages in the specified period of time
	 */
	public double getTimeVariance(long firstStep, long lastStep) {
		double mean = percentageFulfilledBetween(firstStep, lastStep);
		double globalVariance = 0;
		double[] pfd = getTimeComplianceData(firstStep, lastStep);
		for (int i = 0; i < pfd.length; i++) {
			globalVariance += Math.pow(mean - pfd[i], 2);
		}
		globalVariance /= pfd.length;
		return globalVariance;
	}
	
	/**
	 * Returns the standard deviation of the global coverages. 
	 * This gives an indication of how far the coverages over time are spread out from the average coverage.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return The standard deviation of the global coverages in the specified period of time
	 */
	public double getTimeStandardDeviation(long firstStep, long lastStep) {
		return Math.sqrt(getTimeVariance(firstStep, lastStep));
	}
	
	/**
	 * Returns the variance on the coverages of the different SLAs during the specified period. 
	 * This gives an indication of how far the SLA coverages over time are spread out from the average coverage.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return The variance of the SLA coverages in the specified period of time
	 */
	public double getSLAVariance(long firstStep, long lastStep) {
		double mean = percentageFulfilledBetween(firstStep, lastStep);
		double slaVariance = 0;
		double[] pfd = getSLAComplianceData(firstStep, lastStep);
		for (int i = 0; i < pfd.length; i++) {
			slaVariance += Math.pow(mean - pfd[i], 2);
		}
		slaVariance /= pfd.length;
		return slaVariance;
	}
	
	/**
	 * Returns the standard deviation on the coverages of the different SLAs during the specified period. 
	 * This gives an indication of how far the SLA coverages over time are spread out from the average coverage.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return The standard deviation of the SLA coverages in the specified period of time
	 */
	public double getSLAStandardDeviation(long firstStep, long lastStep) {
		return Math.sqrt(getSLAVariance(firstStep, lastStep));
	}

	/**
	 * Returns the percentage of SLAs that was fulfilled at the given step.
	 * @param step The step to check the SLAs at
	 * @return A number between 0 and 100 inclusive representing the number of SLAs that are fulfilled at the given step.
	 */
	public double percentageFulfilledAt(long step) {
		double percent = 0;
		for(TimeBoundedSLA sla : timeSLAs) 
			if(sla.isFulfilled(step))
				percent++;
		percent *= 100;
		return percent /= timeSLAs.size();
	}
	
	/**
	 * Returns the average percentage of SLAs that were fulfilled during the period specified by the arguments.
	 * This number is calculated by taking the average over the different percentages during the specified period.
	 * @param step The step to check the SLAs at
	 * @return A number between 0 and 100 inclusive representing the average coverage of SLAs during the specified period.
	 */
	public double percentageFulfilledBetween(long firstStep, long lastStep) {
		double[] pfd = getTimeComplianceData(firstStep, lastStep);
		
		double percent = 0;
		for(int i = 0; i < pfd.length; i++) {
			percent += pfd[i];
		}
		return percent / pfd.length;
	}
	
	/**
	 * Returns the lowest of any average SLA coverages during the period specified by the arguments.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return The lowest of any average SLA coverages during the period specified by the arguments.
	 */
	public double lowestPercentage(long firstStep, long lastStep) {
		double min = Double.MAX_VALUE;
		long stepSize = (lastStep-firstStep) / nbDataPoints;
		for(TimeBoundedSLA sla : timeSLAs) {
			if(sla.getPercentage(firstStep, lastStep, stepSize) < min) {
				min = sla.getPercentage(firstStep, lastStep, stepSize);
			}
		}
		return min;
	}
	
	/**
	 * Checks whether all SLAs were above the specified percentage during the specified period.
	 * @param percentage The percentage of coverage all SLAs should reach.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return True if all SLAs in this manager reach the specified coverage during the specified period, False otherwise.
	 */
	public boolean allSLAsAbove(double percentage, long firstStep, long lastStep) {
		return lowestPercentage(firstStep, lastStep) >= percentage;
	}

	/**
	 * Checks whether the average coverage for the specified period is above the specified coverage.
	 * @param minCoverage The minimum coverage the average should be above.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return True if the average coverage of all SLAs in this manager is above the specified coverage during the specified period, False otherwise.
	 */
	public boolean coverageAbove(double minCoverage, long firstStep, long lastStep) {
		return percentageFulfilledBetween(firstStep, lastStep) >= minCoverage;
	}
	
	/**
	 * Checks if the average coverage of the SLAs is above the minCoverage and the coverage of each SLA above the minSLACoverage.
	 * @param minSLACoverage The percentage of coverage all SLAs should reach.
	 * @param minCoverage The minimum coverage the average should be above.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @see coverageAbove and allSLAsAbove
	 * @return True if the average coverage of all SLAs in this manager is above the specified coverage during the specified period and
	 *  		all SLAs in this manager reach the specified coverage during the specified period, False otherwise.
	 */
	public boolean SLAsFulfilled(double minCoverage, double minSLACoverage, long firstStep, long lastStep) {
		return coverageAbove(minCoverage, firstStep, lastStep) && allSLAsAbove(minSLACoverage, firstStep, lastStep);
	}
	
	/**
	 * Returns a list of all SLAs in this manager, sorted by average coverage during the specified period of time.
	 * @param firstStep The first step at which the coverage of the SLAs must be checked
	 * @param lastStep The last step at which the coverage of the SLAs must be checked
	 * @return A list of all SLAs in this manager, sorted by average coverage during the specified period of time.
	 */
	public List<TimeBoundedSLA> getSortedSLAs(final long firstStep, final long lastStep) {
		final long stepSize = (lastStep-firstStep) / nbDataPoints;
		timeSLAs.sort(new Comparator<TimeBoundedSLA>() {

			@Override
			public int compare(TimeBoundedSLA sla1, TimeBoundedSLA sla2) {
				if(sla1.getPercentage(firstStep, lastStep, stepSize) == sla2.getPercentage(firstStep, lastStep, stepSize))
					return 0;
				return sla1.getPercentage(firstStep, lastStep, stepSize) > sla2.getPercentage(firstStep, lastStep, stepSize) ? 1 : -1;
			}
		});
		return getInspectionSLAs();
	}
	
	/**
	 * Removes all SLAs of this manager.
	 */
	public void clear() {
		timeSLAs.clear();
		responseTimeSLA = null;
	}
	
}

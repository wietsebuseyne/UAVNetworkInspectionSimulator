package sla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import network.Inspectable;
import network.UAVNetwork;

/**
 * An SLA that measures the time it takes until a request for inspection is fulfilled.
 * @author Wietse Buseyne
 *
 */
public class ResponseTimeSLA {
	
	private long goal;
	
	/**
	 * Constructs a ResponseTimeSLA with the specified goal as the number of steps in which an inspection request should be fulfilled.
	 * @param goal The number of steps in which an inspection request should be fulfilled.
	 */
	public ResponseTimeSLA(long goal) {
		this.goal = goal;
	}

	/**
	 * Returns a list of all times it took to fulfill the inspection requests in the specified network.
	 * @param network The network for which to return all response times.
	 * @return A list of all times it took to fulfill the inspection requests in the specified network.
	 */
	public List<Long> getInspectionResponseTimes(UAVNetwork network) {
		List<Long> responseTimes = new ArrayList<Long>();
		for(Inspectable inspectable : network.getInspectables()) {
			for(Long t : inspectable.getInspectionNeededTimes()) {
				long nit = inspectable.getNextInspectionTimeAt(t);
				if(nit != 0)
					responseTimes.add(nit - t);
			}
		}
		Collections.sort(responseTimes);
		return responseTimes;
	}
	
	/**
	 * Returns the number of steps in which an inspection request should be fulfilled to satisfy this SLA.
	 * @return The number of steps in which an inspection request should be fulfilled to satisfy this SLA.
	 */
	public long getGoal() {
		return goal;
	}
	
	/**
	 * Returns the average response time for the specified network
	 * @param network The network for which to return the average response time.
	 * @return A number representing the average response time for the specified network measured in simulation steps.
	 */
	public double getAverageResponseTime(UAVNetwork network) {
		OptionalDouble average = getInspectionResponseTimes(network)
	            .stream()
	            .mapToDouble(a -> a)
	            .average();
		return average.isPresent() ? average.getAsDouble() : 0; 
	}

}

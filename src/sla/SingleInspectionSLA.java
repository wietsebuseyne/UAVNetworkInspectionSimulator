package sla;

import network.Inspectable;
/**
 * A class that represents a SLA that one inspectable element should be inspected once every x minutes.
 * Once an inspection happened, the SLA will be fulfilled and stay this way for as long as the time no inspection 
 * has happened does not exceed the maximum amount of minutes that's allowed between two inspections.
 * 
 * @author Wietse Buseyne
 *
 */
public class SingleInspectionSLA extends TimeBoundedSLA {
	
	private Inspectable inspectable;
	private long maximumStepsBetweenInspections;
	
	/**
	 * Constructs an SLA that defines the level of inspection of a single inspectable element, and that can return measurements about the fulfillment of this SLA.
	 * @param inspectable The element of the network for which the SLA should measure the inspection
	 * @param maximumMinutesBetweenInspections The maximum interval between two inspections, in simulation steps
	 */
	public SingleInspectionSLA(Inspectable inspectable,	long maximumMinutesBetweenInspections) {
		this.inspectable = inspectable;
		this.maximumStepsBetweenInspections = maximumMinutesBetweenInspections;
	}

	/**
	 * Returns the inspectable element this SLA applies to.
	 * @return The inspectable element this SLA applies to.
	 */
	public Inspectable getInspectable() {
		return inspectable;
	}

	@Override
	public boolean isFulfilled(long currentStep) {
		if(currentStep - inspectable.getLastInspectionTimeAt(currentStep) <= maximumStepsBetweenInspections)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return "Inspect " + inspectable + " once every " + maximumStepsBetweenInspections + " minutes";
	}

}

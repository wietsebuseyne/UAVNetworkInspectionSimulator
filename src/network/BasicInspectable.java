package network;

import java.util.ArrayList;
import java.util.List;

import sim.engine.Schedule;

/**
 * A basic implementation of an inspectable element. 
 * Keeps track of the inspection times, the times at which inspection has been requested and whether or not the element needs inspection.
 * @author Wietse Buseyne
 *
 */
public abstract class BasicInspectable implements Inspectable {
	
	protected boolean needsInspection;
	protected List<Long> inspectionTimes = new ArrayList<Long>();
	private List<Long> inspectionNeededTimes = new ArrayList<Long>();
	private double inspectionMultiplier = 1;
	
	public BasicInspectable(double inspectionMultiplier) {
		if(inspectionMultiplier < 0)
			throw new IllegalArgumentException("The inspection multiplier must be positive");
		this.inspectionMultiplier = inspectionMultiplier;
		inspectionTimes = new ArrayList<Long>();
		inspectionNeededTimes = new ArrayList<Long>();
	}
	
	public BasicInspectable() {
		inspectionTimes = new ArrayList<Long>();
		inspectionNeededTimes = new ArrayList<Long>();
	}
	
	@Override
	public long getLastInspectionTime() {
		if(inspectionTimes.size() == 0)
			return 0;
		return inspectionTimes.get(inspectionTimes.size()-1);
	}

	@Override
	public long getLastInspectionTimeAt(long step) {
		if(inspectionTimes.size() == 0)
			return 0;
		int i = 0;
		while(i < inspectionTimes.size() && inspectionTimes.get(i) < step)
			i++;
		return i == 0? 0 : inspectionTimes.get(i-1);
	}

	@Override
	public void addInspection(Schedule schedule) {
		inspectionTimes.add(schedule.getSteps());
		needsInspection = false;
	}

	@Override
	public boolean needsInspection() {
		return needsInspection;
	}

	@Override
	public void inspectionNeeded(Schedule schedule) {
		this.needsInspection = true;
		inspectionNeededTimes.add(schedule.getSteps());
	}

	@Override
	public List<Long> getInspectionNeededTimes() {
		return inspectionNeededTimes;
	}

	@Override
	public long getNextInspectionTimeAt(long step) {
		if(inspectionTimes.isEmpty())
			return 0;
		int i = 0;
		while(i < inspectionTimes.size() && inspectionTimes.get(i) < step)
			i++;
		return inspectionTimes.get(i);
	}
	
	@Override
	public double getRiskMultiplier() {
		return inspectionMultiplier;
	}
	
}

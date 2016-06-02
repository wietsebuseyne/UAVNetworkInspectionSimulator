package network;

import java.util.List;

import sim.engine.Schedule;

/**
 * If a class is inspectable it means we can inspect it at a certain time, ask the last inspection time from it as well as the time it takes to inspect this element.
 * Furthermore the class can be in need of inspection.
 * 
 * @author Wietse Buseyne
 *
 */
public interface Inspectable {
	
	public int getTimeToInspect();
	
	public long getLastInspectionTime();
	
	public long getLastInspectionTimeAt(long step);

	public long getNextInspectionTimeAt(long t);

	public void addInspection(Schedule schedule);
	
	public boolean needsInspection();

	public void inspectionNeeded(Schedule schedule);

	public List<Long> getInspectionNeededTimes();
	
	public double getRiskMultiplier();
	
}

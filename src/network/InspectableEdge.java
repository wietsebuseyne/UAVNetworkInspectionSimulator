package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;

/**
 * An edge of a network that can be inspected. 
 * The edge keeps track of its digital pheromones
 * @author Wietse Buseyne
 *
 */
public class InspectableEdge extends Edge implements Inspectable, Steppable {

	private static final long serialVersionUID = 1L;
	private int pheromoneLevel = 0;
	private boolean needsInspection;
	private double riskLevelMultiplier = 1.0;
	private long lastInspectionStartTime = 0;

	private Map<Object, Long> inspectionStartTimes = new HashMap<>();
	private List<Long> inspectionTimes = new ArrayList<Long>(),
			inspectionNeededTimes = new ArrayList<Long>();
	
	public InspectableEdge(Object from, Object to, double riskLevelMultiplier) {
		super(from, to, 0);
		setRiskLevelMultiplier(riskLevelMultiplier);
	}
	
	public Node getOtherNode(Node node) {
		return (Node) super.getOtherNode(node);
	}

	public double getRiskLevelMultiplier() {
		return riskLevelMultiplier;
	}

	public void setRiskLevelMultiplier(double riskLevelMultiplier) {
		if(riskLevelMultiplier < 0)
			throw new IllegalArgumentException("The risk level multiplier must be positive");
		this.riskLevelMultiplier = riskLevelMultiplier;
	}

	private void increasePheromone(Schedule schedule) {
		/*schedule.scheduleOnce(schedule.getSteps()+1440, this);
		schedule.scheduleOnce(schedule.getSteps()+2880, this);
		schedule.scheduleOnce(schedule.getSteps()+5760, this);
		schedule.scheduleOnce(schedule.getSteps()+11520, this);
		schedule.scheduleOnce(schedule.getSteps()+23040, this);
		schedule.scheduleOnce(schedule.getSteps()+46080, this);
		schedule.scheduleOnce(schedule.getSteps()+92160, this);
		schedule.scheduleOnce(schedule.getSteps()+184320, this);
		schedule.scheduleOnce(schedule.getSteps()+368640, this);
		schedule.scheduleOnce(schedule.getSteps()+525600, this);*/
		/*schedule.scheduleOnce(schedule.getSteps()+52560, this);
		schedule.scheduleOnce(schedule.getSteps()+105120, this);
		schedule.scheduleOnce(schedule.getSteps()+157680, this);
		schedule.scheduleOnce(schedule.getSteps()+210240, this);
		schedule.scheduleOnce(schedule.getSteps()+262800, this);
		schedule.scheduleOnce(schedule.getSteps()+315360, this);
		schedule.scheduleOnce(schedule.getSteps()+367920, this);
		schedule.scheduleOnce(schedule.getSteps()+420480, this);
		schedule.scheduleOnce(schedule.getSteps()+473040, this);
		schedule.scheduleOnce(schedule.getSteps()+525600, this);*/
		schedule.scheduleOnce(schedule.getSteps()+525600, this);
		pheromoneLevel++;
		//pheromoneLevel += 10;
		this.info = (int)getRiskLevelMultiplier() + " " + pheromoneLevel;
	}
	
	private void decreasePheromone() {
		pheromoneLevel--;
		this.info = (int)getRiskLevelMultiplier() + " " + pheromoneLevel;
	}
	
	public int getPheromoneLevel() {
		return pheromoneLevel;
	}

	@Override
	public void step(SimState sim) {
		decreasePheromone();
	}
	
	public boolean isUnderInspection() {
		return !inspectionStartTimes.isEmpty();
	}

	public long getLastInspectionStartTime() {
		return lastInspectionStartTime;
	}
	
	public long getLIT(){
		return getLastInspectionStartTime() == 0 ? getLastInspectionTime() : getLastInspectionStartTime();
	}

	public synchronized void startInspection(Object o, long startTime) {
		lastInspectionStartTime = startTime;
		inspectionStartTimes.put(o, startTime);
	}
	
	public synchronized void stopInspection(Object o) {
		inspectionStartTimes.remove(o);
		lastInspectionStartTime = 0;
		for(Long startTime : inspectionStartTimes.values())
			if(startTime > lastInspectionStartTime)
				lastInspectionStartTime = startTime;
	}
	
	public int getNbInspections() {
		return inspectionTimes.size();
	}

	@Override
	public int getTimeToInspect() {
		return 0;
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
	public long getNextInspectionTimeAt(long step) {
		if(inspectionTimes.isEmpty())
			return 0;
		int i = 0;
		while(i < inspectionTimes.size()-1 && inspectionTimes.get(i) < step)
			i++;
		return inspectionTimes.get(i) < step ? 0 : inspectionTimes.get(i);
	}

	@Override
	public void addInspection(Schedule schedule) {
		needsInspection = false;
		inspectionTimes.add(schedule.getSteps());
		increasePheromone(schedule);
	}

	@Override
	public boolean needsInspection() {
		return needsInspection;
	}

	@Override
	public void inspectionNeeded(Schedule schedule) {
		if(!needsInspection)
			inspectionNeededTimes.add(schedule.getSteps());
		this.needsInspection = true;
	}

	@Override
	public List<Long> getInspectionNeededTimes() {
		return Collections.unmodifiableList(inspectionNeededTimes);
	}
	
	@Override
	public String toString() {
		return getFrom().toString() + " - " + getTo().toString();
	}
	
	public double length() {
		return ((Node) getFrom()).getLocation().distance(((Node)getTo()).getLocation());
	}

	@Override
	public double getRiskMultiplier() {
		return riskLevelMultiplier;
	}

}

package network;

import java.util.ArrayList;

import sim.util.Double2D;

/**
 * A node that can be part of a network. The node is inspectable and can possibly be a recharge node for UAVs.
 * @author Wietse Buseyne
 *
 */
public class Node extends BasicInspectable { 
	
	private Double2D location;
	private boolean isRechargeStation;
	public static int DEFAULT_INSPECTION_TIME = 0;
	private long minStepsBetweenInspections;
	private int inspectionTime;
	
	public Node(double x, double y, long minStepsBetweenInspections) {
		this(new Double2D(x, y), minStepsBetweenInspections);
	}
	
	public Node(double x, double y, boolean isRechargeStation, long minStepsBetweenInspections) {
		this(new Double2D(x, y), isRechargeStation, minStepsBetweenInspections);
	}
	
	public Node(Double2D location, long minStepsBetweenInspections) {
		this(location, false, minStepsBetweenInspections);
	}
	
	public Node(Double2D location, boolean isRechargeStation, long minStepsBetweenInspections) {
		this(location, isRechargeStation, DEFAULT_INSPECTION_TIME, minStepsBetweenInspections);
	}
	
	public Node(Double2D location, boolean isRechargeStation, int inspectionTime, long minStepsBetweenInspections) {
		super();
		this.isRechargeStation = isRechargeStation;
		this.location = location;
		setInspectionTime(inspectionTime);
		setMinStepsBetweenInspections(minStepsBetweenInspections);
	}
	
	public void setInspectionTime(int inspectionTime) {
		if(inspectionTime < 0)
			throw new IllegalArgumentException("The inspection time must be positive");
		this.inspectionTime = inspectionTime;
	}

	public double distance(Node other) {
		return getLocation().distance(other.getLocation());
	}
	
	public void initialize() {
		inspectionTimes = new ArrayList<Long>();
	}

	public long getMinStepsBetweenInspections() {
		return minStepsBetweenInspections;
	}

	private void setMinStepsBetweenInspections(long minStepsBetweenInspections) {
		this.minStepsBetweenInspections = minStepsBetweenInspections;
	}

	public boolean isRechargeNode() {
		return isRechargeStation;
	}
	
	public void setRechargeNode(boolean isRechargeNode) {
		this.isRechargeStation = isRechargeNode;
	}

	public Double2D getLocation() {
		return location;
	}
	
	public void setLocation(Double2D location) {
		if(location == null)
			throw new IllegalArgumentException("The location cannot be null");
		this.location = location;
	}
	
	@Override
	public String toString() {
		return "(" + location.x + ", " + location.y + ")";
	}

	@Override
	public int getTimeToInspect() {
		return inspectionTime;
	}

}

package uav;
import java.util.List;

import network.Node;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.MutableDouble2D;
import simulation.UAVNetworkSimulation;
import uav.navigation.CentralServerLNINavigation;
import uav.navigation.EdgeNodeLocation;
import uav.navigation.UAVNavigationStrategy;

/**
 * Represents a UAV that can fly over a network according to a specific navigation behaviour.
 * The UAV can go to standby, has a battery life and can inspect edges and nodes of a network.
 * 
 * @author Wietse Buseyne
 *
 */
public class UAV implements Steppable {
	
	private static final long serialVersionUID = 1;
	
	private EdgeNodeLocation nextLocation;
	private UAVNavigationStrategy navigationBehaviour;
	
	private boolean crashed = false;
	private long rechargeTime = 10;
	private double speedKmHour = 20;
	private double broadcastRadius = 5;

	private boolean standbyPossible = true;
	private long standByTime = 0;
	private long standByCounter = 0, standByCounterQueue = 0;
		
	private Counter batteryLife, inspectionCounter, rechargeCounter, crashCounter;
	
	private static double nextRechargeNodeSafetyMultiplier = 1.5;
	
	public UAV(Node startNode) {
		this(startNode, new CentralServerLNINavigation(), new UAVConfiguration());
	}
	
	public UAV(Node startNode, UAVNavigationStrategy navigationBehaviour, UAVConfiguration configuration) {
		this.speedKmHour = configuration.speedKmHour;
		this.broadcastRadius = configuration.broadcastRadius;
		this.navigationBehaviour = navigationBehaviour;
		this.nextLocation = new EdgeNodeLocation(null, startNode);
		
		batteryLife = new Counter(configuration.batteryLife, new Runnable() {
			
			@Override
			public void run() {
				System.out.println("UAV crashed");
				crash();
			}
		});
		
		rechargeTime = configuration.rechargeTime;
		rechargeCounter = new Counter(rechargeTime, new Runnable() {
			
			@Override
			public void run() {
				//System.out.println("Recharging... Old battery level: " + getBatteryLife());
				recharge();
			}
		});
		rechargeCounter.setCount(-1);
		
		crashCounter = new Counter(0, new Runnable() {
			
			@Override
			public void run() {
				crashed = false;
			}
		});
		crashCounter.setCount(-1);
	}
	
	public void setStandby(boolean standbyPossible) {
		this.standbyPossible = standbyPossible;
	}
	
	/**
	 * Returns the maximum distance in km the UAV can fly (i.e., with a full battery).
	 * @return The maximum distance in km the UAV can fly (i.e., with a full battery).
	 */
	public double getMaxFlyingDistance() {
		return getFlyingDistance(batteryLife.getMax());
	}
	
	/**
	 * Returns the distance in km the UAV can fly with the given battery life.
	 * @param batteryLife The battery life
	 * @return The distance in km the UAV can fly with the given battery life.
	 */
	public double getFlyingDistance(long batteryLife) {
		return getSpeedKmMinute() * batteryLife;
	}

	/**
	 * Returns the distance in km the UAV can fly with its current battery life.
	 * @return The distance in km the UAV can fly with its current battery life.
	 */
	public double getFlyingDistance() {
		return getSpeedKmMinute() * getBatteryLife();
	}
	
	/**
	 * Resets the battery level of this UAV to its maximum
	 */
	public void recharge() {
		batteryLife.reset();
	}
	
	/**
	 * Returns the amount of minutes the UAV can fly for with its current battery level.
	 * @return The amount of minutes the UAV can fly for with its current battery level.
	 */
	public long getBatteryLife() {
		return batteryLife.getCount();
	}

	/**
	 * Returns the amount of minutes the UAV has been in standby time. 
	 * This only includes the time the UAV could be flying according to the flight time SLA, but decided to stay standby. 
	 * @return The amount of minutes the UAV has been in standby time. 
	 */
	public long getStandByTime() {
		return standByTime;
	}
	
	/**
	 * Returns the speed in km/h for this UAV
	 * @return The speed in km/h
	 */
	public double getSpeedKmHour() {
		return speedKmHour;
	}
	
	/**
	 * Returns the speed in km/m for this UAV
	 * @return The speed in km/m
	 */
	public double getSpeedKmMinute() {
		return speedKmHour / 60;
	}

	public void setSpeedKmHour(double speedKmHour) {
		if(speedKmHour < 0)
			throw new IllegalArgumentException("The speed must be positive!");
		this.speedKmHour = speedKmHour;
	}


	public long getRechargeTime() {
		return rechargeTime;
	}
	/**
	 * Sets the time it takes for this UAV to recharge its battery.
	 * This time includes landing on a recharge station, recharging (or replacing) its battery and getting ready to fly again.
	 * @param rechargeTime The new recharge time
	 */
	public void setRechargeTime(long rechargeTime) {
		this.rechargeTime = rechargeTime;
	}

	public UAVNavigationStrategy getNavigationBehaviour() {
		return navigationBehaviour;
	}

	@Override
	public void step(final SimState state) {
		final UAVNetworkSimulation sim = (UAVNetworkSimulation)state;
		if(crashCounter.tick()) {
			//Do nothing if crashed and not yet revived
			//Revive time continues outside fly time as well
		} else if(!sim.isUAVFlyTime()) {
		} else if(rechargeCounter.tick()) {
			//Do nothing if recharging
		} else if(standByCounter > 0) {
			standByCounter--;
			standByTime++;
		} else if(!crashed) {
			batteryLife.tick();
			if(standByCounterQueue > 0)
				standByCounterQueue--;
			
			if(nextLocation == null) {
				nextLocation = new EdgeNodeLocation(null, navigationBehaviour.getStartLocation(sim));
			}
			if(inspectionCounter == null) {
				inspectionCounter = new Counter(0, new Runnable() { //start new counter, will update itself on next location
					
					@Override
					public void run() {
						boolean recharge = false;
						Node previous = nextLocation.getNode();
						if(nextLocation.getNode().isRechargeNode()) {
							recharge = true;
						}
						//Go to standby mode if needed
						if(standByCounterQueue > 0) {
							standByCounter = standByCounterQueue;
							standByCounterQueue = 0;
						}
						if(state.schedule.getSteps() - nextLocation.getNode().getLastInspectionTime() >= nextLocation.getNode().getMinStepsBetweenInspections())
							nextLocation.getNode().addInspection(state.schedule);
						
						if(nextLocation.getEdge() != null) {
							nextLocation.getEdge().stopInspection(UAV.this); 
							nextLocation.getEdge().addInspection(state.schedule);
						}
						
						nextLocation = navigationBehaviour.getNextDestination(sim, nextLocation.getNode());
						if(nextLocation != null) {
							//If recharge station and cannot reach next station
							if(recharge && (!nextLocation.getNode().isRechargeNode() || nextLocation.getNode().distance(previous)*nextRechargeNodeSafetyMultiplier > getFlyingDistance())) {
								rechargeCounter.reset();
							}
							nextLocation.getEdge().startInspection(UAV.this, state.schedule.getSteps());
							inspectionCounter.reset(0);
						} else { //No next location, go to standby
						}
					}
				});
			}
			
			Double2D me = sim.map.getObjectLocation(this);
	
			if(me.distance(nextLocation.getNode().getLocation()) <= getSpeedKmMinute()/2) {
				if(nextLocation.getNode().isRechargeNode() && getBatteryLife() < inspectionCounter.getCount()) { //otherwise inspection cannot complete
					rechargeCounter.reset();
				} else {
					if(state.schedule.getSteps() - nextLocation.getNode().getLastInspectionTime() < nextLocation.getNode().getMinStepsBetweenInspections())
						inspectionCounter.setCount(0);
					inspectionCounter.tick();
				}
			}
			
			MutableDouble2D sumForces = new MutableDouble2D();
			
			sumForces.addIn(new Double2D((nextLocation.getNode().getLocation().x-me.x), (nextLocation.getNode().getLocation().y-me.y)));
			try {
				sumForces.normalize();
			} catch(ArithmeticException ex) {}
			sumForces.setX(sumForces.getX()*getSpeedKmMinute());
			sumForces.setY(sumForces.getY()*getSpeedKmMinute());
			sumForces.addIn(me);
			
			sim.map.setObjectLocation(this, new Double2D(sumForces));
		}
	}
	
	/**
	 * Puts the UAV in stanby mode for the specified amount of steps
	 * @param nbOfSteps The amount of steps this UAV should stay in standby mode
	 */
	public void standBy(long nbOfSteps) {
		if(nbOfSteps < 0)
			throw new IllegalArgumentException("The number of steps to be standby must be positive");
		if(standbyPossible)
			if(standByCounter > 0) {
				if(nbOfSteps > standByCounter)
					standByCounter = nbOfSteps;
			} else if(nbOfSteps > standByCounterQueue) {
				standByCounterQueue = nbOfSteps;
			}
	}
	
	/**
	 * Checks if the UAV is currently in standby mode.
	 * @return True if the UAV is in standby mode, False otherwise.
	 */
	public boolean isStandBy() {
		return standByCounter > 0;
	}
	
	/**
	 * Sets the navigation strategy of this UAV to the specified one.
	 * @param navigationBehaviour The new navigation strategy of this UAV
	 */
	public void setNavigation(UAVNavigationStrategy navigationBehaviour) {
		this.navigationBehaviour = navigationBehaviour;
	}
	
	/**
	 * Crashes this UAV. It will stop all its activities immediately and stay crashed during the simulation.
	 */
	public void crash() {
		crashed = true;
		if(nextLocation.getEdge() != null)
			nextLocation.getEdge().stopInspection(this);
	}
	
	/**
	 * Crashes this UAV for the specified amount of time. During this time, the UAV will not perform any action.
	 * After the time has passed, it will continue its action again.
	 * @param time The amount of time for which the UAV will crash.
	 */
	public void crash(long time) {
		crash();
		crashCounter.setCount(time-1);
	}
	
	/**
	 * Checks if the UAV is currently crashed.
	 * @return True if the UAV is currently crashed, False otherwise.
	 */
	public boolean hasCrashed() {
		return crashed;
	}
	
	/**
	 * Instructs this UAV to inspect the specified locations next.
	 * This information will be passed on to the navigation strategy which will decide what to do with it.
	 * @param nextLocations The next locations the UAV is instructed to fly to.
	 */
	public void inspect(List<EdgeNodeLocation> nextLocations) {
		navigationBehaviour.inspect(nextLocations);
	}
	
	/**
	 * Returns the current job of this UAV
	 * @return The current job of this UAV
	 */
	public JobDescription getJobDescription() {
		return navigationBehaviour.getJob();
	}

	/**
	 * Returns the broadcast radius of this UAV
	 * @return The broadcast radius of this UAV
	 */
	public double getBroadcastRadius() {
		return broadcastRadius;
	}

}

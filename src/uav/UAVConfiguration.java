package uav;

/**
 * A class that holds the necessary variables to define a UAV: speed, battery life, broadcast radius and recharge time.
 * Can be used to initialize a UAV.
 * 
 * @author Wietse Buseyne
 *
 */
public class UAVConfiguration {
	
	public long rechargeTime = 10;
	public double broadcastRadius = 5;
	public double speedKmHour = 20;
	public long batteryLife = 20;
	
	public double getSpeedKmMinute() {
		return speedKmHour / 60;
	}
	
	public double getMaxFlyingDistance() {
		return getSpeedKmMinute() * batteryLife;
	}
	
}

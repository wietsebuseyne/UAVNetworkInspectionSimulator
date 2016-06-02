package simulation.event.generator;

import java.util.List;
import java.util.Map;

import simulation.UAVNetworkSimulation;
import simulation.event.EdgeInspection;
import simulation.event.NodeInspection;

/**
 * Creates event generators based on the specified configuration.
 * @author Wietse Buseyne
 *
 */
public class EventGeneratorFactory {
	
	private UAVNetworkSimulation sim;
	
	public EventGeneratorFactory(UAVNetworkSimulation sim) {
		if(sim == null)
			throw new IllegalArgumentException("Sim cannot be null");
		this.sim = sim;
	}

	@SuppressWarnings("unchecked")
	public EventGenerator createEventGenerator(Map<String, Object> configuration) {
		if(!(configuration.containsKey("type")  && configuration.containsKey("event")))
			throw new IllegalArgumentException("The configuration should contain the 'type' and 'event' keys with a string value");
		
		IllegalArgumentException typeEx = new IllegalArgumentException("The type of the configuration should be either 'probabilistic' or 'static'"),
				eventEx = new IllegalArgumentException("The event of the configuration should be either 'failure', 'node' or 'edge'");

		EventGenerator eventGenerator = null;
		switch((String)configuration.get("type")) {
		case "probabilistic": 
			switch((String)configuration.get("event")) {
			case "failure": eventGenerator = new ProbabilisticFailureGenerator(
						((Number) configuration.get("intervalBetween")).intValue(),
						(double) configuration.get("likelihood"),
						((Number)configuration.get("minTimeToRevive")).longValue(),
						((Number)configuration.get("maxTimeToRevive")).longValue()); break;
			case "edge": eventGenerator = new SimpleProbabilisticEventGenerator(
					EdgeInspection.class,
					((Number) configuration.get("intervalBetween")).intValue(),
					(double) configuration.get("likelihood"),
					sim.network.getNbNodes()); break;
			case "node": eventGenerator = new SimpleProbabilisticEventGenerator(
					NodeInspection.class,
					((Number) configuration.get("intervalBetween")).intValue(),
					(double) configuration.get("likelihood"),
					sim.network.getNbEdges()); break;
			default: throw eventEx;
			}; break;
		case "static": 
			List<Number> timesList = (List<Number>) configuration.get("times");
			long[] times = new long[timesList.size()];
			for(int i = 0; i < times.length; i++)
				times[i] = timesList.get(i).longValue();
			switch((String) configuration.get("event")) {
			case "failure": eventGenerator = 
					new StaticFailureGenerator(
							times, 
							((Number)configuration.get("minTimeToRevive")).longValue(), 
							((Number)configuration.get("maxTimeToRevive")).longValue()); break;
			case "edge": eventGenerator = 
					new StaticEventGenerator(EdgeInspection.class, times); break;
			case "node": eventGenerator = 
					new StaticEventGenerator(NodeInspection.class, times); break;
			default: throw eventEx;
			}; break;
		default: throw typeEx;
		}
		return eventGenerator;
	}

}

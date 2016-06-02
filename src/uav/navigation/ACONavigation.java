package uav.navigation;

import network.InspectableEdge;
import network.Node;
import network.UAVNetwork;
import sim.util.Bag;
import simulation.UAVNetworkSimulation;
import uav.navigation.aco.ACOImpl;
import uav.navigation.aco.LNIACOImpl;

/**
 * Uses digital pheromones and a heuristic function to decide which edge to navigate on next.
 * The implementation of the pheromone and heuristic computation can be easily switched to a different strategy.
 * 
 * @author Wietse Buseyne
 *
 */
public class ACONavigation extends SimpleStartLocationNavigation {
	
	public static double ALPHA = 12.64, BETA = 9.78;
	public static final ACOImpl DEFAULT_IMPL = new LNIACOImpl();
	private double alpha, beta;
	private ACOImpl acoStrategy;
	
	public static Class<?>[] getParamTypes() {
		Class<?>[] paramTypes = new Class<?>[3];
		paramTypes[0] = double.class;
		paramTypes[1] = double.class;
		paramTypes[2] = ACOImpl.class;
		return paramTypes;
	}
	
	public ACONavigation() {
		this(ALPHA, BETA);
	}
	
	public ACONavigation(double alpha, double beta) {
		this(alpha, beta, new LNIACOImpl());
	}
	
	public ACONavigation(double alpha, double beta, ACOImpl acoStrategy) {
		this.alpha = alpha;
		this.beta = beta;
		this.acoStrategy = acoStrategy;
	}

	@Override
	public EdgeNodeLocation getNextDestination(UAVNetworkSimulation sim, Node currentLocation) {
		Bag edges = sim.network.getEdges(currentLocation, null);
		double randomNumber = sim.random.nextDouble(false, true);
		double[] probabilities = computeProbabilities(sim.network, currentLocation, sim.schedule.getSteps());
		for(int i = 0; i < probabilities.length; i++) {
			if(randomNumber < probabilities[i]) {
				return new EdgeNodeLocation((InspectableEdge)edges.get(i), (Node)((InspectableEdge)edges.get(i)).getOtherNode(currentLocation));
			}
			randomNumber -= probabilities[i];
		}
		destination = new EdgeNodeLocation((InspectableEdge)edges.get(edges.size()-1), (Node)((InspectableEdge)edges.get(edges.size()-1)).getOtherNode(currentLocation));
		return destination;
	}
	
	private double[] computeProbabilities(UAVNetwork network, Node node, long currentStep) {
		double[] probabilities = new double[network.getEdges(node, null).size()];
		double denom = 0;
		for(int i = 0; i < probabilities.length; i++) {
			InspectableEdge edge = (InspectableEdge) network.getEdges(node, null).get(i);
			probabilities[i] = 
					acoStrategy.computeHeuristicValue(network, node, edge, currentStep, beta) *
					acoStrategy.computePheromone(currentStep, edge, alpha);
			denom += probabilities[i];
		}
		for(int i = 0; i < probabilities.length; i++) {
			probabilities[i] = denom == 0 ? 0 : probabilities[i] / denom;
		}
		return probabilities;
	}
	
	@Override
    public String toString() {
		return "ACONavigation_A" + alpha + "_B" + beta + "_" + acoStrategy.getClass().getSimpleName().substring(0, acoStrategy.getClass().getSimpleName().length()-7);
	}
	
}

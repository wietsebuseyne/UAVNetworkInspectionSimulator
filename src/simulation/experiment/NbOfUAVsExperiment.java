package simulation.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.data.category.DefaultCategoryDataset;

import simulation.UAVNetworkSimulation;
import uav.navigation.NavigationInitializationClass;
import util.parsers.configuration.UAVNavigationClassDeserialiser;

/**
 * An abstract class that provides a framework for executing multiple simulations with different numbers of UAVs and/or different navigation strategies.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class NbOfUAVsExperiment extends Experiment {
	
	protected int minNbOfUAVs = 10, maxNbOfUAVs = 20, uavStep = 5;
	//protected List<Class<? extends UAVNavigationBehaviour>> navigationStrategies = new ArrayList<Class<? extends UAVNavigationBehaviour>>();
	protected List<NavigationInitializationClass> navigationStrategies = new ArrayList<NavigationInitializationClass>();
	protected boolean cycleNavigation = false;
	protected long firstStep, lastStep;
	@SuppressWarnings("unused")
	private DefaultCategoryDataset minUAVsDS = new DefaultCategoryDataset();
	
	public NbOfUAVsExperiment(UAVNetworkSimulation sim) {
		super(sim);
	}

	public NbOfUAVsExperiment(UAVNetworkSimulation sim, Map<String, Object> configuration) {
		super(sim);
		initializeWithConfiguration(configuration);
	}

	@SuppressWarnings("unchecked")
	public void initializeWithConfiguration(Map<String, Object> configuration) {
		try {
			minNbOfUAVs =  ((Double)configuration.get("minNbOfUAVs")).intValue();
			maxNbOfUAVs = ((Double)configuration.get("maxNbOfUAVs")).intValue();
			uavStep = ((Double)configuration.get("uavStep")).intValue();
			List<String> navs = (List<String>) configuration.get("navigationStrategies");
			for(String navName : navs)
				if(navName.equals("CycleNavigation"))
					cycleNavigation = true;
				else
					navigationStrategies.add(UAVNavigationClassDeserialiser.deserialize(navName));
		} catch(NullPointerException | NumberFormatException | ClassCastException ex) {
			throw new IllegalArgumentException(
					"The experiment configuration is not correctly formatted. "
					+ "It should have contain the following structure (with possibly multiple configurations defined):\n"
					+ "'configuration':{"
					+ "\n\t'minNbOfUAVs':int,"
					+ "\n\t'maxNbOfUAVs':int,"
					+ "\n\t'uavStep':int,"
					+ "\n\t'navigationStrategies':[String]"
					+ "\n}");
		}
	}
	
	@Override
	public void execute() {
		System.out.println("Started experiment");
		sim.initializeSLAs();
		long nbOfSteps = sim.getConfiguration().steps;
		firstStep = sim.getStepsInSLACycle()*2;
		if(cycleNavigation) {
			if(sim.getCycleNavigation() == null) {
				CycleNavigationExperiment e = new CycleNavigationExperiment(sim);
				e.execute();
			} else 
				sim.setCycleNavigation(true);
			long stepsInCycle = sim.getStepsInNavCycle();
				
			int UAVsNeeded = (int) (stepsInCycle / sim.getStepsInSLACycle())+1;
			long stepsBetween = stepsInCycle / UAVsNeeded;
			
			//First step is the maximum of the cycleNavigation and other navigations
			firstStep = Math.max((1+UAVsNeeded)*stepsBetween, firstStep);
			lastStep = nbOfSteps + firstStep;

			prepareCycleDatasets();
			System.out.println("\nSimulating with strategy: CycleNavigation");
			doSingleExperiment();

		}
		lastStep = nbOfSteps + firstStep;
		sim.unsetCycleNavigation();
		
		for(NavigationInitializationClass nav : navigationStrategies) {
			System.out.println("\n-----------------------------------------------\n\nSimulating with strategy: " + nav.getSimpleName());
			sim.setNavigationBehaviour(nav);
			prepareStrategyDatasets(nav.getSimpleName());
			doSingleExperiment();
		}
		
		writeGraphs();
		sim.reporter.clear();
		System.out.println("\nFinished experiment");
	}
	
	protected void runSimulation(int nb) {
		runSimulation(nb, true, false);
	}
	
	protected void runSimulation(int nb, boolean events) {
		runSimulation(nb, events, false);
	}
	
	protected void runSimulation(int nb, boolean events, boolean boxDatasets) {
		System.out.print("\nSimulating with " + nb + " UAVs...");
		sim.setNumUAVs(nb);
		sim.start();
		if(events)
			sim.addEvents(firstStep, lastStep);
		do {
			if (!sim.schedule.step(sim)) break;
		} while(sim.schedule.getSteps() < lastStep);
		sim.finish();
		System.out.println(" " + sim.getSlaChecker().percentageFulfilledBetween(firstStep, lastStep) + "%");
		sim.addToDatasets(nb, sim.getNavigationBehaviourClass().getSimpleName(), firstStep, lastStep, boxDatasets);
	}

	protected abstract void prepareCycleDatasets();
	
	protected abstract void prepareStrategyDatasets(String navigationName);
	
	protected abstract void writeGraphs();
	
	protected abstract void doSingleExperiment();

}

package simulation.experiment;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import simulation.UAVNetworkSimulation;
import uav.navigation.ACONavigation;
import uav.navigation.NavigationInitializationClass;
import uav.navigation.aco.ACOImpl;
import util.algorithms.Pair;

/**
 * A class that executes the same simulation multiple times with different alpha and beta values for the ACONavigation and writes the SLA coverages to a file.
 * 
 * @author Wietse Buseyne
 *
 */
public class ACOConfigurationExperiment extends Experiment {

	private int nbOfUAVs, nbOfConfigurations, nbOfSimulationsPerConfiguration;
	private ACOImpl acoImpl;
	
	private String output;

	private long firstStep, lastStep;

	public ACOConfigurationExperiment(UAVNetworkSimulation sim, Map<String, Object> configuration) {
		super(sim);
		try {
			nbOfUAVs =  ((Double)configuration.get("nbOfUAVs")).intValue();
			nbOfConfigurations = ((Double)configuration.get("nbOfConfigurations")).intValue();
			nbOfSimulationsPerConfiguration = ((Double)configuration.get("nbOfSimulationsPerConfiguration")).intValue();
			acoImpl = (ACOImpl) Class.forName("uav.navigation.aco." + configuration.get("implementation").toString() + "ACOImpl").newInstance();
		} catch(ClassCastException | NullPointerException ex) {
			throw new IllegalArgumentException("The experiment configuration file is not correctly formatted");
		} catch(InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
			throw new IllegalArgumentException("An invalid implementation was given.\nDetails: " + ex.getMessage());
		}
		output = "#UAVs: " + nbOfUAVs + "\nNb of configurations: " + nbOfConfigurations +
				"\nNb of simulations per configuration: " + nbOfSimulationsPerConfiguration + 
				"\nACO implementation: " + acoImpl.getClass().getSimpleName() +
				"\na\t\t\t\t\tb\t\t\t\t\tCoverage";
	}
	
	@Override
	public void execute() {
		System.out.println("Started Experiment to find good alpha and beta for ACO.");

		sim.initializeSLAs();
		long nbOfSteps = sim.getConfiguration().steps;
		firstStep = sim.getStepsInSLACycle()*2;
		lastStep = nbOfSteps + firstStep;
		sim.unsetCycleNavigation();

		List<Result> results = new ArrayList<Result>();
		for(int i = 0; i < nbOfConfigurations; i++) {
			//List<Pair<Double>> acoParameters = new ArrayList<>();
			Pair<Double> configuration = new Pair<Double>(sim.random.nextDouble(true, true)*15, sim.random.nextDouble(true, true)*15);
			
			//for(int u = 0; u < nbOfUAVs; u++)
				/*if (u%3 == 0)
					acoParameters.add(new Pair<Double>(12.64, 9.78));
				else if (u%3 == 1)
					acoParameters.add(new Pair<Double>(13.15, 1.0));
				else
					acoParameters.add(new Pair<Double>(1.0, 14.77));*/
				//acoParameters.add(configuration);
			//sim.setACOParameters(acoParameters);
			Object[] params = new Object[3];
			params[0] = configuration.first();
			params[1] = configuration.second();
			params[2] = acoImpl;
			sim.setNavigationBehaviour(new NavigationInitializationClass(ACONavigation.class, ACONavigation.getParamTypes(), params));
			System.out.println("\nTesting with configuration " + (i+1) + "/" + nbOfConfigurations + " " + configuration);
			double coverage = 0;
			for(int j = 0; j < nbOfSimulationsPerConfiguration; j++) {
				doSingleExperiment();
				System.out.println(sim.percentageFulfilledBetween(firstStep, lastStep));
				coverage += sim.percentageFulfilledBetween(firstStep, lastStep);
			}
			results.add(new Result(configuration, coverage/nbOfSimulationsPerConfiguration));
		}
		Collections.sort(results);
		System.out.println("a\t\t\tb\t\t\tCoverage");
		for(Result r : results) {
			String o = r.configuration.first() + "\t" + r.configuration.second() + "\t" + r.coverage;
			output += "\n" + o;
			System.out.println(o);
		}
		writeGraphs();
		System.out.println("\nFinished UAV Configuration experiment");
	}
	
	public class Result implements Comparable<Result>{
		public Pair<Double> configuration;
		public double coverage;
		public Result(Pair<Double> configuration, double coverage) {
			this.configuration = configuration;
			this.coverage = coverage;
		}
		public int compareTo(Result o) {
			return (int) ((o.coverage - coverage)*1000);
		}
		public String toString() {
			return "a" + configuration.first() + " b" + configuration.second() + "\t" + coverage + "%";
		}
	}
	
	private void doSingleExperiment() {
		sim.setNumUAVs(nbOfUAVs);
		sim.start();
		sim.addEvents(firstStep, lastStep);
		do {
			if (!sim.schedule.step(sim)) break;
		} while(sim.schedule.getSteps() < lastStep);
		sim.finish();		
	}
	
	private void writeGraphs() {
		super.prepareToWriteGraphs();
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getDirectoryName() + "/ACOConfigurations.txt"), "utf-8"))) {
		   writer.write(output);
		} catch (IOException e) {
			System.out.println("Error writing results: " + e.getMessage());
		}
	}


}
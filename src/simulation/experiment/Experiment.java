package simulation.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import simulation.UAVNetworkSimulation;

/**
 * An experiment contains some actions that will be done with the specified simulation class and will output some findings or results to the terminal and/or files.
 * 
 * @author Wietse Buseyne
 *
 */
public abstract class Experiment {
	
	protected UAVNetworkSimulation sim;
	
	public Experiment(UAVNetworkSimulation sim) {
		this.sim = sim;
	}
	
	protected String getDirectoryName() { 
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		return this.getClass().getSimpleName() + dateFormat.format(date);
	}
	
	
	protected void prepareToWriteGraphs() {
		String directoryName = getDirectoryName();
		new File(directoryName).mkdirs();

		try {
			Files.deleteIfExists(new File(directoryName + "/configuration.json").toPath());
			Files.copy(new File("configuration.json").toPath(), new File(directoryName + "/configuration.json").toPath());
			Files.deleteIfExists(new File(directoryName + "/experiments.json").toPath());
			Files.copy(new File("experiments.json").toPath(), new File(directoryName + "/experiments.json").toPath());
		} catch (IOException e) {
			System.out.println("Error writing configuration files: " + e.getMessage());
		}
	}

	public abstract void execute();
		
	protected void executeUntil(long firstStep, long lastStep) {
		sim.setJob(sim.job()+1);
		sim.start();
		sim.addEvents(firstStep, lastStep);
		do {
			if (!sim.schedule.step(sim)) break;
		} while(sim.schedule.getSteps() < lastStep);
		sim.finish();
		System.out.println("Gathering simulation results");
				
		//sim.slaChecker.addToDataset(firstStep, lastStep, sim.getNumUAVs());
	}
	
	protected void findUAVsNeeded(long firstStep, long lastStep) {
		//sim.slaChecker.clearDatasets();
		sim.initializeNumUAVs();

		double percentageFulfilled = 0;
		boolean finished = false;
		sim.nameThread();
		
		do {
			System.out.println("Simulating with " + sim.getNumUAVs() + " UAV(s)...");
			executeUntil(firstStep, lastStep);
			
			percentageFulfilled = sim.slaChecker.percentageFulfilledBetween(firstStep, lastStep);
			System.out.printf("%.2f%% of SLAs fulfilled\n", percentageFulfilled);
			
			finished = sim.SLAsFulfilled(firstStep, lastStep);
			//check for next number of UAVs to test
			int multiplier = (int) (100/percentageFulfilled);
			if(!finished)
				if(sim.getNumUAVs()*multiplier > sim.getNumUAVs()+sim.getConfiguration().numUAVsSteps)
					sim.setNumUAVs(sim.getNumUAVs() * multiplier);
				else
					sim.setNumUAVs(sim.getNumUAVs() + sim.getConfiguration().numUAVsSteps);
			
		} while(!finished);
		System.out.println("Generating graphs...");
		System.out.println("Lowest SLA coverage: " +
				sim.slaChecker.lowestPercentage(firstStep, lastStep) + "%");
		//sim.slaChecker.clearDatasets();
	}
	
}

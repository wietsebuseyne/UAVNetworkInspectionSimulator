package simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sim.display.Console;
import simulation.experiment.Experiment;
import util.parsers.configuration.ExperimentParser;
import util.parsers.configuration.UAVNavigationClassDeserialiser;

import com.google.gson.JsonSyntaxException;

/**
 * A class that can execute multiple experiments after each other. 
 * Can be run as a main and will then try to initialize an ExperimentConductor with the configuration.json and experiments.json configuration files and execute the experiments.
 * 
 * @author Wietse Buseyne
 *
 */
public class ExperimentConductor {
	
	//private UAVNetworkSimulation sim;
	private List<Experiment> experiments = new ArrayList<>();
	
	public ExperimentConductor(List<Experiment> experiments) {
		//this.sim = simulation;
		this.experiments.addAll(experiments);
	}
	
	public void runExperiments() {
		for(Experiment e : experiments)
			e.execute();
	}
	
	public static void main(String[] args) {
        CommandLine commandLine = null;
        //Option option_A = Option.builder("gui").argName("gui").desc("Start the simulator with the GUI");
        //Option option_r = OptionBuilder.withArgName("opt1").hasArg().withDescription("The r option").create("r");
        //Option option_S = OptionBuilder.withArgName("opt2").hasArg().withDescription("The S option").create("S");
        Option option_gui = new Option("g", "gui", false, "Start the GUI of the simulator.");
        Option option_nav = Option.builder("n").longOpt("nav").argName("Navigation strategy").desc("Sets the navigation strategy to use").hasArg().build();
        Option option_uavs = Option.builder("u").longOpt("uavs").argName("NbOfUAVs").desc("Sets the number of UAVs to use").hasArg().build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        
        options.addOption(option_gui).addOption(option_nav).addOption(option_uavs);
        
        try {
			commandLine = parser.parse(options, args);
		} catch (ParseException ex) {
            System.out.print("Argument parse error: ");
            System.out.println(ex.getMessage());
            System.exit(-1);
		}
			    
		if(!commandLine.hasOption("gui")) {
			UAVNetworkSimulation sim = null;
			try {
				sim = new UAVNetworkSimulation(System.currentTimeMillis());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error: " + e.getMessage());
				System.exit(-1);
			}
			
			ExperimentParser experimentParser = new ExperimentParser(sim, "experiments.json");
			try {
				ExperimentConductor conductor = new ExperimentConductor(experimentParser.parse());
				conductor.runExperiments();
			} catch (IOException | IllegalArgumentException | JsonSyntaxException e) {
				System.out.println("Error: " + e.getMessage());
				System.exit(-1);
			}
		} else {
			UAVNetworkSimulationGUI vid = null;
			try {
				vid = new UAVNetworkSimulationGUI();
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
				System.exit(-1);
			}
			if(commandLine.hasOption('n'))
				if(commandLine.getOptionValue('n').equals("CycleNavigation")) {
					System.out.println("Warning: using the CycleNavigation in large networks might take a very long time to instantiate!");
					((UAVNetworkSimulation)vid.state).initializeCycleNavigation();
				} else
					((UAVNetworkSimulation)vid.state).setNavigationBehaviour(UAVNavigationClassDeserialiser.deserialize(commandLine.getOptionValue('n')));
			if(commandLine.hasOption('u'))
				((UAVNetworkSimulation)vid.state).setNumUAVs(Integer.parseInt(commandLine.getOptionValue('u')));
			Console c = new Console(vid);
			c.setVisible(true);
		}
	}

}

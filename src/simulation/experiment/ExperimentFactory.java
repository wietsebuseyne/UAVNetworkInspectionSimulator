package simulation.experiment;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import simulation.UAVNetworkSimulation;

/**
 * A factory class that will create an experiment based on a className and a configuration map. 
 * @author Wietse Buseyne
 *
 */
public class ExperimentFactory {
	
	public static Experiment createExperiment(UAVNetworkSimulation sim, String className, Map<String, Object> configuration) {
		Class<?>[] params; 
		if(configuration == null || configuration.isEmpty()) {
			params = new Class<?>[1];
			params[0] = UAVNetworkSimulation.class;
		} else {
			params = new Class<?>[2];
			params[0] = UAVNetworkSimulation.class;
			params[1] = Map.class;
		}
		Experiment e;
		try {
			if(configuration == null || configuration.isEmpty())
				e = (Experiment) Class.forName("simulation.experiment." + className).getConstructor(params).newInstance(sim);
			else
				e = (Experiment) Class.forName("simulation.experiment." + className).getConstructor(params).newInstance(sim, configuration);
		} catch (InstantiationException
				| IllegalAccessException
				| NoSuchMethodException 
				| SecurityException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException("'" + className + "' could not be initialized with the given configuration file.");
		} catch (ClassCastException | ClassNotFoundException ex) {
			throw new IllegalArgumentException("The experiment with the name '" + className + "' could not be found.");
		} catch (InvocationTargetException ex) {
			throw new IllegalArgumentException(ex.getTargetException().getMessage());
		}
		return e;
	}

}

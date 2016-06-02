package util.parsers.configuration;

import java.lang.reflect.Type;
import java.util.Map;

import simulation.UAVNetworkSimulation;
import simulation.experiment.Experiment;
import simulation.experiment.ExperimentFactory;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * A class that can deserialize experiments.
 * 
 * @author Wietse Buseyne
 *
 */
public class ExperimentDeserializer implements JsonDeserializer<Experiment> {

	private UAVNetworkSimulation sim;
	
	public ExperimentDeserializer(UAVNetworkSimulation sim) {
		this.sim = sim;
	}

	@Override
	public Experiment deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> conf = new Gson().fromJson(obj.get("configuration"), type);
		return ExperimentFactory.createExperiment(sim, 
				obj.get("name").getAsString(),
				conf);
	}

}

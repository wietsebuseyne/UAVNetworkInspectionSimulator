package util.parsers.configuration;

import java.io.IOException;
import java.util.List;

import simulation.UAVNetworkSimulation;
import simulation.experiment.Experiment;
import util.parsers.JsonParser;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ExperimentParser extends JsonParser<List<Experiment>> {

	public ExperimentParser(UAVNetworkSimulation sim, String filename) {
		super(filename);
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Experiment.class, new ExperimentDeserializer(sim));
		setGson(gsonBuilder.create());
	}

	@Override
	public List<Experiment> parse() throws IOException {
		return getGson().fromJson(getBufferedReader(), new TypeToken<List<Experiment>>(){}.getType());
	}


}

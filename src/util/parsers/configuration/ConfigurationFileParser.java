package util.parsers.configuration;

import java.io.IOException;

import util.parsers.JsonParser;

/**
 * A JSON parser for the configuration file.
 * 
 * @author Wietse Buseyne
 *
 */
public class ConfigurationFileParser extends JsonParser<Configuration> {
		
	public ConfigurationFileParser(String filename) {
		super(filename);
	}
	
	public Configuration parse() throws IOException {
		return getGson().fromJson(getBufferedReader(), Configuration.class);
	}

}

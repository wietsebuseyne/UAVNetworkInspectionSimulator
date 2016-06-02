package util.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A generic and abstract parser class that is meant to be used to write to and read from JSON files. 
 * 
 * @author Wietse Buseyne
 *
 * @param <T> The class that will be parsed from and/or written to a JSON file.
 */
public abstract class JsonParser<T> {

	private Gson gson;
	private String filename;
	
	public JsonParser(String filename) {
		this.filename = filename;
		GsonBuilder gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}
	
	protected void setGson(Gson gson) {
		if(gson == null)
			throw new IllegalArgumentException("The gson argument cannot be null");
		this.gson = gson;
	}
	
	protected Gson getGson() {
		return gson;
	}
	
	protected BufferedReader getBufferedReader() throws FileNotFoundException {
		return new BufferedReader(new FileReader(filename));
	}
	
	public abstract T parse() throws IOException;
	
	public void writeToFile(T t) {
		try (Writer writer = new FileWriter(filename)){  
			writer.write(gson.toJson(t));  
			writer.close();
		} catch (IOException e) {} 
	}
	
	public String getFilename() {
		return filename;
	}

}

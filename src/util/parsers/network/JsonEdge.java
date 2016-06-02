package util.parsers.network;

/**
 * A class that represents an edge in a representation that can be easily used to write to or read from JSON.
 * 
 * @author Wietse Buseyne
 *
 */
public class JsonEdge {

	public int source, target;
	//public double weight;
	public double riskLevelMultiplier = 1;

	public JsonEdge(int source, int target, double riskLevelMultiplier) {
		this.source = source;
		this.target = target;
		//this.weight = weight;
		this.riskLevelMultiplier = riskLevelMultiplier;
	}
	
	public JsonEdge(int source, int target) {
		this(source, target, 0);
	}

	/*public int compareTo(JsonEdge that) {
        if      (this.weight < that.weight) return -1;
        else if (this.weight > that.weight) return +1;
        else                                return  0;
	}*/

}

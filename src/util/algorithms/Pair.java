package util.algorithms;

/**
 * Holds a pair of P, null values are not allowed.
 * 
 * @author Wietse Buseyne
 *
 * @param <P> The class of the two values in the pair.
 */
public class Pair<P> {
	
	private P first, second;
	
	public Pair(P first, P second) {
		if(first == null || second == null)
			throw new IllegalArgumentException("No null allowed in pairs.");
		this.first = first;
		this.second = second;
	}
	
	public P first() {
		return first;
	}
	
	public P second() {
		return second;
	}
	
	public boolean contains(P p) {
		return first.equals(p) || second.equals(p);
	}
	
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
	
}

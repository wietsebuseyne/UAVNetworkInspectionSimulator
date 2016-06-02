package util.algorithms.dijkstra;

public class Position {
	
	private double x, y;

	public Position(double x, double y) {
		super();
		setX(x);
		setY(y);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public double distanceTo(Position other) {
		return Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y, 2));
	}

}

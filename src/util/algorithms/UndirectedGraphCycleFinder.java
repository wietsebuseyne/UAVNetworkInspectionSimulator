package util.algorithms;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.princeton.cs.algs4.EulerianCycle;
import edu.princeton.cs.algs4.Graph;

/**
 * Class for finding a cycle in an undirected graph that tries to approximate the optimal Chinese Postman tour.
 * 
 * Based on the CPP class by Harold Thimbleby for directed graphs, see <a href="https://www3.cs.stonybrook.edu/~algorith/implement/cpp/distrib/CPP.java">https://www3.cs.stonybrook.edu/~algorith/implement/cpp/distrib/CPP.java</a>.
 *
 * @author Harold Thimbleby, 2001, 2, 3
 * @author Wietse Buseyne, 2016
 */

@SuppressWarnings("rawtypes")
public class UndirectedGraphCycleFinder {	
	
	int N; // number of vertices
	int delta[]; // deltas of vertices
	int neg[], pos[]; // unbalanced vertices
	List<Integer> odd = new ArrayList<Integer>();
	int arcs[][]; // adjacency matrix, counts arcs between vertices
	int f[][]; // repeated arcs in CPT
	float c[][]; // costs of cheapest arcs or paths
	Vector label[][]; // vectors of labels of arcs (for each vertex pair) 
	String cheapestLabel[][]; // labels of cheapest arcs
	boolean defined[][]; // whether path cost is defined between vertices
	int path[][]; // spanning tree of the graph
	float basicCost; // total cost of traversing each arc once
	Graph graph;
	List<Integer> cycle = null;
	
	
	public UndirectedGraphCycleFinder(int vertices) {
		if((N = vertices) <= 0)
			throw new IllegalArgumentException("The number of vertices must be strictly positivey");
		delta = new int[N];
		defined = new boolean[N][N];
		label = new Vector[N][N];
		c = new float[N][N];
		f = new int[N][N];
		arcs = new int[N][N];
		cheapestLabel = new String[N][N];
		path = new int[N][N];
		basicCost = 0;
		graph = new Graph(vertices);
	}
	
	public void solve() 	{	
		leastCostPaths();
		checkValid();
		findOdd();
		for(Pair<Integer> p : getRepeatedEdges()) {
			graph.addEdge(p.first(), p.second());
		}
        EulerianCycle eulerianCycle = new EulerianCycle(graph);
        cycle = new ArrayList<Integer>();
        for(Integer i : eulerianCycle.cycle())
        	cycle.add(i);
        cycle.remove(cycle.size()-1);
	}
	
	public List<Integer> getCycle() {
        return cycle;
	}
	
	public List<Pair<Integer>> getRepeatedEdges() {
		List<Pair<Integer>> repeatedEdges = new ArrayList<Pair<Integer>>();
		for(Pair<Integer> pair : getPairing()) {
			int  u = pair.first(), v = pair.second();
			for( int p; u != v; u = p ) { // break down path into its arcs
				p = path[u][v];
				repeatedEdges.add(new Pair<Integer>(u, p));
			}
		}
		return repeatedEdges;
	}
	
	public void findOdd() {
		for(int i = 0; i < N; i++) // initialise sets
			if(delta[i] %2 != 0)
				odd.add(i);
	}
	
	/**
	 * Returns a pairing of all the odd edges.
	 * If this method is be optimized to return the optimal pairing, this class would always find the optimal cycle as well.
	 * @return A list of pairs of the odd edges.
	 */
	private  List<Pair<Integer>> getPairing() {
		List<Pair<Integer>> bestPairing = new ArrayList<Pair<Integer>>();
		for(int i = 0; i < odd.size()-1; i+=2)
			bestPairing.add(new Pair<Integer>(odd.get(i), odd.get(i+1)));
		return bestPairing;
	}
	
	@SuppressWarnings("unchecked")
	public UndirectedGraphCycleFinder addArc(String lab, int u, int v, float cost){	
		if( !defined[u][v] ) label[u][v] = new Vector();
	
		if( !defined[v][u] ) label[v][u] = new Vector();
		
		label[u][v].addElement(lab); 
		
		label[v][u].addElement(lab+"'"); 
		
		basicCost += cost;
		if( !defined[u][v] || c[u][v] > cost )
		{	c[u][v] = cost;
			cheapestLabel[u][v] = lab;
			defined[u][v] = true;
			path[u][v] = v;
			
			c[v][u] = cost;
			cheapestLabel[v][u] = lab+"'";
			defined[v][u] = true;
			path[v][u] = u;
		}
		arcs[u][v]++;
		delta[u]++;

		arcs[v][u]++;
		delta[v]++;
		
		graph.addEdge(u, v);
		return this;
	}
	
	/** Floyd-Warshall algorithm
	 *  Assumes no negative self-cycles.
	 *  Finds least cost paths or terminates on finding any non-trivial negative cycle.
	 */
	private void leastCostPaths() {	
		for( int k = 0; k < N; k++ )
			for( int i = 0; i < N; i++ )
				if( defined[i][k] )
					for( int j = 0; j < N; j++ )
						if( defined[k][j]
						    && (!defined[i][j] || c[i][j] > c[i][k]+c[k][j]) )
						{	path[i][j] = path[i][k];
							c[i][j] = c[i][k]+c[k][j];
							defined[i][j] = true;
							if( i == j && c[i][j] < 0 ) return; // stop on negative cycle
						}
	}

	private void checkValid() {	
		for( int i = 0; i < N; i++ ) {	
			for( int j = 0; j < N; j++ )
				if( !defined[i][j] ) throw new Error("Graph is not strongly connected");
			if( c[i][i] < 0 ) throw new Error("Graph has a negative cycle");
		}
	}
	
}
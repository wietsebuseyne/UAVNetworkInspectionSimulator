package util.algorithms.dijkstra;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDijkstra {
	
	private Vertex v00, v01, v10, v11;
	private Vertex w00, w01, w10, w11;
	private Vertex g00, g01, g02,
					g10, g11, g12,
					g20, g21, g22;
	
	@BeforeClass
	public static void setUpImmutableFixture() {
	}
	
	@Before
	public void setUpMutableFixture() {
		v00 = new Vertex(new Position(0,0), null);
		v01 = new Vertex(new Position(0,1), null);
		v10 = new Vertex(new Position(1,0), null);
		v11 = new Vertex(new Position(1,1), null);
		
		v00.addBidirectionalEdge(new Edge(v01, 10));
		v00.addBidirectionalEdge(new Edge(v10, 5));
		v00.addBidirectionalEdge(new Edge(v11, 7));
		
		v01.addBidirectionalEdge(new Edge(v10, 3));
		v01.addBidirectionalEdge(new Edge(v11, 4));
		
		v10.addBidirectionalEdge(new Edge(v11, 8));
		
		
		w00 = new Vertex(new Position(0,0), null);
		w01 = new Vertex(new Position(0,1), null);
		w10 = new Vertex(new Position(1,0), null);
		w11 = new Vertex(new Position(1,1), null);
		
		w00.addBidirectionalEdge(new Edge(w01, 3));
		w00.addBidirectionalEdge(new Edge(w10, 2));
		w00.addBidirectionalEdge(new Edge(w11, 5));
		
		w01.addBidirectionalEdge(new Edge(w10, 5));
		w01.addBidirectionalEdge(new Edge(w11, 2));
		
		w10.addBidirectionalEdge(new Edge(w11, 3));
		
		
		g00 = new Vertex(new Position(0,0), null);
		g01 = new Vertex(new Position(0,1), null);
		g02 = new Vertex(new Position(0,2), null);
		
		g10 = new Vertex(new Position(1,0), null);
		g11 = new Vertex(new Position(1,1), null);
		g12 = new Vertex(new Position(1,2), null);
		
		g20 = new Vertex(new Position(2,0), null);
		g21 = new Vertex(new Position(2,1), null);
		g22 = new Vertex(new Position(2,2), null);
		
		g01.addEdge(new Edge(g00, 49));
		g10.addEdge(new Edge(g00, 49));
		g11.addEdge(new Edge(g00, 49));
		
		g00.addEdge(new Edge(g10, 1));
		g20.addEdge(new Edge(g10, 1));
		g01.addEdge(new Edge(g10, 1));
		g11.addEdge(new Edge(g10, 1));
		g21.addEdge(new Edge(g10, 1));
		
		g10.addEdge(new Edge(g20, 1));
		g11.addEdge(new Edge(g20, 1));
		g12.addEdge(new Edge(g20, 1));
		
		g00.addEdge(new Edge(g01, 4));
		g10.addEdge(new Edge(g01, 4));
		g11.addEdge(new Edge(g01, 4));
		g02.addEdge(new Edge(g01, 4));
		g12.addEdge(new Edge(g01, 4));
		
		g00.addEdge(new Edge(g11, 64));
		g10.addEdge(new Edge(g11, 64));
		g20.addEdge(new Edge(g11, 64));
		g01.addEdge(new Edge(g11, 64));
		g21.addEdge(new Edge(g11, 64));
		g02.addEdge(new Edge(g11, 64));
		g12.addEdge(new Edge(g11, 64));
		g22.addEdge(new Edge(g11, 64));
		
		g10.addEdge(new Edge(g21, 25));
		g20.addEdge(new Edge(g21, 25));
		g11.addEdge(new Edge(g21, 25));
		g12.addEdge(new Edge(g21, 25));
		g22.addEdge(new Edge(g21, 25));
		
		g01.addEdge(new Edge(g02, 1));
		g11.addEdge(new Edge(g02, 1));
		g12.addEdge(new Edge(g02, 1));
		
		g02.addEdge(new Edge(g12, 64));
		g01.addEdge(new Edge(g12, 64));
		g11.addEdge(new Edge(g12, 64));
		g21.addEdge(new Edge(g12, 64));
		g22.addEdge(new Edge(g12, 64));
		
		g12.addEdge(new Edge(g22, 64));
		g11.addEdge(new Edge(g22, 64));
		g21.addEdge(new Edge(g22, 64));
	}
	
	@Test
	public void shortestPath_baseCase1() {
		Dijkstra dijkstra = new Dijkstra(v00);
		List<Vertex> vertices = dijkstra.getShortestPath(v10);
		assertEquals(2, vertices.size());
		assertEquals(v00, vertices.get(0));
		assertEquals(v10, vertices.get(1));
	}

	@Test
	public void shortestPath_baseCase2() {
		Dijkstra dijkstra = new Dijkstra(v01);
		List<Vertex> vertices = dijkstra.getShortestPath(v00);
		assertEquals(3, vertices.size());
		assertEquals(v01, vertices.get(0));
		assertEquals(v10, vertices.get(1));
		assertEquals(v00, vertices.get(2));
	}

	
	@Test
	public void shortestPath_baseCase3() {
		Dijkstra dijkstra = new Dijkstra(v10);
		List<Vertex> vertices = dijkstra.getShortestPath(v11);
		assertEquals(3, vertices.size());
		assertEquals(v10, vertices.get(0));
		assertEquals(v01, vertices.get(1));
		assertEquals(v11, vertices.get(2));
	}

	
	@Test
	public void shortestPath_baseCase4() {
		Dijkstra dijkstra = new Dijkstra(g00);
		List<Vertex> vertices = dijkstra.getShortestPath(g22);
		assertEquals(4, vertices.size());
		assertEquals(g00, vertices.get(0));
		assertEquals(g10, vertices.get(1));
		assertEquals(g21, vertices.get(2));
		assertEquals(g22, vertices.get(3));
	}
	
	@Test
	public void shortestPath_diagonal() {
		Dijkstra dijkstra = new Dijkstra(w00);
		List<Vertex> vertices = dijkstra.getShortestPath(w11);
		assertEquals(2, vertices.size());
		assertEquals(w00, vertices.get(0));
		assertEquals(w11, vertices.get(1));
	}

}

package sla.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;

import util.boxandwhisker.ExtendedBoxAndWhiskerRenderer;

/**
 * Provides some standard methods for getting graphs from the SLA manager.
 * 
 * @author Wietse Buseyne
 *
 */
public class SLAReporter {

	private SLAManager manager;
	private DefaultCategoryDataset avgResponseTimesDS = new DefaultCategoryDataset();
	private DefaultBoxAndWhiskerCategoryDataset boxDS = new DefaultBoxAndWhiskerCategoryDataset();
	private DefaultBoxAndWhiskerCategoryDataset slaBoxDS = new DefaultBoxAndWhiskerCategoryDataset();

	public SLAReporter(SLAManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("The manager cannot be null");
		}
		this.manager = manager;
	}

	/**
	 * Adds the current response times to a dataset with the given nb of UAVs and strategy, so that graphs can be generated later on.
	 * @param nbOfUAVs The number of UAVs that was used during this simulation
	 * @param strategy A string representing the navigation strategy that was used during the simulation
	 */
	public void addToDatasets(int nbOfUAVs, String strategy) {
		avgResponseTimesDS.addValue(manager.getAverageResponseTime(), strategy,
				nbOfUAVs + "");
	}
	
	/**
	 * Adds response time and SLA coverage data to the boxplot datasets so that graphs can be generated later on.
	 * This method should ideally only be used with one number of UAVs. If not, the generated graphs will not be easily readable.
	 * @param nbOfUAVs The number of UAVs that was used during this simulation
	 * @param strategy A string representing the navigation strategy that was used during the simulation
	 * @param firstStep The first step of which data should be added to the datasets
	 * @param lastStep The last step of which data should be added to the datasets
	 */
	public void addToBoxDatasets(int nbOfUAVs, String strategy, long firstStep, long lastStep) {
		boxDS.add(manager.getResponseTimes(), strategy, nbOfUAVs + "");
		slaBoxDS.add(
				Arrays.stream(manager.getSLAComplianceData(firstStep, lastStep)).boxed().collect(Collectors.toList()),
				strategy, "SLA Coverages");
		slaBoxDS.add(
				Arrays.stream(manager.getTimeComplianceData(firstStep, lastStep)).boxed().collect(Collectors.toList()),
				strategy, "Coverage over time");
	}

	/**
	 * Returns an object representing a boxplot graph of the response times.
	 * @return An object representing a boxplot graph of the response times.
	 */
	public JFreeChart getResponseTimesBoxplot() {
		return createBoxPlot(boxDS, "Response times", "#UAVs", "Time (m)", manager.getResponseTimeGoal());
	}

	/**
	 * Returns an object representing a boxplot graph of the coverages.
	 * @return An object representing a boxplot graph of the coverages
	 */
	public JFreeChart getCoverageBoxplot() {
		return createBoxPlot(slaBoxDS, "Coverage spreads", "", "SLA Coverage (%)", 0);
	}
	
	private JFreeChart createBoxPlot(DefaultBoxAndWhiskerCategoryDataset ds, String title, String x, String y, double goal) {
		ExtendedBoxAndWhiskerRenderer renderer = new ExtendedBoxAndWhiskerRenderer();
		renderer.setFillBox(false);
		//renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesPaint(3, Color.BLACK);
		renderer.setUseOutlinePaintForWhiskers(true);
		renderer.setOutlierPaint(Color.BLACK);
		renderer.setFaroutPaint(Color.BLACK);

		final CategoryAxis xAxis = new CategoryAxis(x);
		final NumberAxis yAxis = new NumberAxis(y);
		final CategoryPlot plot = new CategoryPlot(ds, xAxis, yAxis,
				renderer);

		if(goal > 0) {
			Marker goalMarker = new ValueMarker(goal,
					Color.BLACK, new BasicStroke(2));
			goalMarker.setLabelAnchor(RectangleAnchor.RIGHT);
			goalMarker.setLabelOffset(new RectangleInsets(15, 0, 0, 10));
			goalMarker.setLabel("Goal");
			plot.addRangeMarker(goalMarker);
		}

		final JFreeChart chart = new JFreeChart(title, new Font("SansSerif",
				Font.BOLD, 14), plot, true);

		return chart;
	}

	/**
	 * Creates and returns an object representing a linechart of the average response time per number of UAVs used.
	 * @return An object representing a linechart of the average response time per number of UAVs used.
	 */
	public JFreeChart getAverageResponseTimesGraph() {
		JFreeChart chart = ChartFactory.createLineChart("Response times",
				"#UAVs", "Response times", avgResponseTimesDS);
		CategoryPlot plot = chart.getCategoryPlot();
		Marker goal = new ValueMarker(manager.getResponseTimeGoal(),
				Color.BLACK, new BasicStroke(2));
		goal.setLabelAnchor(RectangleAnchor.RIGHT);
		goal.setLabelOffset(new RectangleInsets(15, 0, 0, 10));
		goal.setLabel("Goal");
		plot.addRangeMarker(goal);
		return chart;
	}

	/**
	 * Creates and returns an object representing a barchart of the response time.
	 * @return An object representing a barchart of the average response time.
	 */
	public JFreeChart getResponseTimeGraph() {
		DefaultCategoryDataset responseTimeDS = new DefaultCategoryDataset();
		int i = 1;
		for (Long l : manager.getResponseTimes()) {
			responseTimeDS.addValue(l, (i++) + "", "");
		}
		JFreeChart chart = ChartFactory.createBarChart("Response times",
				"Case", "Response times", responseTimeDS);
		CategoryPlot plot = chart.getCategoryPlot();
		Marker avg = new ValueMarker(manager.getAverageResponseTime()), goal = new ValueMarker(
				manager.getResponseTimeGoal());
		avg.setLabel("Average");
		goal.setLabel("Goal");
		plot.addRangeMarker(avg);
		plot.addRangeMarker(goal);
		return chart;
	}

	/**
	 * Removes all gathered data from the datasets.
	 */
	public void clear() {
		avgResponseTimesDS.clear();
		boxDS.clear();
		slaBoxDS.clear();
	}

}

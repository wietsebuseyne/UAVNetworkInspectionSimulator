package simulation.experiment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;

import simulation.UAVNetworkSimulation;
import uav.UAVConfiguration;

import com.google.gson.internal.LinkedTreeMap;

/**
 * An experiment to compare one or more strategies with different kind of UAVs. 
 * 
 * @author Wietse Buseyne
 *
 */
public class UAVConfigurationExperiment extends NbOfUAVsExperiment {

	private List<UAVConfiguration> configurations = new ArrayList<UAVConfiguration>();
	private boolean minUAVsGraph = false, SLACoverage_time = false;
	
	private List<XYSeries> totalCoverageXYs = new ArrayList<XYSeries>();
	private List<XYSeriesCollection> totalCoverages = new ArrayList<XYSeriesCollection>();
	private List<String> navNames = new ArrayList<String>();
	private List<XYSeries> lowestSLAxys = new ArrayList<XYSeries>();
	private DefaultCategoryDataset minUAVsDS = new DefaultCategoryDataset();

	@SuppressWarnings("unchecked")
	public UAVConfigurationExperiment(UAVNetworkSimulation sim, Map<String, Object> configuration) {
		super(sim, configuration);
		try {
			List<LinkedTreeMap<String, Double>> confs = (List<LinkedTreeMap<String, Double>>) configuration.get("uavConfigurations");
			
			for(LinkedTreeMap<String, Double> conf : confs) {
				UAVConfiguration c = new UAVConfiguration();
				c.rechargeTime = conf.get("rechargeTime").longValue();
				c.broadcastRadius = conf.get("broadcastRadius");
				c.batteryLife = conf.get("batteryLife").longValue();
				c.speedKmHour = conf.get("speedKmHour");
				configurations.add(c);
			}
		} catch(ClassCastException | NullPointerException ex) {
			throw new IllegalArgumentException(
					"The experiment configuration is not correctly formatted. "
					+ "The uav configurations should have contain the following structure (with possibly multiple configurations defined):\n"
					+ "'uavConfigurations':["
					+ "\n\t{"
					+ "\n\t\t'rechargeTime':long,"
					+ "\n\t\t'broadcastRadius':double,"
					+ "\n\t\t'speedKmHour':double,"
					+ "\n\t\t'batteryLife':long"
					+ "\n\t}"
					+ "\n]");
		}
	}
	
	@Override
	protected void prepareCycleDatasets() { }

	@Override
	protected void prepareStrategyDatasets(String navigationName) { }
	
	@Override
	protected void doSingleExperiment() {
		int i = 1;
		for(UAVConfiguration conf : configurations) {
			System.out.println("\nTesting with configuration " + i);
			sim.setUAVConfiguration(conf);
			String name = sim.getCurrentNavigationName();
			if(configurations.size() > 1)
				name += i;
			totalCoverageXYs.add(new XYSeries(name));
			lowestSLAxys.add(new XYSeries(name));
			experiment(i);
			i++;
		}

	}
	
	private void experiment(int configurationId) {
		int nb = minNbOfUAVs;
		XYSeriesCollection totalCoverageDS = new XYSeriesCollection();
		boolean slasOk = false;
		do {
			runSimulation(nb);
			
			if(SLACoverage_time) {
				XYSeries xySeries = new XYSeries("UAVs: " + nb);
				double[] pfd = sim.getSlaChecker().getTimeComplianceData(firstStep, lastStep);
				long stepSize = (lastStep-firstStep) / sim.getSlaChecker().getNbOfDataPoints();
				for(int i = 0; i < pfd.length; i++) {
					xySeries.add(i * stepSize, pfd[i]);
				}
				totalCoverageDS.addSeries(xySeries);
			}
			lowestSLAxys.get(lowestSLAxys.size()-1).add(nb, sim.slaChecker.lowestPercentage(firstStep, lastStep));
			totalCoverageXYs.get(totalCoverageXYs.size()-1).add(nb, sim.getSlaChecker().percentageFulfilledBetween(firstStep, lastStep));
			
			if(minUAVsGraph && !slasOk && sim.allSLAsFulfilled(firstStep, lastStep)) {
				minUAVsDS.addValue(nb, "conf" + configurationId, sim.getNavigationBehaviourClass().getSimpleName());
				slasOk = true;
			}
			nb += uavStep;
		} while (nb <= maxNbOfUAVs);
		if(SLACoverage_time)
			totalCoverages.add(totalCoverageDS);
		navNames.add(sim.getNavigationBehaviourClass().getSimpleName());
	}
	
	@Override
	protected void writeGraphs() {
		String directoryName = getDirectoryName();
		super.prepareToWriteGraphs();
		JFreeChart chart;
        
        FileOutputStream fout;
		try {
			XYSeriesCollection totalCoverage = new XYSeriesCollection();
			for(int i = 0; i < totalCoverageXYs.size(); i++) {
				XYSeriesCollection xySeries = new XYSeriesCollection();
				xySeries.addSeries(totalCoverageXYs.get(i));
				totalCoverage.addSeries(totalCoverageXYs.get(i));
			}
			fout = new FileOutputStream(new File(directoryName + "/SLACoverage_total.png"));
			chart = ChartFactory.createXYLineChart("Total SLA Coverage", "#UAVs", "%", totalCoverage);
			XYPlot plot = chart.getXYPlot();
			Marker goal = new ValueMarker(sim.getConfiguration().averageCoverageGoal);
			goal.setLabelAnchor(RectangleAnchor.RIGHT);
			goal.setLabelOffset(new RectangleInsets(15, 0, 0, 10));
			goal.setLabel("Goal");
			plot.addRangeMarker(goal);
			ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);

			XYSeriesCollection xyNo = new XYSeriesCollection();
			for(int i = 0; i < lowestSLAxys.size(); i++) {
				xyNo.addSeries(lowestSLAxys.get(i));
			}
			
			fout = new FileOutputStream(new File(directoryName + "/SLACoverage_lowest.png"));
			chart = ChartFactory.createXYLineChart("Lowest SLA Coverage", "#UAVs", "%", xyNo);
			plot = chart.getXYPlot();
			goal = new ValueMarker(sim.getConfiguration().coveragePerSLAGoal);
			goal.setLabelAnchor(RectangleAnchor.RIGHT);
			goal.setLabelOffset(new RectangleInsets(15, 0, 0, 10));
			goal.setLabel("Goal");
			plot.addRangeMarker(goal);
			ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
			
			if(SLACoverage_time) 
				for(int i = 0; i < totalCoverages.size(); i++) {
					fout = new FileOutputStream(new File(directoryName + "/SLACoverage_time" + String.format("%02d", i+1) + navNames.get(i) + ".png"));
					chart = ChartFactory.createXYLineChart("SLA coverage over time: " + navNames.get(i), "Step", "% of SLAs fulfilled", totalCoverages.get(i));
					plot = chart.getXYPlot();
					((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
					plot.getRangeAxis().setUpperBound(100);
					((NumberAxis)plot.getRangeAxis()).setAutoRangeMinimumSize(10);
					ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
				}
			
			if(minUAVsGraph) {
				chart = ChartFactory.createBarChart("Min #UAVs to fulfill SLAs", "Configuration", "#UAVs", minUAVsDS);
				fout = new FileOutputStream(new File(directoryName + "/minUAVsSLAsFulfilled.png"));
				ChartUtilities.writeChartAsPNG(fout, chart, Math.max(200, 80 + (20 * minUAVsDS.getColumnCount() * minUAVsDS.getRowCount())), 400);
			}
			
			fout = new FileOutputStream(new File(directoryName + "/responseTime.png"));
			ChartUtilities.writeChartAsPNG(fout, sim.reporter.getAverageResponseTimesGraph(), 600, 400);
		} catch (IOException e) {
			System.out.println("Error writing chart: " + e.getMessage());
		}
	}


}
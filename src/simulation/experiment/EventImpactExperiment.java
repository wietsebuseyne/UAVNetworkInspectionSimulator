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
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import simulation.UAVNetworkSimulation;

/**
 * An experiment that measures the impact of adding events to the simulation.
 * All simulations will be done twice: once with and once without events.
 * The events that are added will be the ones specified in the configuration.json file.
 * 
 * @author Wietse Buseyne
 *
 */
public class EventImpactExperiment extends NbOfUAVsExperiment {
	
	private DefaultCategoryDataset coverageDropDS = new DefaultCategoryDataset();
	private List<XYSeries> totalCoverageXYs = new ArrayList<XYSeries>();
	private List<XYSeriesCollection> totalCoverages = new ArrayList<XYSeriesCollection>();
	private List<String> navNames = new ArrayList<String>();
	private List<XYSeries> lowestSLAxys = new ArrayList<XYSeries>();
	private DefaultCategoryDataset minUAVsDS = new DefaultCategoryDataset();
	
	private boolean minUAVsGraph = true;
	
	public EventImpactExperiment(UAVNetworkSimulation sim, Map<String, Object> configuration) {
		super(sim, configuration);
		/*navigationStrategies.add(LongestNoInspectionStartedEdgeNavigation.class);
		navigationStrategies.add(ACONavigation.class);
		navigationStrategies.add(IndividualNavigation.class);*/
	}
	
	@Override
	protected void prepareCycleDatasets() {
		totalCoverageXYs.add(new XYSeries("CycleNavigation"));
		totalCoverageXYs.add(new XYSeries("CycleNavigation_failures"));
		lowestSLAxys.add(new XYSeries("CycleNavigation"));
		lowestSLAxys.add(new XYSeries("CycleNavigation_failures"));
	}
	
	@Override
	protected void prepareStrategyDatasets(String navigationName) {
		totalCoverageXYs.add(new XYSeries(navigationName));
		totalCoverageXYs.add(new XYSeries(navigationName+"_failures"));
		lowestSLAxys.add(new XYSeries(navigationName));
		lowestSLAxys.add(new XYSeries(navigationName+"_failures"));
	}
	
	@Override
	protected void doSingleExperiment() {
		int nb = minNbOfUAVs;
		XYSeriesCollection totalCoverageDS = new XYSeriesCollection(),
				totalCoverageDSFailures = new XYSeriesCollection();
		boolean slasOk = false;
		do {
			runSimulation(nb, false);
			
			{
				XYSeries xySeries = new XYSeries("UAVs: " + nb);
				double[] pfd = sim.getSlaChecker().getTimeComplianceData(firstStep, lastStep);
				for(int i = 0; i < pfd.length; i++) {
					xySeries.add(i, pfd[i]);
				}
				totalCoverageDS.addSeries(xySeries);
				lowestSLAxys.get(lowestSLAxys.size()-2).add(nb, sim.slaChecker.lowestPercentage(firstStep, lastStep));
			}
			
			double coverage = sim.getSlaChecker().percentageFulfilledBetween(firstStep, lastStep);
			runSimulation(nb, true);
			
			double failCoverage = sim.getSlaChecker().percentageFulfilledBetween(firstStep, lastStep);
			double drop = coverage-failCoverage;
			System.out.printf("The SLA coverage dropped from %.2f to %.2f (%.2f%%)", coverage, failCoverage, drop);
			
			totalCoverageXYs.get(totalCoverageXYs.size()-2).add(nb, coverage);
			totalCoverageXYs.get(totalCoverageXYs.size()-1).add(nb, failCoverage);
			coverageDropDS.addValue(drop, sim.getNavigationBehaviourClass().getSimpleName(), nb+"");
			XYSeries xySeries = new XYSeries("UAVs: " + nb);
			double[] pfd = sim.getSlaChecker().getTimeComplianceData(firstStep, lastStep);
			for(int i = 0; i < pfd.length; i++) {
				xySeries.add(i, pfd[i]);
			}
			totalCoverageDSFailures.addSeries(xySeries);
			lowestSLAxys.get(lowestSLAxys.size()-1).add(nb, sim.slaChecker.lowestPercentage(firstStep, lastStep));
			
			if(minUAVsGraph && !slasOk && sim.allSLAsFulfilled(firstStep, lastStep)) {
				minUAVsDS.addValue(nb, sim.getNavigationBehaviourClass().getSimpleName(), sim.getNavigationBehaviourClass().getSimpleName());
				slasOk = true;
			}
			nb += uavStep;
		} while (nb <= maxNbOfUAVs);
		totalCoverages.add(totalCoverageDS);
		totalCoverages.add(totalCoverageDSFailures);
		navNames.add(sim.getNavigationBehaviourClass().getSimpleName());
		navNames.add(sim.getNavigationBehaviourClass().getSimpleName() + "_failures");
	}
	
	@Override
	protected void writeGraphs() {
		String directoryName = getDirectoryName();
		super.prepareToWriteGraphs();
		JFreeChart chart;
		XYPlot xyPlot;
		
		JFreeChart coverageDropChart = ChartFactory.createLineChart("Drop in SLA coverage", "Navigation", "%", coverageDropDS);
		CategoryPlot plot = coverageDropChart.getCategoryPlot();
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        
        FileOutputStream fout;
		try {
			XYSeriesCollection noFailures = new XYSeriesCollection(), failures = new XYSeriesCollection();
			for(int i = 0; i < totalCoverageXYs.size(); i += 2) {
				XYSeriesCollection xySeries = new XYSeriesCollection();
				xySeries.addSeries(totalCoverageXYs.get(i));
				xySeries.addSeries(totalCoverageXYs.get(i+1));
				fout = new FileOutputStream(new File(directoryName + "/" + String.format("%02d", (i/2)+4) + totalCoverageXYs.get(i).getKey() + ".png"));
				ChartUtilities.writeChartAsPNG(fout, ChartFactory.createXYLineChart("Total SLA Coverage", "#UAVs", "%", xySeries), 600, 400);
				noFailures.addSeries(totalCoverageXYs.get(i));
				failures.addSeries(totalCoverageXYs.get(i+1));
			}
			fout = new FileOutputStream(new File(directoryName + "/01CoverageDrop.png"));
			ChartUtilities.writeChartAsPNG(fout, coverageDropChart, 600, 400);
			fout = new FileOutputStream(new File(directoryName + "/02NoFailures.png"));
			ChartUtilities.writeChartAsPNG(fout, ChartFactory.createXYLineChart("Total SLA Coverage", "#UAVs", "%", noFailures), 600, 400);
			fout = new FileOutputStream(new File(directoryName + "/03Failures.png"));
			ChartUtilities.writeChartAsPNG(fout, ChartFactory.createXYLineChart("Total SLA Coverage", "#UAVs", "%", failures), 600, 400);

			new File(directoryName + "/SLACoverage_individual").mkdirs();
			XYSeriesCollection xyNo = new XYSeriesCollection(), xyFailures = new XYSeriesCollection();
			for(int i = 0; i < lowestSLAxys.size(); i += 2) {
				xyNo.addSeries(lowestSLAxys.get(i));
				xyFailures.addSeries(lowestSLAxys.get(i+1));
			}
			
			fout = new FileOutputStream(new File(directoryName + "/SLACoverage_individual/01NoFailures.png"));
			chart = ChartFactory.createXYLineChart("Lowest SLA Coverage", "#UAVs", "%", xyNo);
			xyPlot = chart.getXYPlot();
			((NumberAxis)xyPlot.getRangeAxis()).setAutoRangeIncludesZero(false);
			xyPlot.getRangeAxis().setUpperBound(100);
			ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
			
			fout = new FileOutputStream(new File(directoryName + "/SLACoverage_individual/02Failures.png"));
			ChartUtilities.writeChartAsPNG(fout, ChartFactory.createXYLineChart("Lowest SLA Coverage", "#UAVs", "%", xyFailures), 600, 400);

			new File(directoryName + "/SLACoverage").mkdirs();
			for(int i = 0; i < totalCoverages.size(); i++) {
				fout = new FileOutputStream(new File(directoryName + "/SLACoverage/" + String.format("%02d", i+1) + navNames.get(i) + ".png"));
				chart = ChartFactory.createXYLineChart("SLA coverage over time: " + navNames.get(i), "Step", "% of SLAs fulfilled", totalCoverages.get(i));
				/*XYPlot plot = chart.getXYPlot();
				((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);
				plot.getRangeAxis().setUpperBound(100);
				((NumberAxis)plot.getRangeAxis()).setAutoRangeMinimumSize(10);*/
				ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
			}
			
			if(minUAVsGraph) {
				chart = ChartFactory.createBarChart("Min #UAVs to fulfill SLAs", "Configuration", "#UAVs", minUAVsDS);
				fout = new FileOutputStream(new File(directoryName + "/minUAVsSLAsFulfilled.png"));
				ChartUtilities.writeChartAsPNG(fout, chart, Math.max(200, 80 + (20 * minUAVsDS.getColumnCount() * minUAVsDS.getRowCount())), 400);
			}
			
		} catch (IOException e) {
			System.out.println("Error writing chart: " + e.getMessage());
		}
	}

}

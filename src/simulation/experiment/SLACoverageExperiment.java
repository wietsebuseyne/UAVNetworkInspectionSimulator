package simulation.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import simulation.UAVNetworkSimulation;

public class SLACoverageExperiment extends NbOfUAVsExperiment {

	private int histogramBins = 20;

	private XYSeriesCollection coverageTimeDS = new XYSeriesCollection();
	private List<HistogramDataset> globalSpreadDSs = new ArrayList<HistogramDataset>();
	private List<HistogramDataset> slaSpreadDSs = new ArrayList<HistogramDataset>();
	
	List<String> navNames = new ArrayList<String>();
	private String statistics = "Strategy\tAvg Coverage\tLowest Coverage\tSLA Std Dev\tTime Std Dev",
			latexStatistics = "Strategy & Avg Cov & Lowest Cov & SLA SD & Time SD \\\\ \\hline";

	public SLACoverageExperiment(UAVNetworkSimulation sim,
			Map<String, Object> configuration) {
		super(sim);
		try {
			Double nbOfUAVs = (Double) configuration.get("nbOfUAVs");
			configuration.put("minNbOfUAVs", nbOfUAVs);
			configuration.put("maxNbOfUAVs", nbOfUAVs);
			configuration.put("uavStep", 1.0);
			initializeWithConfiguration(configuration);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException(
					"The experiment configuration is not correctly formatted. "
							+ "It should have contain the following structure:\n"
							+ "'configuration':{" + "\n\t'nbOfUAVs':int,"
							+ "\n\t'navigationStrategies':[String]" + "\n}\n"
							+ "Details: " + ex.getMessage());
		}
	}

	@Override
	protected void prepareCycleDatasets() {
	}

	@Override
	protected void prepareStrategyDatasets(String navigationName) {
	}

	@Override
	protected void doSingleExperiment() {
		String name = sim.getNavigationBehaviourClass().getSimpleName();
		navNames.add(name);
		runSimulation(minNbOfUAVs, true, true);

		XYSeries xySeries = new XYSeries(name);
		HistogramDataset globalSpreadDS = new HistogramDataset();
		HistogramDataset slaSpreadDS = new HistogramDataset();
		globalSpreadDS.setType(HistogramType.RELATIVE_FREQUENCY);
		/*
		 * SimpleHistogramDataset spreadDS = new SimpleHistogramDataset("Test");
		 * spreadDS.setAdjustForBinSize(true); for(int i = 0; i <= 100; i +=
		 * histogramBinWidth) { spreadDS.addBin(new SimpleHistogramBin(i, i +
		 * histogramBinWidth, true, false)); }
		 */
		
		double[] pfd = sim.getSlaChecker().getTimeComplianceData(
				firstStep, lastStep);
		globalSpreadDS.addSeries(name, pfd, histogramBins);
		slaSpreadDS.addSeries(name, sim.getSlaChecker().getSLAComplianceData(firstStep, lastStep), histogramBins);
		long stepSize = (lastStep - firstStep)
				/ sim.getSlaChecker().getNbOfDataPoints();
		for (int i = 0; i < pfd.length; i++) {
			xySeries.add(i * stepSize, pfd[i]);
		}
		
		double mean = sim.getSlaChecker().percentageFulfilledBetween(firstStep, lastStep);
		double lowest = sim.getSlaChecker().lowestPercentage(firstStep, lastStep);
		double globalSD = sim.getSlaChecker().getTimeStandardDeviation(firstStep, lastStep);
		double slaSD = sim.getSlaChecker().getSLAStandardDeviation(firstStep, lastStep);
		statistics += "\n" + name + "\t" + mean + "\t" + lowest + "\t" + slaSD + "\t" + globalSD;
		BigDecimal bMean = new BigDecimal(mean);
		bMean = bMean.round(new MathContext(5));
		BigDecimal bLowest = new BigDecimal(lowest);
		bLowest = bLowest.round(new MathContext(5));
		BigDecimal bGlobalSD = new BigDecimal(globalSD);
		bGlobalSD = bGlobalSD.round(new MathContext(5));
		BigDecimal bSLASD = new BigDecimal(slaSD);
		bSLASD = bSLASD.round(new MathContext(5));
		latexStatistics +=  "\n" + name + " & " + bMean.doubleValue() + " & " + bLowest.doubleValue() + " & " + bSLASD.doubleValue() + " & " + bGlobalSD.doubleValue() + "\\\\";
		coverageTimeDS.addSeries(xySeries);
		globalSpreadDSs.add(globalSpreadDS);
		slaSpreadDSs.add(globalSpreadDS);
	}

	@Override
	protected void writeGraphs() {
		String directoryName = getDirectoryName();
		super.prepareToWriteGraphs();
		JFreeChart chart;
		XYPlot plot;
		try {
			FileOutputStream fout;
			for (int i = 0; i < globalSpreadDSs.size(); i++) {
				chart = ChartFactory.createHistogram(
						navNames.get(i) + " Time coverage spread", "SLA percentage",
						"Relative occurence", globalSpreadDSs.get(i), PlotOrientation.VERTICAL,
						false, false, false);
				plot = chart.getXYPlot();
				plot.getRangeAxis().setUpperBound(1);
				fout = new FileOutputStream(new File(directoryName
						+ "/TimeCoverage_spread_"
						+ navNames.get(i) + ".png"));
				ChartUtilities.writeChartAsPNG(fout, chart, 400, 300);
			}
			for (int i = 0; i < slaSpreadDSs.size(); i++) {
				chart = ChartFactory.createHistogram(
						navNames.get(i) + " SLA coverage spread", "SLA percentage",
						"Relative occurence", slaSpreadDSs.get(i), PlotOrientation.VERTICAL,
						false, false, false);
				plot = chart.getXYPlot();
				plot.getRangeAxis().setUpperBound(1);
				fout = new FileOutputStream(new File(directoryName
						+ "/SLACoverage_spread_"
						+ navNames.get(i) + ".png"));
				ChartUtilities.writeChartAsPNG(fout, chart, 400, 300);
			}

			fout = new FileOutputStream(new File(directoryName
					+ "/SLACoverage_time.png"));
			chart = ChartFactory.createXYLineChart("SLA coverage over time",
					"Step", "% of SLAs fulfilled", coverageTimeDS);
			plot = chart.getXYPlot();
			((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);
			plot.getRangeAxis().setUpperBound(100);
			((NumberAxis) plot.getRangeAxis()).setAutoRangeMinimumSize(10);
			ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
			
			for(int i = 0; i < coverageTimeDS.getSeriesCount(); i++) {
				fout = new FileOutputStream(new File(directoryName
						+ "/SLACoverage_time_" + navNames.get(i) + ".png"));
				chart = ChartFactory.createXYLineChart("SLA coverage over time",
						"Step", "% of SLAs fulfilled", new XYSeriesCollection(coverageTimeDS.getSeries(i)));
				plot = chart.getXYPlot();
				((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);
				plot.getRangeAxis().setUpperBound(100);
				((NumberAxis) plot.getRangeAxis()).setAutoRangeMinimumSize(10);
				ChartUtilities.writeChartAsPNG(fout, chart, 600, 400);
			}
			
			fout = new FileOutputStream(new File(directoryName + "/responseTimeBoxPlot.png"));
			ChartUtilities.writeChartAsPNG(fout, sim.reporter.getResponseTimesBoxplot(), 80 + (30 * navigationStrategies.size()), 400);
			
			fout = new FileOutputStream(new File(directoryName + "/CoverageBoxPlot.png"));
			ChartUtilities.writeChartAsPNG(fout, sim.reporter.getCoverageBoxplot(), 80 + (100 * navigationStrategies.size()), 400);
			
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directoryName + "/statistics.txt"), "utf-8"))) {
			   writer.write(statistics);
			}
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directoryName + "/latex_statistics.txt"), "utf-8"))) {
			   writer.write(latexStatistics);
			}

		} catch (IOException e) {
			System.out.println("Error writing chart: " + e.getMessage());
		}
	}

}

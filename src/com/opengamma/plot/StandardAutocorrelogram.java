package com.opengamma.plot;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesAutocorrelation;
import com.opengamma.timeseries.DoubleTimeSeries;

public class StandardAutocorrelogram {

  public static JFreeChart getAutocorrelogram(DoubleTimeSeries ts, int max) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    List<Double> correlations = DoubleTimeSeriesAutocorrelation.getAutocorrelationSeries(ts, max);
    for (int i = 0; i <= max; i++) {
      dataset.addValue(correlations.get(i), "Value", Integer.toString(i));
    }
    double stdErr = DoubleTimeSeriesAutocorrelation.getStandardError(ts, max);
    JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset, PlotOrientation.VERTICAL, false, false, false);
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    plot.getRangeAxis().setUpperBound(1.1);
    plot.getRangeAxis().setLowerBound(-1.1);
    plot.setRangeGridlinesVisible(false);
    return chart;
  }
}

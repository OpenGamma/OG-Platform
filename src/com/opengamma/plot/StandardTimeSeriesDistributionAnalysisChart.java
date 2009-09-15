package com.opengamma.plot;

import org.jfree.chart.JFreeChart;

import com.opengamma.timeseries.DoubleTimeSeries;

public class StandardTimeSeriesDistributionAnalysisChart {
  private static StandardJFreeChartHistogramChart _chart = new StandardJFreeChartHistogramChart();

  public static JFreeChart getChart(DoubleTimeSeries ts) {
    return _chart.getHistogram(ts, "", "", "", false, false, false);
  }
}

package com.opengamma.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.opengamma.timeseries.DoubleTimeSeries;

public class StandardJFreeChartTimeSeriesChart {
  // TODO needs a lot of work: have to do something about customisation (various
  // set methods and defaults)

  // map of ts label -> plot
  public JFreeChart getPlot(DoubleTimeSeries ts, String title, String abscissaLabel, String ordinateLabel, boolean legend, boolean tooltips, boolean urls) {
    TimeSeries jFreeTS = JFreeChartTimeSeriesWrapper.getJFreeChartTimeSeries(ts, "");
    XYDataset dataset = new TimeSeriesCollection(jFreeTS);
    return ChartFactory.createTimeSeriesChart(title, abscissaLabel, ordinateLabel, dataset, legend, tooltips, urls);
  }
}

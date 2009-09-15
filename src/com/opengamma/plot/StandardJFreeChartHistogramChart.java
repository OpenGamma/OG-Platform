package com.opengamma.plot;

import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import com.opengamma.timeseries.DoubleTimeSeries;

public class StandardJFreeChartHistogramChart {
  // TODO
  // TODO map of ts label -> data
  public JFreeChart getHistogram(DoubleTimeSeries ts, String title, String abscissaLabel, String ordinateLabel, boolean legend, boolean tooltip, boolean urls) {
    HistogramDataset dataset = new HistogramDataset();
    double[] data = new double[ts.size()];
    int i = 0;
    for (Iterator<Double> iter = ts.valuesIterator(); iter.hasNext();) {
      data[i++] = iter.next();
    }
    int bins = ts.size() / 10;
    dataset.addSeries("", data, bins >= 1 ? bins : ts.size());// TODO
    return ChartFactory.createHistogram(title, abscissaLabel, ordinateLabel, dataset, PlotOrientation.VERTICAL, legend, tooltip, urls);// TODO
    // change
    // plot
    // orientation
  }

}

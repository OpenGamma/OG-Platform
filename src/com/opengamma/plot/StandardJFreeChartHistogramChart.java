package com.opengamma.plot;

import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import com.opengamma.util.timeseries.DoubleTimeSeries;

public class StandardJFreeChartHistogramChart {
  // TODO
  // TODO map of ts label -> data
  public JFreeChart getHistogram(final DoubleTimeSeries<?> ts, final String title, final String abscissaLabel, final String ordinateLabel, final boolean legend,
      final boolean tooltip, final boolean urls) {
    final HistogramDataset dataset = new HistogramDataset();
    final double[] data = new double[ts.size()];
    int i = 0;
    for (final Iterator<Double> iter = ts.valuesIterator(); iter.hasNext();) {
      data[i++] = iter.next();
    }
    final int bins = ts.size() / 10;
    dataset.addSeries("", data, bins >= 1 ? bins : ts.size());// TODO
    return ChartFactory.createHistogram(title, abscissaLabel, ordinateLabel, dataset, PlotOrientation.VERTICAL, legend, tooltip, urls);// TODO
    // change
    // plot
    // orientation
  }

}

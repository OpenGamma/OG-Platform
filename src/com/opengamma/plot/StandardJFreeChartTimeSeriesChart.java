package com.opengamma.plot;

import javax.time.calendar.ZonedDateTime;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.opengamma.util.timeseries.DoubleTimeSeries;

public class StandardJFreeChartTimeSeriesChart {
  // TODO needs a lot of work: have to do something about customisation (various
  // set methods and defaults)

  // map of ts label -> plot
  public JFreeChart getPlot(final DoubleTimeSeries<ZonedDateTime> ts, final String title, final String abscissaLabel, final String ordinateLabel, final boolean legend,
      final boolean tooltips, final boolean urls) {
    final TimeSeries jFreeTS = JFreeChartTimeSeriesWrapper.getJFreeChartTimeSeries(ts, "");
    final XYDataset dataset = new TimeSeriesCollection(jFreeTS);
    return ChartFactory.createTimeSeriesChart(title, abscissaLabel, ordinateLabel, dataset, legend, tooltips, urls);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.timeseries;

import java.io.PrintStream;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.MutableLocalDateDoubleTimeSeries;

public class TimeSeriesExample {
  public static void timeSeriesExample(final PrintStream out) {
    final MutableLocalDateDoubleTimeSeries ts1 = new ListLocalDateDoubleTimeSeries();
    ts1.putDataPoint(LocalDate.of(2010, 1, 1), 2.1d);
    ts1.putDataPoint(LocalDate.of(2010, 1, 2), 2.2d);
    ts1.putDataPoint(LocalDate.of(2010, 1, 3), 2.3d);
    out.println("ts1: " + ts1);

    final LocalDateDoubleTimeSeries ts2 = new ArrayLocalDateDoubleTimeSeries(ts1);
    out.println("ts2: " + ts2);

    final LocalDateDoubleTimeSeries ts3 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 2)}, new double[] {1.1d,
        1.2d});
    out.println("ts3: " + ts3);

    final LocalDateDoubleTimeSeries ts4 = ts2.subSeries(LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 3));
    out.println("ts4: " + ts4);
  }

  public static void main(final String[] args) throws Exception {
    timeSeriesExample(System.out);
  }
}

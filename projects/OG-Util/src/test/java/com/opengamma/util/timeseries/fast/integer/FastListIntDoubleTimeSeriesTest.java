/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.IntDoubleTimeSeriesTest;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;

@Test
public class FastListIntDoubleTimeSeriesTest extends IntDoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries<Integer> createEmptyTimeSeries() {
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  public DoubleTimeSeries<Integer> createTimeSeries(final Integer[] times, final double[] values) {
    final int[] primTimes = new int[times.length];
    for (int i = 0; i < times.length; i++) {
      primTimes[i] = times[i].intValue();
    }
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, primTimes, values);
  }

  @Override
  public DoubleTimeSeries<Integer> createTimeSeries(final List<Integer> times, final List<Double> values) {
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, times, values);
  }

  @Override
  public DoubleTimeSeries<Integer> createTimeSeries(final DoubleTimeSeries<Integer> dts) {
    return new FastListIntDoubleTimeSeries((FastIntDoubleTimeSeries) dts);
  }

}

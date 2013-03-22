/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.IntDoubleTimeSeriesTest;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;

@Test(groups = "unit")
public class FastListIntDoubleTimeSeriesTest extends IntDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<Integer> createEmptyTimeSeries() {
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  protected DoubleTimeSeries<Integer> createTimeSeries(final Integer[] times, final double[] values) {
    final int[] primTimes = new int[times.length];
    for (int i = 0; i < times.length; i++) {
      primTimes[i] = times[i].intValue();
    }
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, primTimes, values);
  }

  @Override
  protected DoubleTimeSeries<Integer> createTimeSeries(final List<Integer> times, final List<Double> values) {
    return new FastListIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, times, values);
  }

  @Override
  protected DoubleTimeSeries<Integer> createTimeSeries(final DoubleTimeSeries<Integer> dts) {
    return new FastListIntDoubleTimeSeries((FastIntDoubleTimeSeries) dts);
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.LongDoubleTimeSeriesTest;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

@Test(groups = "unit")
public class FastArrayLongDoubleTimeSeriesTest extends LongDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<Long> createEmptyTimeSeries() {
    return FastArrayLongDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final Long[] times, final double[] values) {
    final long[] primTimes = new long[times.length];
    for (int i = 0; i < times.length; i++) {
      primTimes[i] = times[i].intValue();
    }
    return new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, primTimes, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final List<Long> times, final List<Double> values) {
    return new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, times, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final DoubleTimeSeries<Long> dts) {
    return new FastArrayLongDoubleTimeSeries((FastLongDoubleTimeSeries) dts);
  }

}

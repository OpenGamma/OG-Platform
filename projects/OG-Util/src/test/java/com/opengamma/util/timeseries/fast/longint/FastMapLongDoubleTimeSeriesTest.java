/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.LongDoubleTimeSeriesTest;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;

@Test(groups = TestGroup.UNIT)
public class FastMapLongDoubleTimeSeriesTest extends LongDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<Long> createEmptyTimeSeries() {
    return new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final Long[] times, final double[] values) {
    final long[] primTimes = new long[times.length];
    for (int i = 0; i < times.length; i++) {
      primTimes[i] = times[i].intValue();
    }
    return new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, primTimes, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final List<Long> times, final List<Double> values) {
    return new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, times, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final DoubleTimeSeries<Long> dts) {
    return new FastMapLongDoubleTimeSeries((FastLongDoubleTimeSeries) dts);
  }

}

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
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

@Test(groups = "unit")
public class FastListLongDoubleTimeSeriesTest extends LongDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<Long> createEmptyTimeSeries() {
    return new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final Long[] times, final double[] values) {
    final long[] primTimes = new long[times.length];
    for (int i = 0; i < times.length; i++) {
      primTimes[i] = times[i].intValue();
    }
    return new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, primTimes, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final List<Long> times, final List<Double> values) {
    return new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, times, values);
  }

  @Override
  protected DoubleTimeSeries<Long> createTimeSeries(final DoubleTimeSeries<Long> dts) {
    return new FastListLongDoubleTimeSeries((FastLongDoubleTimeSeries) dts);
  }

}

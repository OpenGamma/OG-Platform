/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.LocalDateDoubleTimeSeriesTest;

/**
 * Test.
 */
@Test(groups = "unit")
public class ImmutableLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<LocalDate> createEmptyTimeSeries() {
    return ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  protected DoubleTimeSeries<LocalDate> createTimeSeries(LocalDate[] times, double[] values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> createTimeSeries(List<LocalDate> times, List<Double> values) {
    return ImmutableLocalDateDoubleTimeSeries.of(times, values);
  }

  @Override
  protected DoubleTimeSeries<LocalDate> createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return ImmutableLocalDateDoubleTimeSeries.from(dts);
  }

}

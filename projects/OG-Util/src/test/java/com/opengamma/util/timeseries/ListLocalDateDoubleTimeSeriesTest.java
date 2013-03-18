/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ListLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  protected LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return new ListLocalDateDoubleTimeSeries();
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(LocalDate[] times, double[] values) {
    return new ListLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(List<LocalDate> times, List<Double> values) {
    return new ListLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return new ListLocalDateDoubleTimeSeries(dts);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

@Test(groups = "unit")
public class ArrayDateDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  protected DoubleTimeSeries<Date> createEmptyTimeSeries() {
    return new ArrayDateDoubleTimeSeries();
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArrayDateDoubleTimeSeries(dts);
  }

}

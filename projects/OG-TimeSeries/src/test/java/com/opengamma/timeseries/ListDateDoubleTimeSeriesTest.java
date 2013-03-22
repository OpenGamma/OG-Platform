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
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.ListDateDoubleTimeSeries;

@Test(groups = "unit")
public class ListDateDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  protected DateDoubleTimeSeries createEmptyTimeSeries() {
    return new ListDateDoubleTimeSeries();
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ListDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ListDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ListDateDoubleTimeSeries(dts);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MapDateDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class MapDateDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  protected DateDoubleTimeSeries createEmptyTimeSeries() {
    return new MapDateDoubleTimeSeries();
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new MapDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new MapDateDoubleTimeSeries(times, values);
  }

  @Override
  protected DateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new MapDateDoubleTimeSeries(dts);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.sql.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

@Test
public class ArraySQLDateDoubleTimeSeriesTest extends SQLDateDoubleTimeSeriesTest {

  @Override
  public SQLDateDoubleTimeSeries createEmptyTimeSeries() {
    return new ArraySQLDateDoubleTimeSeries();
  }

  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArraySQLDateDoubleTimeSeries(times, values);
  }

  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArraySQLDateDoubleTimeSeries(times, values);
  }

  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArraySQLDateDoubleTimeSeries(dts);
  }
}

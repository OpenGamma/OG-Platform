/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.sql.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ArraySQLDateDoubleTimeSeriesTest extends SQLDateDoubleTimeSeriesTest {

  @Override
  protected SQLDateDoubleTimeSeries createEmptyTimeSeries() {
    return new ArraySQLDateDoubleTimeSeries();
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArraySQLDateDoubleTimeSeries(times, values);
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArraySQLDateDoubleTimeSeries(times, values);
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArraySQLDateDoubleTimeSeries(dts);
  }

}

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
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ListSQLDateDoubleTimeSeriesTest extends SQLDateDoubleTimeSeriesTest {

  @Override
  protected SQLDateDoubleTimeSeries createEmptyTimeSeries() {
    return new ListSQLDateDoubleTimeSeries();
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ListSQLDateDoubleTimeSeries(times, values);
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ListSQLDateDoubleTimeSeries(times, values);
  }

  @Override
  protected SQLDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ListSQLDateDoubleTimeSeries(dts);
  }

}

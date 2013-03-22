/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.sql.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.timeseries.sqldate.SQLDateDoubleTimeSeries;

@Test(groups = "unit")
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

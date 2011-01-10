/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.sql.Date;
import java.util.List;

import com.opengamma.util.timeseries.sqldate.MapSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

public class MapSQLDateDoubleTimeSeriesTest extends SQLDateDoubleTimeSeriesTest {

  @Override
  public SQLDateDoubleTimeSeries createEmptyTimeSeries() {
    return new MapSQLDateDoubleTimeSeries();
  }

  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new MapSQLDateDoubleTimeSeries(times, values);
  }

  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new MapSQLDateDoubleTimeSeries(times, values);
  }
  
  @Override
  public SQLDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new MapSQLDateDoubleTimeSeries(dts);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateObjectTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateObjectTimeSeries;

public class ListSQLDateObjectTimeSeriesTest extends SQLDateObjectTimeSeriesTest {

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListSQLDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(dts);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeObjectTimeSeries;

public class ArrayDateTimeObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ArrayDateTimeObjectTimeSeries<BigDecimal>();
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ArrayDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ArrayDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ArrayDateTimeObjectTimeSeries<BigDecimal>(dts);
  }
}

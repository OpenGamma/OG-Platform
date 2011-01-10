/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.MapDateTimeObjectTimeSeries;

public class MapDateTimeObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new MapDateTimeObjectTimeSeries<BigDecimal>();
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(dts);
  }
}

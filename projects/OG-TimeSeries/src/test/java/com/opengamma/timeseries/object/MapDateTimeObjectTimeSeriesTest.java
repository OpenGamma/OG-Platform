/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.object;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.date.time.DateTimeObjectTimeSeries;
import com.opengamma.timeseries.date.time.MapDateTimeObjectTimeSeries;

@Test(groups = "unit")
public class MapDateTimeObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new MapDateTimeObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new MapDateTimeObjectTimeSeries<BigDecimal>(dts);
  }

}

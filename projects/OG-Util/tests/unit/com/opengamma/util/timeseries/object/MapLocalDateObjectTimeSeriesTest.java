/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateObjectTimeSeries;

public class MapLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new MapLocalDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(LocalDate[] times, BigDecimal[] values) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(List<LocalDate> times, List<BigDecimal> values) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(dts);
  }
}

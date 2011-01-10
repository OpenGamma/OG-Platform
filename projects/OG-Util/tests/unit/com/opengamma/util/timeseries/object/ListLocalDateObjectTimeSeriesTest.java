/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateObjectTimeSeries;

public class ListLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListLocalDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(LocalDate[] times, BigDecimal[] values) {
    return new ListLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(List<LocalDate> times, List<BigDecimal> values) {
    return new ListLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return new ListLocalDateObjectTimeSeries<BigDecimal>(dts);
  }
}

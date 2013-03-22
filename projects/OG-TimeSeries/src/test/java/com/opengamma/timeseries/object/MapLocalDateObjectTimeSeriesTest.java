/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.object;

import java.math.BigDecimal;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateObjectTimeSeries;
import com.opengamma.timeseries.localdate.MapLocalDateObjectTimeSeries;

@Test(groups = "unit")
public class MapLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new MapLocalDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(LocalDate[] times, BigDecimal[] values) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(List<LocalDate> times, List<BigDecimal> values) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return new MapLocalDateObjectTimeSeries<BigDecimal>(dts);
  }

}

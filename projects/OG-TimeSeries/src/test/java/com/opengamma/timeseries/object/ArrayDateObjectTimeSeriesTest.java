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
import com.opengamma.timeseries.date.ArrayDateObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;

@Test(groups = "unit")
public class ArrayDateObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  protected ObjectTimeSeries<Date, BigDecimal> createEmptyTimeSeries() {
    return new ArrayDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected DateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ArrayDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected DateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ArrayDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected DateObjectTimeSeries<BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ArrayDateObjectTimeSeries<BigDecimal>(dts);
  }

}

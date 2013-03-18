/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.date.ArrayDateObjectTimeSeries;
import com.opengamma.util.timeseries.date.DateObjectTimeSeries;

@Test(groups = TestGroup.UNIT)
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

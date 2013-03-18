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
import com.opengamma.util.timeseries.date.time.DateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeObjectTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ListDateTimeObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListDateTimeObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(dts);
  }

}

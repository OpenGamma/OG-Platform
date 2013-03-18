/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;

import java.math.BigDecimal;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateObjectTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ArrayLocalDateObjectTimeSeriesTest extends LocalDateObjectTimeSeriesTest {

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ArrayLocalDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(LocalDate[] times, BigDecimal[] values) {
    return new ArrayLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected LocalDateObjectTimeSeries<BigDecimal> createTimeSeries(List<LocalDate> times, List<BigDecimal> values) {
    return new ArrayLocalDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected ObjectTimeSeries<LocalDate, BigDecimal> createTimeSeries(ObjectTimeSeries<LocalDate, BigDecimal> dts) {
    return new ArrayLocalDateObjectTimeSeries<BigDecimal>(dts);
  }

}

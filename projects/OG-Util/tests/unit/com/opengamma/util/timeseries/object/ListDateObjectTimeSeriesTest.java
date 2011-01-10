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
import com.opengamma.util.timeseries.date.DateObjectTimeSeries;
import com.opengamma.util.timeseries.date.ListDateObjectTimeSeries;

public class ListDateObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createEmptyTimeSeries() {
    return new ListDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ListDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ListDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ListDateObjectTimeSeries<BigDecimal>(dts);
  }
}

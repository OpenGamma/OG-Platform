/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;

import java.math.BigDecimal;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ListZonedDateTimeObjectTimeSeriesTest extends ZonedDateTimeObjectTimeSeriesTest {

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(ZonedDateTime[] times, BigDecimal[] values) {
    return new ListZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, times, values);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<ZonedDateTime> times, List<BigDecimal> values) {
    return new ListZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, times, values);
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(ObjectTimeSeries<ZonedDateTime, BigDecimal> dts) {
    return new ListZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, dts);
  }

}

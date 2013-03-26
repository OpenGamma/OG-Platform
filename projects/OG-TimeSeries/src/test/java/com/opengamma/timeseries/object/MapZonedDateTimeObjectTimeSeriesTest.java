/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.object;

import java.math.BigDecimal;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.zoneddatetime.MapZonedDateTimeObjectTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ZonedDateTimeObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class MapZonedDateTimeObjectTimeSeriesTest extends ZonedDateTimeObjectTimeSeriesTest {

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new MapZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(ZonedDateTime[] times, BigDecimal[] values) {
    return new MapZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, times, values);
  }

  @Override
  protected ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<ZonedDateTime> times, List<BigDecimal> values) {
    return new MapZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, times, values);
  }

  @Override
  protected ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(ObjectTimeSeries<ZonedDateTime, BigDecimal> dts) {
    return new MapZonedDateTimeObjectTimeSeries<BigDecimal>(ZoneOffset.UTC, dts);
  }

}

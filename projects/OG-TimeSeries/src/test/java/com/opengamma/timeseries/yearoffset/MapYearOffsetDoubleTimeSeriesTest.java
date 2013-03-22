/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.yearoffset;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.yearoffset.MapYearOffsetDoubleTimeSeries;
import com.opengamma.timeseries.yearoffset.YearOffsetDoubleTimeSeries;

@Test(groups = "unit")
public class MapYearOffsetDoubleTimeSeriesTest extends YearOffsetDoubleTimeSeriesTest {

  @Override
  protected YearOffsetDoubleTimeSeries createEmptyTimeSeries() {
    return new MapYearOffsetDoubleTimeSeries(zdt(2013, 1, 24, 12, 0, 0, 0, ZoneOffset.UTC), new Double[0], new double[0]);
  }

  @Override
  protected YearOffsetDoubleTimeSeries createTimeSeries(final Double[] times, final double[] values) {
    return new MapYearOffsetDoubleTimeSeries(zdt(2013, 1, 24, 12, 0, 0, 0, ZoneOffset.UTC), times, values);
  }

  @Override
  protected YearOffsetDoubleTimeSeries createTimeSeries(final List<Double> times, final List<Double> values) {
    return new MapYearOffsetDoubleTimeSeries(zdt(2013, 1, 24, 12, 0, 0, 0, ZoneOffset.UTC), times, values);
  }

  @Override
  protected YearOffsetDoubleTimeSeries createTimeSeries(final DoubleTimeSeries<Double> dts) {
    return new MapYearOffsetDoubleTimeSeries((YearOffsetDoubleTimeSeries) dts);
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}

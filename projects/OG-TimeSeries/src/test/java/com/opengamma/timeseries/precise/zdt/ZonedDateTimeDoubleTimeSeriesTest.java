/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeriesTest;

/**
 * Abstract test for ZonedDateTimeDoubleTimeSeries.
 */
public abstract class ZonedDateTimeDoubleTimeSeriesTest extends DoubleTimeSeriesTest<ZonedDateTime> {

  @Override
  protected ZonedDateTime[] testTimes() {
    ZonedDateTime one = ZonedDateTime.of(2010, 2, 8, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime two = ZonedDateTime.of(2010, 2, 9, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime three = ZonedDateTime.of(2010, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime four = ZonedDateTime.of(2010, 2, 11, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime five = ZonedDateTime.of(2010, 2, 12, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime six = ZonedDateTime.of(2010, 2, 13, 0, 0, 0, 0, ZoneOffset.UTC);
    return new ZonedDateTime[] {one, two, three, four, five, six };
  }

  @Override
  protected ZonedDateTime[] testTimes2() {
    ZonedDateTime one = ZonedDateTime.of(2010, 2, 11, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime two = ZonedDateTime.of(2010, 2, 12, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime three = ZonedDateTime.of(2010, 2, 13, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime four = ZonedDateTime.of(2010, 2, 14, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime five = ZonedDateTime.of(2010, 2, 15, 0, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime six = ZonedDateTime.of(2010, 2, 16, 0, 0, 0, 0, ZoneOffset.UTC);
    return new ZonedDateTime[] {one, two, three, four, five, six };
  }

  @Override
  protected ZonedDateTime[] emptyTimes() {
    return new ZonedDateTime[] {};
  }

}

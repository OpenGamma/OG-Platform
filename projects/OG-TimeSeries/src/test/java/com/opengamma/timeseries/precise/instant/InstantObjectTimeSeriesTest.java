/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeriesTest;

/**
 * Abstract test for InstantObjectTimeSeries.
 */
public abstract class InstantObjectTimeSeriesTest extends PreciseObjectTimeSeriesTest<Instant> {

  @Override
  protected Instant[] testTimes() {
    Instant one = OffsetDateTime.of(2010, 2, 8, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant two = OffsetDateTime.of(2010, 2, 9, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant three = OffsetDateTime.of(2010, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant four = OffsetDateTime.of(2010, 2, 11, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant five = OffsetDateTime.of(2010, 2, 12, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant six = OffsetDateTime.of(2010, 2, 13, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    return new Instant[] { one, two, three, four, five, six };
  }

  @Override
  protected Instant[] testTimes2() {
    Instant one = OffsetDateTime.of(2010, 2, 11, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant two = OffsetDateTime.of(2010, 2, 12, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant three = OffsetDateTime.of(2010, 2, 13, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant four = OffsetDateTime.of(2010, 2, 14, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant five = OffsetDateTime.of(2010, 2, 15, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    Instant six = OffsetDateTime.of(2010, 2, 16, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
    return new Instant[] { one, two, three, four, five, six };
  } 

  @Override
  protected Instant[] emptyTimes() {
    return new Instant[] {};
  }

}

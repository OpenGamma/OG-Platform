/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/**
 * Test.
 */
public abstract class ZonedDateTimeDoubleTimeSeriesTest extends DoubleTimeSeriesTest<ZonedDateTime> {
  
  protected ZonedDateTime makeDate(int year, int month, int day) {
    ZonedDateTime one = ZonedDateTime.of(LocalDateTime.of(year, month, day, 0, 0), ZoneOffset.UTC);//ZoneId.of(java.util.TimeZone.getDefault().getID()));
    return one;
  }
  
  @Override
  protected ZonedDateTime[] testTimes() {
    ZonedDateTime one = makeDate(2010, 2, 8);
    ZonedDateTime two = makeDate(2010, 2, 9);
    ZonedDateTime three = makeDate(2010, 2, 10);
    ZonedDateTime four = makeDate(2010, 2, 11);
    ZonedDateTime five = makeDate(2010, 2, 12);
    ZonedDateTime six = makeDate(2010, 2, 13);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }

  @Override
  protected ZonedDateTime[] testTimes2() {
    ZonedDateTime one = makeDate(2010, 2, 11);
    ZonedDateTime two = makeDate(2010, 2, 12);
    ZonedDateTime three = makeDate(2010, 2, 13);
    ZonedDateTime four = makeDate(2010, 2, 14);
    ZonedDateTime five = makeDate(2010, 2, 15);
    ZonedDateTime six = makeDate(2010, 2, 16);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  } 

  @Override
  protected ZonedDateTime[] emptyTimes() {
    return new ZonedDateTime[] {};
  }

}

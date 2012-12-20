/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public abstract class ZonedDateTimeDoubleTimeSeriesTest extends DoubleTimeSeriesTest<ZonedDateTime> {
  
  public ZonedDateTime makeDate(int year, int month, int day) {
    ZonedDateTime one = ZonedDateTime.of(LocalDateTime.ofMidnight(year, month, day), TimeZone.UTC);//TimeZone.of(java.util.TimeZone.getDefault().getID()));
    return one;
  }
  
  @Override
  public ZonedDateTime[] testTimes() {
    ZonedDateTime one = makeDate(2010, 2, 8);
    ZonedDateTime two = makeDate(2010, 2, 9);
    ZonedDateTime three = makeDate(2010, 2, 10);
    ZonedDateTime four = makeDate(2010, 2, 11);
    ZonedDateTime five = makeDate(2010, 2, 12);
    ZonedDateTime six = makeDate(2010, 2, 13);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  }

  @Override
  public ZonedDateTime[] testTimes2() {
    ZonedDateTime one = makeDate(2010, 2, 11);
    ZonedDateTime two = makeDate(2010, 2, 12);
    ZonedDateTime three = makeDate(2010, 2, 13);
    ZonedDateTime four = makeDate(2010, 2, 14);
    ZonedDateTime five = makeDate(2010, 2, 15);
    ZonedDateTime six = makeDate(2010, 2, 16);
    return new ZonedDateTime[] { one, two, three, four, five, six };
  } 

  @Override
  public ZonedDateTime[] emptyTimes() {
    return new ZonedDateTime[] {};
  }
}

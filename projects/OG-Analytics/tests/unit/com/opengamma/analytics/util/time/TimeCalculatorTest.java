/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.analytics.util.time.TimeCalculator;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * The TimeCalculator computes the difference between two instants as 'Analytics time', 
 * which is actually a measure of years. This is used primarily for interest accrual and curve/surface interpolation
 */
public class TimeCalculatorTest {

  private static final double TOLERANCE = 1.0E-50;

  @Test
  /** Same instant must have no time between */
  public void sameInstant() {

    final ZonedDateTime now = ZonedDateTime.now();
    //final ZonedDateTime dt1 = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDDAY, TimeZone.UTC);

    assertEquals(0.0, TimeCalculator.getTimeBetween(now, now));
  }

  @Test
  /** No time between instants on same date */
  public void sameDay() {

    final ZonedDateTime midday = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDDAY, TimeZone.UTC);
    final ZonedDateTime midnight = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, TimeZone.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midday, midnight);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** No time between instants on same date */
  public void sameDay2() {

    final ZonedDateTime midday = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDDAY, TimeZone.UTC);
    final ZonedDateTime midnight = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, TimeZone.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midnight, midday);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /** Time between same instants but specified under time zones that fall on different days.
      This is trapped as daycount computation first converts each ZonedDateTime to LocalDate. */
  public void sameTimeDifferentLocalDates() {

    final ZonedDateTime midnightLondon = ZonedDateTime.of(LocalDate.of(2012, 03, 12), LocalTime.MIDNIGHT, TimeZone.UTC);
    final ZonedDateTime sevenNewYork = ZonedDateTime.of(LocalDate.of(2012, 03, 11), LocalTime.of(19, 0), TimeZone.of("EST"));
    assertTrue(midnightLondon.equalInstant(sevenNewYork));
    final double yearFraction = TimeCalculator.getTimeBetween(sevenNewYork, midnightLondon);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }
}

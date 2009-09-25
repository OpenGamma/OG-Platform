/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.time.Duration;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

/**
 * 
 * Utility class for InstantProvider.
 * 
 * @author emcleod
 */
public class DateUtil {
  public static final long SECONDS_PER_DAY = 86400L;
  public static final double DAYS_PER_YEAR = 365.25;
  public static final long MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000;
  public static final long SECONDS_PER_YEAR = (long) (SECONDS_PER_DAY * DAYS_PER_YEAR);
  public static final long MILLISECONDS_PER_YEAR = SECONDS_PER_YEAR * 1000;

  /**
   * Returns d1 - d2 in years, where a year is defined as 365.25 days.
   * 
   * @param d1
   *          The first date.
   * @param d2
   *          The second date.
   * @return The difference in years.
   * @throws IllegalArgumentException
   *           If either date is null.
   */
  public static double getDifferenceInYears(final InstantProvider d1, final InstantProvider d2) {

    // TODO InstantInterval.intervalBetween
    if (d1 == null) {
      throw new IllegalArgumentException("First date was null");
    }
    if (d2 == null) {
      throw new IllegalArgumentException("Second date was null");
    }
    return (double) (d1.toInstant().toEpochMillis() - d2.toInstant().toEpochMillis()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns d1 - d2 in years.
   * 
   * @param d1
   *          The first date.
   * @param d2
   *          The second date.
   * @param daysInYear
   *          Number of days in year.
   * @return The difference in years.
   */
  public static double getDifferenceInYears(final InstantProvider d1, final InstantProvider d2, final double daysInYear) {
    return daysInYear * d1.toInstant().toEpochMillis() - d2.toInstant().toEpochMillis() / MILLISECONDS_PER_DAY;
  }

  // REVIEW emcleod 24/9/09. This is a quick and dirty way of doing this - I
  // don't like the
  // rounding.
  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest long.
   * 
   * @param startDate
   *          The date to offset.
   * @param yearFraction
   *          The fraction of a year to add to the original date.
   * @return
   */
  public static InstantProvider getDateOffsetWithYearFraction(final InstantProvider startDate, final double yearFraction) {
    final long seconds = Math.round(SECONDS_PER_YEAR * yearFraction);
    return startDate.toInstant().plus(Duration.seconds(seconds));
  }

  public static ZonedDateTime getUTCDate(final int year, final int month, final int day, final int hour, final int minutes) {
    return ZonedDateTime.dateTime(LocalDate.date(year, month, day), LocalTime.time(hour, minutes), TimeZone.UTC);
  }

  public static double subtract(final Date d1, final Date d2) {
    return (d1.getTime() - d2.getTime()) / MILLISECONDS_PER_DAY;
  }

  public static Date add(final Date d, final double offset) {
    final long x = d.getTime() + (long) (offset * MILLISECONDS_PER_DAY * 365.25);
    return new Date(x);
  }

  public static Date today() {
    final Calendar today = Calendar.getInstance();
    final int year = today.get(Calendar.YEAR);
    final int month = today.get(Calendar.MONTH);
    final int day = today.get(Calendar.DAY_OF_MONTH);
    final Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }

  public static Date date(final int yyyymmdd) {
    final int year = yyyymmdd / 10000;
    final int month = (yyyymmdd - 10000 * year) / 100;
    final int day = yyyymmdd - 10000 * year - 100 * month;
    final Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }
}

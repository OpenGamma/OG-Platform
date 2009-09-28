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
import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.field.MonthOfYear;

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
    if (d1 == null)
      throw new IllegalArgumentException("First date was null");
    if (d2 == null)
      throw new IllegalArgumentException("Second date was null");
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
   * @throws IllegalArgumentException
   *           If either date is null.
   */
  public static double getDifferenceInYears(final InstantProvider d1, final InstantProvider d2, final double daysInYear) {
    if (d1 == null)
      throw new IllegalArgumentException("First date was null");
    if (d2 == null)
      throw new IllegalArgumentException("Second date was null");
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
   * @return The offset date.
   */
  public static InstantProvider getDateOffsetWithYearFraction(final InstantProvider startDate, final double yearFraction) {
    if (startDate == null)
      throw new IllegalArgumentException("Date was null");
    final long seconds = Math.round(SECONDS_PER_YEAR * yearFraction);
    return startDate.toInstant().plus(Duration.seconds(seconds));
  }

  /**
   * Returns a UTC date given year, month, day with the time set to midnight
   * (UTC).
   * 
   * @param year
   * @param month
   * @param day
   * @return
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day) {
    return ZonedDateTime.dateTime(LocalDateTime.dateMidnight(year, month, day), TimeZone.UTC);
  }

  /**
   * Returns a UTC date given year, month, day, hour and minutes.
   * 
   * @param year
   * @param month
   * @param day
   * @param hour
   * @param minutes
   * @return A UTC date.
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day, final int hour, final int minutes) {
    return ZonedDateTime.dateTime(LocalDate.date(year, month, day), LocalTime.time(hour, minutes), TimeZone.UTC);
  }

  /**
   * Returns a date given year, month, day, hour, minutes and the name of the
   * time zone.
   * 
   * @param year
   * @param month
   * @param day
   * @param hour
   * @param minutes
   * @param timeZone
   * @return A UTC date.
   */
  public static ZonedDateTime getDateInTimeZone(final int year, final int month, final int day, final int hour, final int minutes, final String timeZone) {
    return ZonedDateTime.dateTime(LocalDate.date(year, month, day), LocalTime.time(hour, minutes), TimeZone.timeZone("London"));
  }

  /**
   * Determines whether the ZonedDateTime is in a leap year.
   * 
   * @param date
   * @return True if the date is in a leap year.
   */
  public static boolean isLeapYear(final ZonedDateTime date) {
    return MonthOfYear.FEBRUARY.lengthInDays(date.getYear()) == 29 ? true : false;
  }

  /**
   * Determines whether the LocalDate is in a leap year.
   * 
   * @param date
   * @return True if the date is in a leap year.
   */
  public static boolean isLeapYear(final LocalDate date) {
    return MonthOfYear.FEBRUARY.lengthInDays(date.getYear()) == 29 ? true : false;
  }

  /**
   * Calculates the exact number of days in between two dates. Accounts for
   * dates being in different time zones.
   * 
   * @param startDate
   * @param endDate
   * @return The exact fraction of days between two dates.
   */
  public static double getExactDaysBetween(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    return (endDate.toInstant().getEpochSeconds() - startDate.toInstant().getEpochSeconds()) / SECONDS_PER_DAY;
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate
   * @param includeStart
   * @param endDate
   * @param includeEnd
   * @param dateAdjuster
   * @return The number of days between two dates.
   */
  public static int getDaysBetween(final ZonedDateTime startDate, final boolean includeStart, final ZonedDateTime endDate, final boolean includeEnd) {
    final DateAdjuster dateAdjuster = DateAdjusters.next(startDate.toDayOfWeek());
    int result = includeStart ? 1 : 0;
    while (!dateAdjuster.adjustDate(startDate.toLocalDate()).equals(endDate.toLocalDate())) {
      result++;
    }
    return includeEnd ? result + 1 : result;
  }

  /**
   * Calculates the number of days in between two dates with the date count rule
   * specified by the DateAdjuster.
   * 
   * @param startDate
   * @param includeStart
   * @param endDate
   * @param includeEnd
   * @param dateAdjuster
   * @return The number of days between two dates.
   */
  public static int getDaysBetween(final ZonedDateTime startDate, final boolean includeStart, final ZonedDateTime endDate, final boolean includeEnd, final DateAdjuster dateAdjuster) {
    int result = includeStart ? 1 : 0;
    while (!dateAdjuster.adjustDate(startDate.toLocalDate()).equals(endDate.toLocalDate())) {
      result++;
    }
    return includeEnd ? result + 1 : result;
  }

  @Deprecated
  public static double subtract(final Date d1, final Date d2) {
    return (d1.getTime() - d2.getTime()) / MILLISECONDS_PER_DAY;
  }

  @Deprecated
  public static Date add(final Date d, final double offset) {
    final long x = d.getTime() + (long) (offset * MILLISECONDS_PER_DAY * 365.25);
    return new Date(x);
  }

  @Deprecated
  public static Date today() {
    final Calendar today = Calendar.getInstance();
    final int year = today.get(Calendar.YEAR);
    final int month = today.get(Calendar.MONTH);
    final int day = today.get(Calendar.DAY_OF_MONTH);
    final Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }

  @Deprecated
  public static Date date(final int yyyymmdd) {
    final int year = yyyymmdd / 10000;
    final int month = (yyyymmdd - 10000 * year) / 100;
    final int day = yyyymmdd - 10000 * year - 100 * month;
    final Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }
}

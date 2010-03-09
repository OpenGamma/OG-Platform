/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.Duration;
import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;

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
   * Returns endDate - startDate in years, where a year is defined as 365.25
   * days.
   * 
   * @param startDate
   *          The start date.
   * @param endDate
   *          The end date.
   * @return The difference in years.
   * @throws IllegalArgumentException
   *           If either date is null.
   */
  public static double getDifferenceInYears(final InstantProvider startDate, final InstantProvider endDate) {
    if (startDate == null)
      throw new IllegalArgumentException("Start date was null");
    if (endDate == null)
      throw new IllegalArgumentException("End date was null");
    return (double) (endDate.toInstant().toEpochMillisLong() - startDate.toInstant().toEpochMillisLong()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years.
   * 
   * @param startDate
   *          The start date.
   * @param endDate
   *          The endDate date.
   * @param daysInYear
   *          Number of days in year.
   * @return The difference in years.
   * @throws IllegalArgumentException
   *           If either date is null.
   */
  public static double getDifferenceInYears(final InstantProvider startDate, final InstantProvider endDate, final double daysInYear) {
    if (startDate == null)
      throw new IllegalArgumentException("Start date was null");
    if (endDate == null)
      throw new IllegalArgumentException("End date was null");
    return (endDate.toInstant().toEpochMillisLong() - startDate.toInstant().toEpochMillisLong()) / MILLISECONDS_PER_DAY / daysInYear;
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond. Note that the number of days in a year
   * is defined to be 365.25.
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
    final long nanos = Math.round(1e9 * SECONDS_PER_YEAR * yearFraction);
    return startDate.toInstant().plus(Duration.nanos(nanos));
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond. Note that the number of days in a year
   * is defined to be 365.25.
   * 
   * @param startDate
   *          The date to offset.
   * @param yearFraction
   *          The fraction of a year to add to the original date.
   * @return The offset date.
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction) {
    if (startDate == null)
      throw new IllegalArgumentException("Date was null");
    final Instant instant = startDate.toInstant();
    final InstantProvider offsetDate = getDateOffsetWithYearFraction(instant, yearFraction);
    return ZonedDateTime.fromInstant(offsetDate, startDate.getZone());
  }

  /**
   * /** Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond.
   * 
   * @param startDate
   *          The date to offset.
   * @param yearFraction
   *          The fraction of a year to add to the original date.
   * @param daysPerYear
   *          The number of days in a year.
   * @return The offset date.
   */
  public static InstantProvider getDateOffsetWithYearFraction(final InstantProvider startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null)
      throw new IllegalArgumentException("Date was null");
    final long nanos = Math.round(1e9 * SECONDS_PER_DAY * daysPerYear * yearFraction);
    return startDate.toInstant().plus(Duration.nanos(nanos));
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond.
   * 
   * @param startDate
   *          The date to offset.
   * @param yearFraction
   *          The fraction of a year to add to the original date.
   * @param daysPerYear
   *          The number of days in a year.
   * @return The offset date.
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null)
      throw new IllegalArgumentException("Date was null");
    final Instant instant = startDate.toInstant();
    final InstantProvider offsetDate = getDateOffsetWithYearFraction(instant, yearFraction, daysPerYear);
    return ZonedDateTime.fromInstant(offsetDate, startDate.getZone());
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
    return ZonedDateTime.from(LocalDateTime.midnight(year, month, day), TimeZone.UTC);
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
    return ZonedDateTime.from(LocalDate.of(year, month, day), LocalTime.of(hour, minutes), TimeZone.UTC);
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
    return ZonedDateTime.from(LocalDate.of(year, month, day), LocalTime.of(hour, minutes), TimeZone.of("London"));
  }

  /**
   * Determines whether the ZonedDateTime is in a leap year.
   * 
   * @param date
   * @return True if the date is in a leap year.
   */
  public static boolean isLeapYear(final ZonedDateTime date) {
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    return date.toYear().isLeap();
  }

  /**
   * Determines whether the LocalDate is in a leap year.
   * 
   * @param date
   * @return True if the date is in a leap year.
   */
  public static boolean isLeapYear(final LocalDate date) {
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    return date.toYear().isLeap();
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
    if (startDate == null)
      throw new IllegalArgumentException("Start date was null");
    if (endDate == null)
      throw new IllegalArgumentException("End date was null");
    return (endDate.toInstant().getEpochSeconds() - startDate.toInstant().getEpochSeconds()) / SECONDS_PER_DAY;
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate
   * @param includeStart
   * @param endDate
   * @param includeEnd
   * @return The number of days between two dates.
   */
  public static int getDaysBetween(final ZonedDateTime startDate, final boolean includeStart, final ZonedDateTime endDate, final boolean includeEnd) {
    if (startDate == null)
      throw new IllegalArgumentException("Start date was null");
    if (endDate == null)
      throw new IllegalArgumentException("End date was null");
    LocalDate date = startDate.toLocalDate();
    LocalDate localEndDate = endDate.toLocalDate();
    int mult = 1;
    if (startDate.isAfter(endDate)) {
      date = endDate.toLocalDate();
      localEndDate = startDate.toLocalDate();
      mult = -1;
    }
    int result = includeStart ? 1 : 0;
    while (!date.equals(localEndDate)) {
      date = date.plusDays(1);
      result++;
    }
    return mult * (includeEnd ? result : result - 1);
  }

  /**
   * @param startDate
   * @return date in "yyyymmdd" format
   */
  public static String printYYYYMMDD(ZonedDateTime date) {
    if (date == null)
      throw new IllegalArgumentException("date was null");
    String formatted = DateTimeFormatters.isoLocalDate().print(date);
    return StringUtils.remove(formatted, '-');
  }
  
  public static LocalDate previousWeekDay() {
    Clock clock = Clock.system(TimeZone.UTC);
    return previousWeekDay(clock.today());
  }

  /**
   * @param today
   * @return
   */
  public static LocalDate previousWeekDay(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("date was null");
    }
    LocalDate previous = null;
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    switch(dayOfWeek)  {
    case MONDAY:
      previous = date.minusDays(3);
      break;
    case TUESDAY:
    case WEDNESDAY:
    case THURSDAY:
    case FRIDAY:
    case SATURDAY:
      previous = date.minusDays(1);
      break;
    case SUNDAY:
      previous = date.minusDays(2);
      break;
    default :
       throw new OpenGammaRuntimeException("Unrecognised day of the week");
    }
    
    return previous;
  }

  /**
   * 
   * @param date in YYYYMMDD
   * @return
   */
  public static long getUTCEpochMilis(int date) {
    LocalDate localDate = DateTimeFormatters.basicIsoDate().parse(String.valueOf(date), LocalDate.rule()); 
    return localDate.toEpochDays() * 24 * 60 * 60 * 1000;
  }

  /**
   * 
   * @param date in YYYYMMDD
   * @return
   */
  public static ZonedDateTime toZonedDateTimeUTC(int date) {
    LocalDate localDate = DateTimeFormatters.basicIsoDate().parse(String.valueOf(date), LocalDate.rule());
    ZonedDateTime zonedDateTime = getUTCDate(localDate.getYear(), localDate.getMonthOfYear().getValue(), localDate.getDayOfMonth());
    return zonedDateTime;
  }

  // TODO useful to have methods such as # weeks between.
}

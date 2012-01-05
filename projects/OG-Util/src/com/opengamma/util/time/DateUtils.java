/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.TimeSource;
import javax.time.calendar.Calendrical;
import javax.time.calendar.Clock;
import javax.time.calendar.DateProvider;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.ISOChronology;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;
import javax.time.calendar.format.DateTimeFormatterBuilder.SignStyle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for dates.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class DateUtils {

  /**
   * The original JVM time-zone.
   */
  public static final TimeZone ORIGINAL_TIME_ZONE = Clock.systemDefaultZone().getZone();
  static {
    // essential that OpenGamm runs in a default time-zone that has no Daylight Savings
    // UTC is desirable for many other reasons, so use it here
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
  }

  /**
   * The number of seconds in one day.
   */
  public static final long SECONDS_PER_DAY = 86400L;
  /**
   * The number of days in one year (estimated as 365.25).
   */
  //TODO change this to 365.2425 to be consistent with JSR-310
  public static final double DAYS_PER_YEAR = 365.25;
  /**
   * The number of milliseconds in one day.
   */
  public static final long MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000;
  /**
   * The number of seconds in one year.
   */
  public static final long SECONDS_PER_YEAR = (long) (SECONDS_PER_DAY * DAYS_PER_YEAR);
  /**
   * The number of milliseconds in one year.
   */
  public static final long MILLISECONDS_PER_YEAR = SECONDS_PER_YEAR * 1000;
  /**
   * A formatter for yyyyMMdd.
   */
  private static final DateTimeFormatter YYYYMMDD_LOCAL_DATE;
  static {
    YYYYMMDD_LOCAL_DATE = new DateTimeFormatterBuilder()
          .appendValue(ISOChronology.yearRule(), 4, 10, SignStyle.EXCEEDS_PAD)
          .appendValue(ISOChronology.monthOfYearRule(), 2)
          .appendValue(ISOChronology.dayOfMonthRule(), 2)
          .toFormatter();
  }
  /**
   * A formatter for MM-dd
   */
  private static final DateTimeFormatter MM_DD_LOCAL_DATE;
  static {
    MM_DD_LOCAL_DATE = new DateTimeFormatterBuilder()
        .appendValue(ISOChronology.monthOfYearRule(), 2)
        .appendLiteral("-")
        .appendValue(ISOChronology.dayOfMonthRule(), 2)
        .toFormatter();
  }

  /**
   * Restricted constructor.
   */
  private DateUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the default time-zone to UTC.
   * <p>
   * This method actually does nothing, as the code is in a static initializer.
   */
  public static void initTimeZone() {
  }

  /**
   * Gets the original time-zone before it was set to UTC.
   * 
   * @return the original time-zone, not null
   */
  public static java.util.TimeZone originalTimeZone() {
    return java.util.TimeZone.getTimeZone(ORIGINAL_TIME_ZONE.getID());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final InstantProvider startDate, final InstantProvider endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (double) (endDate.toInstant().toEpochMillisLong() - startDate.toInstant().toEpochMillisLong()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final LocalDate startDate, final LocalDate endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    double diff = endDate.toLocalDate().toEpochDays() - startDate.toLocalDate().toEpochDays();
    return diff / DAYS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year-length is specified.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @param daysPerYear  the number of days in the year for calculation
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final InstantProvider startDate, final InstantProvider endDate, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (endDate.toInstant().toEpochMillisLong() - startDate.toInstant().toEpochMillisLong()) / MILLISECONDS_PER_DAY / daysPerYear;
  }

  //-------------------------------------------------------------------------
  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond. Note that the number of days in a year
   * is defined to be 365.25.
   * 
   * @param startDate  the start date, not null
   * @param yearFraction  the fraction of a year
   * @return the calculated instant, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static Instant getDateOffsetWithYearFraction(final InstantProvider startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final long nanos = Math.round(1e9 * SECONDS_PER_YEAR * yearFraction);
    return startDate.toInstant().plusNanos(nanos);
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond. Note that the number of days in a year
   * is defined to be 365.25.
   * 
   * @param startDate  the start date, not null
   * @param yearFraction  the fraction of a year
   * @return the calculated date-time, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final Instant instant = startDate.toInstant();
    final InstantProvider offsetDate = getDateOffsetWithYearFraction(instant, yearFraction);
    return ZonedDateTime.ofInstant(offsetDate, startDate.getZone());
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond.
   * 
   * @param startDate  the start date, not null
   * @param yearFraction  the fraction of a year
   * @param daysPerYear  the number of days in the year for calculation
   * @return the calculated instant, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static Instant getDateOffsetWithYearFraction(final InstantProvider startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final long nanos = Math.round(1e9 * SECONDS_PER_DAY * daysPerYear * yearFraction);
    return startDate.toInstant().plusNanos(nanos);
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the
   * yearFraction that is used does not give an integer number of seconds, it is
   * rounded to the nearest nanosecond.
   * 
   * @param startDate  the start date, not null
   * @param yearFraction  the fraction of a year
   * @param daysPerYear  the number of days in the year for calculation
   * @return the calculated date-time, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final Instant instant = startDate.toInstant();
    final InstantProvider offsetDate = getDateOffsetWithYearFraction(instant, yearFraction, daysPerYear);
    return ZonedDateTime.ofInstant(offsetDate, startDate.getZone());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a UTC date given year, month, day with the time set to midnight (UTC).
   * 
   * @param year  the year
   * @param month  the month
   * @param day  the day of month
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day) {
    return ZonedDateTime.of(LocalDateTime.ofMidnight(year, month, day), TimeZone.UTC);
  }

  /**
   * Returns a UTC date given year, month, day, hour and minutes.
   * 
   * @param year  the year
   * @param month  the month
   * @param day  the day of month
   * @param hour  the hour
   * @param minute  the minute
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day, final int hour, final int minute) {
    return ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), TimeZone.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the exact number of 24 hour days in between two dates.
   * Accounts for dates being in different time zones.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @return the exact fraction of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static double getExactDaysBetween(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    // TODO: was 24-hour days intended?
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (endDate.toInstant().getEpochSeconds() - startDate.toInstant().getEpochSeconds()) / (double) SECONDS_PER_DAY;
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final DateProvider startDate, final DateProvider endDate) {
    return getDaysBetween(startDate, true, endDate, false);
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate  the start date, not null
   * @param includeStart  whether to include the start
   * @param endDate  the end date, not null
   * @param includeEnd  whether to include the end
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final DateProvider startDate, final boolean includeStart, final DateProvider endDate, final boolean includeEnd) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    int daysBetween = (int) Math.abs(startDate.toLocalDate().toEpochDays() - endDate.toLocalDate().toEpochDays());
    if (includeStart && includeEnd) {
      daysBetween++;
    } else if (!includeStart && !includeEnd) {
      daysBetween--;
    }
    return daysBetween;
  }

  /**
   * Prints the date in yyyyMMdd format.
   * @param date  the date, not null
   * @return the date as a string, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static String printYYYYMMDD(Calendrical date) {
    if (date == null) {
      throw new IllegalArgumentException("date was null");
    }
    return YYYYMMDD_LOCAL_DATE.print(date);
  }

  /**
   * Prints the date in MM-dd format.
   * @param date  the date, not null
   * @return the date as a string, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static String printMMDD(Calendrical date) {
    if (date == null) {
      throw new IllegalArgumentException("date was null");
    }
    return MM_DD_LOCAL_DATE.print(date);
  }

  /**
   * Gets the previous Monday to Friday week-day before now.
   * @return the date, not null
   */
  public static LocalDate previousWeekDay() {
    Clock clock = Clock.system(TimeZone.UTC);
    return previousWeekDay(clock.today());
  }

  /**
   * Gets the next Monday to Friday week-day after now.
   * @return the date, not null
   */
  public static LocalDate nextWeekDay() {
    Clock clock = Clock.system(TimeZone.UTC);
    return nextWeekDay(clock.today());
  }

  /**
   * Gets the next Monday to Friday week-day after now.
   * @param startDate  the date to start from
   * @return the date, not null
   */
  public static LocalDate nextWeekDay(LocalDate startDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("date was null");
    }
    LocalDate next = null;
    DayOfWeek dayOfWeek = startDate.getDayOfWeek();
    switch (dayOfWeek) {
      case FRIDAY:
        next = startDate.plusDays(3);
        break;
      case SATURDAY:
        next = startDate.plusDays(2);
        break;
      case MONDAY:
      case TUESDAY:
      case WEDNESDAY:
      case THURSDAY:
      case SUNDAY:
        next = startDate.plusDays(1);
        break;
      default:
        throw new OpenGammaRuntimeException("Unrecognised day of the week");
    }
    return next;
  }

  /**
   * Gets the previous Monday to Friday week-day before now.
   * @param startDate  the date to start from
   * @return the date, not null
   */
  public static LocalDate previousWeekDay(LocalDate startDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("date was null");
    }
    LocalDate previous = null;
    DayOfWeek dayOfWeek = startDate.getDayOfWeek();
    switch (dayOfWeek) {
      case MONDAY:
        previous = startDate.minusDays(3);
        break;
      case TUESDAY:
      case WEDNESDAY:
      case THURSDAY:
      case FRIDAY:
      case SATURDAY:
        previous = startDate.minusDays(1);
        break;
      case SUNDAY:
        previous = startDate.minusDays(2);
        break;
      default:
        throw new OpenGammaRuntimeException("Unrecognised day of the week");
    }
    return previous;
  }

  /**
   * Converts a date in integer YYYYMMDD representation to epoch millis.
   * @param date in integer YYYYMMDD representation
   * @return the epoch millis
   */
  public static long getUTCEpochMilis(int date) {
    LocalDate localDate = YYYYMMDD_LOCAL_DATE.parse(String.valueOf(date), LocalDate.rule());
    return localDate.toEpochDays() * 24 * 60 * 60 * 1000;
  }

  /**
   * Converts a date in integer YYYYMMDD representation to a UTC date-time.
   * @param date in integer YYYYMMDD representation
   * @return the date-time, not null
   */
  public static ZonedDateTime toZonedDateTimeUTC(int date) {
    LocalDate localDate = YYYYMMDD_LOCAL_DATE.parse(String.valueOf(date), LocalDate.rule());
    ZonedDateTime zonedDateTime = getUTCDate(localDate.getYear(), localDate.getMonthOfYear().getValue(), localDate.getDayOfMonth());
    return zonedDateTime;
  }

  /**
   * Converts a date in integer YYYYMMDD representation to a date.
   * @param date in integer YYYYMMDD representation
   * @return the date, not null
   */
  public static LocalDate toLocalDate(int date) {
    return toLocalDate(String.valueOf(date));
  }

  /**
   * Converts a date in string YYYYMMDD representation to epoch millis.
   * @param date in YYYYMMDD representation, not null
   * @return the date
   */
  public static LocalDate toLocalDate(String date) {
    ArgumentChecker.notNull(date, "date");
    return YYYYMMDD_LOCAL_DATE.parse(date, LocalDate.rule());
  }
  
  /**
   * Constructs a LocalDate from a <code>java.util.Date</code>
   * using exactly the same field values.
   * <p>
   * Each field is queried from the Date and assigned to the LocalDate.
   * This is useful if you have been using the Date as a local date,
   * ignoring the zone.
   *
   * @param date  the Date to extract fields from
   * @return the created LocalDate
   * @throws IllegalArgumentException if the calendar is null
   * @throws IllegalArgumentException if the date is invalid for the ISO chronology
   */
  @SuppressWarnings("deprecation")
  public static LocalDate fromDateFields(java.util.Date date) {
    if (date == null) {
      throw new IllegalArgumentException("The date must not be null");
    }
    return LocalDate.of(
        date.getYear() + 1900,
        date.getMonth() + 1,
        date.getDate()
    );
  }

  //-------------------------------------------------------------------------
  // REVIEW kirk 2010-04-29 -- This is a candidate for inclusion as an easier thing in JSR-310.
  /**
   * Creates a clock with a fixed time-source and UTC time-zone.
   * @param instantProvider  the instant to be provided by the clock, not null
   * @return the clock, not null
   */
  public static Clock fixedClockUTC(InstantProvider instantProvider) {
    TimeSource timeSource = TimeSource.fixed(instantProvider);
    Clock clock = Clock.clock(timeSource, TimeZone.UTC);
    return clock;
  }

  // TODO useful to have methods such as # weeks between.

  //-------------------------------------------------------------------------
  /**
   * Parses an instant.
   * 
   * @param text  the instant text to parse
   * @return the instant, not null
   */
  public static Instant parseInstant(String text) {
    return OffsetDateTime.parse(text).toInstant();
  }

}

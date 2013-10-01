/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.YEAR;
import static org.threeten.bp.temporal.ChronoUnit.DAYS;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.types.FudgeDate;
import org.threeten.bp.Clock;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.SignStyle;
import org.threeten.bp.temporal.Temporal;

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
  public static final ZoneId ORIGINAL_TIME_ZONE = Clock.systemDefaultZone().getZone();
  static {
    // essential that OpenGamm runs in a default time-zone that has no Daylight Savings
    // UTC is desirable for many other reasons, so use it here
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
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
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendValue(MONTH_OF_YEAR, 2)
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter();
  }
  /**
   * A formatter for MM-dd
   */
  private static final DateTimeFormatter MM_DD_LOCAL_DATE;
  static {
    MM_DD_LOCAL_DATE = new DateTimeFormatterBuilder()
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral("-")
        .appendValue(DAY_OF_MONTH, 2)
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
  public static TimeZone originalTimeZone() {
    return TimeZone.getTimeZone(ORIGINAL_TIME_ZONE.getId());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final Instant startDate, final Instant endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (double) (endDate.toEpochMilli() - startDate.toEpochMilli()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (double) (endDate.toInstant().toEpochMilli() - startDate.toInstant().toEpochMilli()) / MILLISECONDS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year is defined as 365.25 days.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
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
    double diff = endDate.toEpochDay() - startDate.toEpochDay();
    return diff / DAYS_PER_YEAR;
  }

  /**
   * Returns endDate - startDate in years, where a year-length is specified.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @param daysPerYear the number of days in the year for calculation
   * @return the difference in years
   * @throws IllegalArgumentException if either date is null
   */
  public static double getDifferenceInYears(final Instant startDate, final Instant endDate, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    return (endDate.toEpochMilli() - startDate.toEpochMilli()) / MILLISECONDS_PER_DAY / daysPerYear;
  }

  //-------------------------------------------------------------------------
  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond. Note that the
   * number of days in a year is defined to be 365.25.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @return the calculated instant, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static Instant getDateOffsetWithYearFraction(final Instant startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final long nanos = Math.round(1e9 * SECONDS_PER_YEAR * yearFraction);
    return startDate.plusNanos(nanos);
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond. Note that the
   * number of days in a year is defined to be 365.25.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @return the calculated date-time, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final Instant instant = startDate.toInstant();
    final Instant offsetDate = getDateOffsetWithYearFraction(instant, yearFraction);
    return ZonedDateTime.ofInstant(offsetDate, startDate.getZone());
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @param daysPerYear the number of days in the year for calculation
   * @return the calculated instant, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static Instant getDateOffsetWithYearFraction(final Instant startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final long nanos = Math.round(1e9 * SECONDS_PER_DAY * daysPerYear * yearFraction);
    return startDate.plusNanos(nanos);
  }

  /**
   * Method that allows a fraction of a year to be added to a date. If the yearFraction that is used does not give an integer number of seconds, it is rounded to the nearest nanosecond.
   * 
   * @param startDate the start date, not null
   * @param yearFraction the fraction of a year
   * @param daysPerYear the number of days in the year for calculation
   * @return the calculated date-time, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static ZonedDateTime getDateOffsetWithYearFraction(final ZonedDateTime startDate, final double yearFraction, final double daysPerYear) {
    if (startDate == null) {
      throw new IllegalArgumentException("Date was null");
    }
    final Instant instant = startDate.toInstant();
    final Instant offsetDate = getDateOffsetWithYearFraction(instant, yearFraction, daysPerYear);
    return ZonedDateTime.ofInstant(offsetDate, startDate.getZone());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a UTC date given year, month, day with the time set to midnight (UTC).
   * 
   * @param year the year
   * @param month the month
   * @param day the day of month
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day) {
    return LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC);
  }

  /**
   * Returns a UTC date given year, month, day, hour and minutes.
   * 
   * @param year the year
   * @param month the month
   * @param day the day of month
   * @param hour the hour
   * @param minute the minute
   * @return the date-time, not null
   */
  public static ZonedDateTime getUTCDate(final int year, final int month, final int day, final int hour, final int minute) {
    return ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the exact number of 24 hour days in between two dates. Accounts for dates being in different time zones.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
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
    return (endDate.toInstant().getEpochSecond() - startDate.toInstant().getEpochSecond()) / (double) SECONDS_PER_DAY;
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate the start date, not null
   * @param endDate the end date, not null
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final Temporal startDate, final Temporal endDate) {
    return getDaysBetween(startDate, true, endDate, false);
  }

  /**
   * Calculates the number of days in between two dates.
   * 
   * @param startDate the start date, not null
   * @param includeStart whether to include the start
   * @param endDate the end date, not null
   * @param includeEnd whether to include the end
   * @return the number of days between two dates
   * @throws IllegalArgumentException if the date is null
   */
  public static int getDaysBetween(final Temporal startDate, final boolean includeStart, final Temporal endDate, final boolean includeEnd) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date was null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date was null");
    }
    int daysBetween = (int) Math.abs(DAYS.between(startDate, endDate));
    if (includeStart && includeEnd) {
      daysBetween++;
    } else if (!includeStart && !includeEnd) {
      daysBetween--;
    }
    return daysBetween;
  }

  /**
   * Prints the date in yyyyMMdd format.
   * 
   * @param date the date, not null
   * @return the date as a string, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static String printYYYYMMDD(Temporal date) {
    if (date == null) {
      throw new IllegalArgumentException("date was null");
    }
    return YYYYMMDD_LOCAL_DATE.format(date);
  }

  /**
   * Prints the date in MM-dd format.
   * 
   * @param date the date, not null
   * @return the date as a string, not null
   * @throws IllegalArgumentException if the date is null
   */
  public static String printMMDD(Temporal date) {
    if (date == null) {
      throw new IllegalArgumentException("date was null");
    }
    return MM_DD_LOCAL_DATE.format(date);
  }

  /**
   * Gets the previous Monday to Friday week-day before now.
   * 
   * @return the date, not null
   */
  public static LocalDate previousWeekDay() {
    Clock clock = Clock.systemUTC();
    return previousWeekDay(LocalDate.now(clock));
  }

  /**
   * Gets the next Monday to Friday week-day after now.
   * 
   * @return the date, not null
   */
  public static LocalDate nextWeekDay() {
    Clock clock = Clock.systemUTC();
    return nextWeekDay(LocalDate.now(clock));
  }

  /**
   * Gets the next Monday to Friday week-day after now.
   * 
   * @param startDate the date to start from
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
   * 
   * @param startDate the date to start from
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
   * 
   * @param date in integer YYYYMMDD representation
   * @return the epoch millis
   */
  public static long getUTCEpochMilis(int date) {
    LocalDate localDate = LocalDate.parse(String.valueOf(date), YYYYMMDD_LOCAL_DATE);
    return localDate.toEpochDay() * 24 * 60 * 60 * 1000;
  }

  /**
   * Converts a date in integer YYYYMMDD representation to a UTC date-time.
   * 
   * @param date in integer YYYYMMDD representation
   * @return the date-time, not null
   */
  public static ZonedDateTime toZonedDateTimeUTC(int date) {
    LocalDate localDate = LocalDate.parse(String.valueOf(date), YYYYMMDD_LOCAL_DATE);
    ZonedDateTime zonedDateTime = getUTCDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    return zonedDateTime;
  }

  /**
   * Converts a date in integer YYYYMMDD representation to a date.
   * 
   * @param date in integer YYYYMMDD representation
   * @return the date, not null
   */
  public static LocalDate toLocalDate(int date) {
    return toLocalDate(String.valueOf(date));
  }

  /**
   * Converts a date in string YYYYMMDD representation to epoch millis.
   * 
   * @param date in YYYYMMDD representation, not null
   * @return the date
   */
  public static LocalDate toLocalDate(String date) {
    ArgumentChecker.notNull(date, "date");
    return LocalDate.parse(date, YYYYMMDD_LOCAL_DATE);
  }

  /**
   * Constructs a LocalDate from a <code>java.util.Date</code> using exactly the same field values.
   * <p>
   * Each field is queried from the Date and assigned to the LocalDate. This is useful if you have been using the Date as a local date, ignoring the zone.
   * 
   * @param date the Date to extract fields from
   * @return the created LocalDate
   * @throws IllegalArgumentException if the calendar is null
   * @throws IllegalArgumentException if the date is invalid for the ISO chronology
   */
  @SuppressWarnings("deprecation")
  public static LocalDate fromDateFields(java.util.Date date) {
    if (date == null) {
      throw new IllegalArgumentException("The date must not be null");
    }
    return LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
  }

  /**
   * Constructs a LocalDate from a Function Requirement / Input passed over the wire via {@link FudgeMsg} <p>
   * Example usage: LocalDate nextDividendDate = DateUtils.toLocalDate(inputs.getValue(MarketDataRequirementNames.NEXT_DIVIDEND_DATE));
   * 
   * @param date an Object
   * @return the created LocalDate
   * @throws IllegalArgumentException if the date is not a recognized type
   */
  public static LocalDate toLocalDate(Object date) {
    if (date instanceof LocalDate) {
      return (LocalDate) date;
    }
    if (date instanceof FudgeDate) {
      return ((FudgeDate) date).toLocalDate();
    }
    throw new IllegalArgumentException(date.toString() + " is not a date");
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a clock with a fixed time-source and UTC time-zone.
   * 
   * @param instant the instant to be provided by the clock, not null
   * @return the clock, not null
   */
  public static Clock fixedClockUTC(Instant instant) {
    return Clock.fixed(instant, ZoneOffset.UTC);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the estimated duration of the period.
   * 
   * @param period the period to estimate the duration of, not null
   * @return the estimated duration, not null
   */
  public static Duration estimatedDuration(Period period) {
    Duration monthsDuration = MONTHS.getDuration().multipliedBy(period.toTotalMonths());
    Duration daysDuration = DAYS.getDuration().multipliedBy(period.getDays());
    return monthsDuration.plus(daysDuration);
  }

  /**
   * Converts GregorianCalendar to ZonedDateTime
   * 
   * @param calendar the calendar, not null
   * @return the zoned-date-time, not null
   */
  public static ZonedDateTime toZonedDateTime(GregorianCalendar calendar) {
    ZoneId zone = ZoneId.of(calendar.getTimeZone().getID());
    Instant instant = Instant.ofEpochMilli(calendar.getTimeInMillis());
    return ZonedDateTime.ofInstant(instant, zone);
  }

  /**
   * Converts a string to a period, allowing the old format of {@code PT0S} for {@code P0D}.
   * 
   * @param period the period to parse, not null
   * @return the parsed period, not null
   * @deprecated Don't rely on this, fix the source of data where the PT0S values are coming from
   */
  @Deprecated
  public static Period toPeriod(final String period) {
    if ("PT0S".equals(period)) {
      return Period.ZERO;
    } else {
      return Period.parse(period);
    }
  }

}

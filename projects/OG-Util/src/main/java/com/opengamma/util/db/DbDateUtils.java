/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Utility class for dates and their interaction with JDBC.
 */
public class DbDateUtils {

  static {
    DateUtils.initTimeZone();
  }

  /**
   * The maximum SQL date, used as far-future in the database.
   */
  @SuppressWarnings("deprecation")
  public static final Date MAX_SQL_DATE = new Date(9999 - 1900, Calendar.DECEMBER, 31);
  /**
   * The minimum SQL date, used as far-past in the database.
   */
  @SuppressWarnings("deprecation")
  public static final Date MIN_SQL_DATE = new Date(1800 - 1900, Calendar.JANUARY, 1);
  /**
   * The maximum SQL time-stamp, used as far-future in the database.
   */
  @SuppressWarnings("deprecation")
  public static final Timestamp MAX_SQL_TIMESTAMP = new Timestamp(9999 - 1900, Calendar.DECEMBER, 31, 23, 59, 59, 0);
  /**
   * The effective maximum SQL time-stamp, used when reading to avoid time zone issues.
   */
  @SuppressWarnings("deprecation")
  public static final Timestamp EFFECTIVE_MAX_TIMESTAMP = new Timestamp(9990 - 1900, Calendar.DECEMBER, 31, 23, 59, 59, 0);
  /**
   * The maximum instant, used as far-future in the database.
   */
  public static final Instant MAX_INSTANT = fromSqlTimestamp(MAX_SQL_TIMESTAMP);

  /**
   * Restricted constructor.
   */
  protected DbDateUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a time-stamp from an {@code InstantProvider}.
   * 
   * @param instant  the instant to convert, not null
   * @return the SQL time-stamp, not null
   */
  public static Timestamp toSqlTimestamp(Instant instant) {
    ArgumentChecker.notNull(instant, "instant");
    OffsetDateTime utc = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    return toSqlDateTime(utc.toLocalDateTime());
  }

  /**
   * Creates an {@code Instant} from an SQL time-stamp.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the instant, null if far-future
   */
  public static Instant fromSqlTimestamp(Timestamp timestamp) {
    LocalDateTime ldt = fromSqlDateTime(timestamp);
    return ldt.atOffset(ZoneOffset.UTC).toInstant();
  }

  /**
   * Creates an {@code Instant} from an SQL time-stamp treating null as far-future.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the instant, null if far-future
   */
  public static Instant fromSqlTimestampNullFarFuture(Timestamp timestamp) {
    if (timestamp.compareTo(EFFECTIVE_MAX_TIMESTAMP) > 0) {
      return null;
    }
    return fromSqlTimestamp(timestamp);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a SQL date from a {@code DateTimeProvider}.
   * 
   * @param dateTime  the date-time to convert, not null
   * @return the SQL date, not null
   */
  @SuppressWarnings("deprecation")
  public static Timestamp toSqlDateTime(LocalDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    return new Timestamp(
        dateTime.getYear() - 1900, dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(),
        dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano());
  }

  /**
   * Creates a {@code LocalDateTime} from an SQL time-stamp.
   * This is used when the time-stamp represents a local date-time rather than an instant.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the date-time, null if far-future
   */
  @SuppressWarnings("deprecation")
  public static LocalDateTime fromSqlDateTime(Timestamp timestamp) {
    ArgumentChecker.notNull(timestamp, "timestamp");
    return LocalDateTime.of(
        timestamp.getYear() + 1900, timestamp.getMonth() + 1, timestamp.getDate(),
        timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getNanos());
  }

  /**
   * Creates a {@code LocalDateTime} from an SQL time-stamp.
   * This is used when the time-stamp represents a local date-time rather than an instant.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the date-time, null if far-future
   */
  public static LocalDateTime fromSqlDateTimeNullFarFuture(Timestamp timestamp) {
    if (timestamp.compareTo(EFFECTIVE_MAX_TIMESTAMP) > 0) {
      return null;
    }
    return fromSqlDateTime(timestamp);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param date  the date to convert, not null
   * @return the SQL date, not null
   */
  @SuppressWarnings("deprecation")
  public static Date toSqlDate(LocalDate date) {
    ArgumentChecker.notNull(date, "dateProvider");
    return new Date(date.getYear() - 1900, date.getMonthValue() - 1, date.getDayOfMonth());
  }

  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param date  the date to convert, null returns max
   * @return the SQL date, not null
   */
  public static Date toSqlDateNullFarFuture(LocalDate date) {
    if (date == null) {
      return MAX_SQL_DATE;
    }
    return toSqlDate(date);
  }

  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param date  the date to convert, null returns max
   * @return the SQL date, not null
   */
  public static Date toSqlDateNullFarPast(LocalDate date) {
    if (date == null) {
      return MIN_SQL_DATE;
    }
    return toSqlDate(date);
  }

  /**
   * Creates a {@code LocalDate} from a SQL date.
   * 
   * @param date  the SQL date to convert, not null
   * @return the date, not null
   */
  @SuppressWarnings("deprecation")
  public static LocalDate fromSqlDate(Date date) {
    ArgumentChecker.notNull(date, "date");
    return LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
  }

  /**
   * Creates a {@code LocalDate} from a SQL date.
   * 
   * @param date  the SQL date to convert, not null
   * @return the date, null if far future
   */
  public static LocalDate fromSqlDateNullFarFuture(Date date) {
    if (date.equals(MAX_SQL_DATE)) {
      return null;
    }
    return fromSqlDate(date);
  }

  /**
   * Creates a {@code LocalDate} from a SQL date.
   * 
   * @param date  the SQL date to convert, not null
   * @return the date, null if far past
   */
  public static LocalDate fromSqlDateNullFarPast(Date date) {
    if (date.equals(MIN_SQL_DATE)) {
      return null;
    }
    return fromSqlDate(date);
  }

  /**
   * Creates a {@code LocalDate} from a possibly null SQL date.
   * 
   * @param date  the SQL date to convert, null returns null
   * @return the date, may be null
   */
  public static LocalDate fromSqlDateAllowNull(Date date) {
    return (date != null ? fromSqlDate(date) : null);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a SQL timestamp from a {@code TimeProvider}.
   * <p>
   * This method is needed to be able to pass nano-of-second to the database when dealing with times.
   * 
   * @param time  the time to convert, not null
   * @return the SQL time, not null
   */
  @SuppressWarnings("deprecation")
  public static Timestamp toSqlTimestamp(LocalTime time) {
    ArgumentChecker.notNull(time, "time");
    return new Timestamp(70, 0, 1, time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
  }

  /**
   * Creates a SQL time from a {@code TimeProvider}.
   * <p>
   * This method does not handle nano-of-second, and should not normally be used.
   * 
   * @param time  the time to convert, not null
   * @return the SQL time, not null
   */
  @SuppressWarnings("deprecation")
  public static Time toSqlTime(LocalTime time) {
    ArgumentChecker.notNull(time, "time");
    return new Time(time.getHour(), time.getMinute(), time.getSecond());
  }

  /**
   * Creates a {@code LocalTime} from an SQL time.
   * 
   * @param time  the SQL time to convert, not null
   * @return the time, not null
   */
  @SuppressWarnings("deprecation")
  public static LocalTime fromSqlTime(Time time) {
    ArgumentChecker.notNull(time, "time");
    return LocalTime.of(time.getHours(), time.getMinutes(), time.getSeconds());
  }

  /**
   * Creates a {@code LocalTime} from an SQL timestamp.
   * <p>
   * This method is needed to be able to retrieve nanoseconds from the database when dealing with times.
   * 
   * @param timestamp  the SQL timestamp to convert, not null
   * @return the time, not null
   */
  @SuppressWarnings("deprecation")
  public static LocalTime fromSqlTime(Timestamp timestamp) {
    ArgumentChecker.notNull(timestamp, "timestamp");
    return LocalTime.of(timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getNanos());
  }

}

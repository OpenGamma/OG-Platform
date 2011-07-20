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

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.DateProvider;
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for dates and their interaction with JDBC.
 */
public class DbDateUtils {

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
   * @param instantProvider  the instant to convert, not null
   * @return the SQL time-stamp, not null
   */
  public static Timestamp toSqlTimestamp(InstantProvider instantProvider) {
    ArgumentChecker.notNull(instantProvider, "instantProvider");
    Instant instant = Instant.of(instantProvider);
    Timestamp timestamp = new Timestamp(instant.toEpochMillisLong());
    timestamp.setNanos(instant.getNanoOfSecond());
    return timestamp;
  }

  /**
   * Creates an {@code Instant} from an SQL time-stamp.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the instant, null if far-future
   */
  public static Instant fromSqlTimestamp(Timestamp timestamp) {
    ArgumentChecker.notNull(timestamp, "timestamp");
    long seconds = timestamp.getTime() / 1000;
    int nanos = timestamp.getNanos();
    return Instant.ofEpochSeconds(seconds, nanos);
  }

  /**
   * Creates an {@code Instant} from an SQL time-stamp treating null as far-future.
   * 
   * @param timestamp  the SQL time-stamp to convert, not null
   * @return the instant, null if far-future
   */
  public static Instant fromSqlTimestampNullFarFuture(Timestamp timestamp) {
    if (timestamp.equals(MAX_SQL_TIMESTAMP)) {
      return null;
    }
    return fromSqlTimestamp(timestamp);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a SQL date from a {@code DateTimeProvider}.
   * 
   * @param dateTimeProvider  the date-time to convert, not null
   * @return the SQL date, not null
   */
  @SuppressWarnings("deprecation")
  public static Timestamp toSqlDateTime(DateTimeProvider dateTimeProvider) {
    ArgumentChecker.notNull(dateTimeProvider, "dateTimeProvider");
    LocalDateTime dt = LocalDateTime.of(dateTimeProvider);
    return new Timestamp(
        dt.getYear() - 1900, dt.getMonthOfYear().getValue() - 1, dt.getDayOfMonth(),
        dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute(), dt.getNanoOfSecond());
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
    if (timestamp.equals(MAX_SQL_TIMESTAMP)) {
      return null;
    }
    return fromSqlDateTime(timestamp);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param dateProvider  the date to convert, not null
   * @return the SQL date, not null
   */
  @SuppressWarnings("deprecation")
  public static Date toSqlDate(DateProvider dateProvider) {
    ArgumentChecker.notNull(dateProvider, "dateProvider");
    LocalDate date = LocalDate.of(dateProvider);
    return new Date(date.getYear() - 1900, date.getMonthOfYear().getValue() - 1, date.getDayOfMonth());
  }

  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param dateProvider  the date to convert, null returns max
   * @return the SQL date, not null
   */
  public static Date toSqlDateNullFarFuture(DateProvider dateProvider) {
    if (dateProvider == null) {
      return MAX_SQL_DATE;
    }
    return toSqlDate(dateProvider);
  }

  /**
   * Creates a SQL date from a {@code DateProvider}.
   * 
   * @param dateProvider  the date to convert, null returns max
   * @return the SQL date, not null
   */
  public static Date toSqlDateNullFarPast(DateProvider dateProvider) {
    if (dateProvider == null) {
      return MIN_SQL_DATE;
    }
    return toSqlDate(dateProvider);
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
   * Creates a SQL time from a {@code TimeProvider}.
   * 
   * @param timeProvider  the time to convert, not null
   * @return the SQL time, not null
   */
  @SuppressWarnings("deprecation")
  public static Time toSqlTime(TimeProvider timeProvider) {
    ArgumentChecker.notNull(timeProvider, "timeProvider");
    LocalTime time = LocalTime.of(timeProvider);
    return new Time(time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
  }

  /**
   * Creates a SQL timestamp from a {@code TimeProvider}.
   * <p>
   * This method is needed to be able to pass nanoseconds to the database when dealing with times.
   * 
   * @param timeProvider  the time to convert, not null
   * @return the SQL time, not null
   */
  @SuppressWarnings("deprecation")
  public static Timestamp toSqlTimestamp(TimeProvider timeProvider) {
    ArgumentChecker.notNull(timeProvider, "timeProvider");
    LocalTime time = LocalTime.of(timeProvider);
    return new Timestamp(70, 0, 1, time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute(), time.getNanoOfSecond());
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

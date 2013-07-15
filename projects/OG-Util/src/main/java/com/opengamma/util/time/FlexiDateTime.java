/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAccessor;
import org.threeten.bp.temporal.TemporalQueries;

import com.google.common.base.Objects;
import com.opengamma.util.ArgumentChecker;

/**
 * A flexible date-time representation.
 * <p>
 * A {@code FlexiDateTime} always stores a date.
 * In addition is can optionally store a time and a time-zone.
 * The time-zone can be an offset-based time-zone.
 * This combination allows the flexi date-time to represent a {@code LocalDate},
 * {@code LocalDateTime}, {@code OffsetDateTime} or {@code ZonedDateTime}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class FlexiDateTime implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The date, not null.
   */
  private final LocalDate _date;
  /**
   * The time, may be null.
   */
  private final LocalTime _time;
  /**
   * The zone, may be null.
   */
  private final ZoneId _zone;

  /**
   * Obtains a flexi date-time, specifying the local date.
   * <p>
   * This factory is strict and requires the date.
   * 
   * @param date  the date, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime of(LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return new FlexiDateTime(date, null, null);
  }

  /**
   * Obtains a flexi date-time, specifying the local date and time.
   * <p>
   * This factory is strict and requires both the date and time.
   * 
   * @param date  the date, not null
   * @param time  the time, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime of(LocalDate date, LocalTime time) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(time, "time");
    return new FlexiDateTime(date, time, null);
  }

  /**
   * Obtains a flexi date-time, specifying the local date-time.
   * <p>
   * This factory is strict and requires the date-time.
   * 
   * @param dateTime  the date-time, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime of(LocalDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    return new FlexiDateTime(dateTime.toLocalDate(), dateTime.toLocalTime(), null);
  }

  /**
   * Obtains a flexi date-time, specifying the offset date-time.
   * <p>
   * This factory is strict and requires the date-time.
   * 
   * @param dateTime  the date-time, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime of(OffsetDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    return new FlexiDateTime(dateTime.toLocalDate(), dateTime.toLocalTime(), dateTime.getOffset());
  }

  /**
   * Obtains a flexi date-time, specifying the zoned date-time.
   * <p>
   * This factory is strict and requires the date-time.
   * 
   * @param dateTime  the date-time, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime of(ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    return new FlexiDateTime(dateTime.toLocalDate(), dateTime.toLocalTime(), dateTime.getZone());
  }

  /**
   * Obtains a flexi date-time, specifying an arbitrary temporal.
   * <p>
   * This factory examines the temporal, extracting the date, time, offset and zone.
   * 
   * @param temporal  the temporal, not null
   * @return the date-time, not null
   */
  public static FlexiDateTime from(TemporalAccessor temporal) {
    ArgumentChecker.notNull(temporal, "calendrical");
    LocalDate date = LocalDate.from(temporal);
    LocalTime time;
    try {
      time = LocalTime.from(temporal);
    } catch (Exception ex) {
      time = null;
    }
    ZoneId zone = temporal.query(TemporalQueries.zone());
    return new FlexiDateTime(date, time, zone);
  }

  /**
   * Obtains a flexi date-time, specifying the date and optionally the time.
   * <p>
   * This factory requires the date.
   * 
   * @param date  the date, not null
   * @param time  the time, may be null
   * @return the date-time, not null
   */
  public static FlexiDateTime ofLenient(LocalDate date, OffsetTime time) {
    ArgumentChecker.notNull(date, "date");
    if (time != null) {
      return new FlexiDateTime(date, time.toLocalTime(), time.getOffset());
    }
    return new FlexiDateTime(date, null, null);
  }

  /**
   * Obtains a flexi date-time, specifying the date and optionally specifying the time and zone.
   * <p>
   * This factory requires the date. The time is only mandatory if the zone is non-null.
   * 
   * @param date  the date, not null
   * @param time  the time, may be null unless the zone is non-null
   * @param zone  the zone, may be null
   * @return the date-time, not null
   */
  public static FlexiDateTime ofLenient(LocalDate date, LocalTime time, ZoneId zone) {
    ArgumentChecker.notNull(date, "date");
    return new FlexiDateTime(date, time, zone);
  }

  /**
   * Creates a flexi date-time for the specified date, time and offset,
   * handling null inputs by returning null.
   *
   * @param date  the date, may be null
   * @param time  the time, may be null
   * @return the date-time, may be null
   */
  public static FlexiDateTime create(LocalDate date, OffsetTime time) {
    if (date != null) {
      return FlexiDateTime.create(date, time);
    } else {
      return null;
    }
  }

  /**
   * Creates a flexi date-time for the specified date, time and offset,
   * handling null inputs by returning null.
   *
   * @param date  the date, may be null
   * @param time  the time, may be null
   * @param zone  the zone, may be null
   * @return the date-time, may be null
   */
  public static FlexiDateTime create(LocalDate date, LocalTime time, ZoneId zone) {
    if (date != null) {
      return FlexiDateTime.create(date, time, zone);
    } else {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new instance.
   */
  private FlexiDateTime(final LocalDate date, LocalTime time, ZoneId zone) {
    ArgumentChecker.notNull(date, "date");
    if (zone != null) {
      ArgumentChecker.notNull(time, "time");
    }
    _date = date;
    _time = time;
    _zone = zone;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the local date.
   * 
   * @return the date, not null
   */
  public LocalDate getDate() {
    return _date;
  }

  /**
   * Gets the optional local time.
   * 
   * @return the time, may be null
   */
  public LocalTime getTime() {
    return _time;
  }

  /**
   * Gets the optional time-zone.
   * 
   * @return the time-zone, may be null
   */
  public ZoneId getZone() {
    return _zone;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts to a local date-time, only if the time is available.
   * <p>
   * If the time is not available, an exception is thrown.
   * 
   * @return the local date-time, not null
   */
  public LocalDateTime toLocalDateTime() {
    return LocalDateTime.of(_date, _time);
  }

  /**
   * Converts to a local date-time, specifying a default time to use if none is stored.
   * 
   * @param defaultTime  the default time, not null
   * @return the local date-time, not null
   */
  public LocalDateTime toLocalDateTime(LocalTime defaultTime) {
    ArgumentChecker.notNull(defaultTime, "defaultTime");
    return LocalDateTime.of(_date, Objects.firstNonNull(_time, defaultTime));
  }

  /**
   * Converts to an offset time, only if all data is available.
   * <p>
   * If the time or zone is not available, an exception is thrown.
   * 
   * @return the offset date-time, not null
   */
  public OffsetTime toOffsetTime() {
    return toZonedDateTime().toOffsetDateTime().toOffsetTime();
  }

  /**
   * Converts to an offset date-time, only if all data is available.
   * <p>
   * If the time or zone is not available, an exception is thrown.
   * 
   * @return the offset date-time, not null
   */
  public OffsetDateTime toOffsetDateTime() {
    return toZonedDateTime().toOffsetDateTime();
  }

  /**
   * Converts to a zoned date-time, only if all data is available.
   * <p>
   * If the time or zone is not available, an exception is thrown.
   * 
   * @return the zoned date-time, not null
   */
  public ZonedDateTime toZonedDateTime() {
    return _date.atTime(_time).atZone(_zone);
  }

  /**
   * Converts to a zoned date-time.
   * <p>
   * Conversion requires defaults for the time and zone.
   * 
   * @param defaultTime  the default time, not null
   * @param defaultZone  the default zone, not null
   * @return the zoned date-time, not null
   */
  public ZonedDateTime toZonedDateTime(LocalTime defaultTime, ZoneId defaultZone) {
    ArgumentChecker.notNull(defaultTime, "defaultTime");
    ArgumentChecker.notNull(defaultZone, "defaultZone");
    return toLocalDateTime(defaultTime).atZone(Objects.firstNonNull(_zone, defaultZone));
  }

  /**
   * Converts to the best representation of the date-time.
   * <p>
   * This will return the best option of {@code LocalDate}, {@code LocalDateTime},
   * {@code OffsetDateTime} or {@code ZonedDateTime} depending on the stored data.
   * 
   * @return the best representation, not null
   */
  public Temporal toBest() {
    if (_zone != null) {
      ZonedDateTime zdt = _date.atTime(_time).atZone(_zone);
      return (_zone instanceof ZoneOffset ? zdt.toOffsetDateTime() : zdt);
    } else if (_time != null) {
      return LocalDateTime.of(_date, _time);
    }
    return _date;
  }

  /**
   * Checks if the date-time is complete.
   * 
   * @return true if complete
   */
  public boolean isComplete() {
    return _zone != null;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this date-time equals the specified date-time.
   * <p>
   * To be equal, the date, time and zone must be equal.
   * 
   * @param obj  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FlexiDateTime) {
      final FlexiDateTime other = (FlexiDateTime) obj;
      return _date.equals(other._date) && ObjectUtils.equals(_time, other._time) && ObjectUtils.equals(_zone, other._zone);
    }
    return false;
  }

  /**
   * A suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _date.hashCode() ^ ObjectUtils.hashCode(_time) ^ ObjectUtils.hashCode(_zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string describing the state of this date-time.
   * <p>
   * This is the {@code toString()} of the result of {@link #toBest()}.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return toBest().toString();
  }

}

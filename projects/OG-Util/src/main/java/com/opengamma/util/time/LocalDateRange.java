/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.ArgumentChecker;

/**
 * A range of dates.
 * <p>
 * This holds a range of dates.
 */
public final class LocalDateRange implements Serializable {

  /**
   * A range over the whole time-line.
   */
  public static final LocalDateRange ALL = LocalDateRange.of(LocalDate.MIN, LocalDate.MAX, true);

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The start date.
   */
  private final LocalDate _startDate;
  /**
   * The end date.
   */
  private final LocalDate _endDateInclusive;

  /**
   * Creates an instance.
   * 
   * @param startDateInclusive  the start date, MIN_DATE treated as unbounded, not null
   * @param endDate  the end date, MAX_DATE treated as unbounded, not null
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the range, not null
   */
  public static LocalDateRange of(LocalDate startDateInclusive, LocalDate endDate, boolean endDateInclusive) {
    ArgumentChecker.notNull(startDateInclusive, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    if (endDateInclusive == false && endDate.isBefore(LocalDate.MAX)) {
      endDate = endDate.minusDays(1);
    }
    if (endDate.isBefore(startDateInclusive)) {
      throw new IllegalArgumentException("Start date must be on or after end date");
    }
    return new LocalDateRange(startDateInclusive, endDate);
  }

  /**
   * Creates an instance treating nulls as unbounded.
   * <p>
   * The null value is stored as {@code MIN_DATE} or {@code MAX_DATE} internally,
   * thus there is no special behavior for unbounded.
   * 
   * @param startDateInclusive  the start date, null means unbounded MIN_DATE
   * @param endDate  the end date, null means unbounded MAX_DATE
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the range, not null
   */
  public static LocalDateRange ofNullUnbounded(LocalDate startDateInclusive, LocalDate endDate, boolean endDateInclusive) {
    startDateInclusive = (startDateInclusive != null ? startDateInclusive : LocalDate.MIN);
    endDate = (endDate != null ? endDate : LocalDate.MAX);
    return of(startDateInclusive, endDate, endDateInclusive);
  }

  /**
   * Creates an instance.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   */
  private LocalDateRange(LocalDate startDate, LocalDate endDate) {
    _startDate = startDate;
    _endDateInclusive = endDate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the start date, inclusive.
   * 
   * @return the start date, not null
   */
  public LocalDate getStartDateInclusive() {
    return _startDate;
  }

  /**
   * Gets the end date, inclusive.
   * 
   * @return the end date, not null
   */
  public LocalDate getEndDateInclusive() {
    return _endDateInclusive;
  }

  /**
   * Gets the end date, exclusive.
   * <p>
   * If the end date (inclusive) is {@code MAX_DATE}, then {@code MAX_DATE} is returned.
   * 
   * @return the end date, not null
   */
  public LocalDate getEndDateExclusive() {
    if (isEndDateMaximum()) {
      return _endDateInclusive;
    }
    return _endDateInclusive.plusDays(1);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the start date is the minimum, typically signalling unbounded.
   * 
   * @return true if maximum
   */
  public boolean isStartDateMinimum() {
    return _startDate.equals(LocalDate.MIN);
  }

  /**
   * Checks if the end date is the maximum, typically signalling unbounded.
   * 
   * @return true if maximum
   */
  public boolean isEndDateMaximum() {
    return _endDateInclusive.equals(LocalDate.MAX);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this range with the start date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withStartDate(TemporalAdjuster adjuster) {
    return new LocalDateRange(_startDate.with(adjuster), _endDateInclusive);
  }

  /**
   * Returns a copy of this range with the end date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withEndDate(TemporalAdjuster adjuster) {
    return new LocalDateRange(_startDate, _endDateInclusive.with(adjuster));
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this range with the start date adjusted if it is unbounded.
   * 
   * @param startDateInclusive  the start date to use if currently MIN_DATE, not null
   * @return the new range, not null
   */
  public LocalDateRange resolveUnboundedStartDate(LocalDate startDateInclusive) {
    return isStartDateMinimum() ? LocalDateRange.of(startDateInclusive, _endDateInclusive, true) : this;
  }

  /**
   * Returns a copy of this range with the end date adjusted if it is unbounded.
   * 
   * @param endDate  the end date to use if currently MAX_DATE, not null
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the new range, not null
   */
  public LocalDateRange resolveUnboundedEndDate(LocalDate endDate, boolean endDateInclusive) {
    return isEndDateMaximum() ? LocalDateRange.of(_startDate, endDate, endDateInclusive) : this;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LocalDateRange) {
      LocalDateRange other = (LocalDateRange) obj;
      return _startDate.equals(other._startDate) && _endDateInclusive.equals(other._endDateInclusive);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _startDate.hashCode() ^ _endDateInclusive.hashCode();
  }

  @Override
  public String toString() {
    return "[" + _startDate + "," + _endDateInclusive + "]";
  }

}

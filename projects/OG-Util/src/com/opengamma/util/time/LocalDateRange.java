/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;

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
  public static final LocalDateRange ALL = LocalDateRange.of(LocalDate.MIN_DATE, LocalDate.MAX_DATE, true);

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The start date.
   */
  private final LocalDate _startDate;
  /**
   * The end date.
   */
  private final LocalDate _endDate;

  /**
   * Creates an instance.
   * 
   * @param startDateInclusive  the start date, not null
   * @param endDate  the end date, not null
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the range, not null
   */
  public static LocalDateRange of(LocalDate startDateInclusive, LocalDate endDate, boolean endDateInclusive) {
    ArgumentChecker.notNull(startDateInclusive, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    if (endDateInclusive == false) {
      endDate = endDate.minusDays(1);
    }
    if (endDate.isBefore(startDateInclusive)) {
      throw new IllegalArgumentException("Start date must be on or after end date");
    }
    return new LocalDateRange(startDateInclusive, endDate);
  }

  /**
   * Creates an instance.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   */
  private LocalDateRange(LocalDate startDate, LocalDate endDate) {
    _startDate = startDate;
    _endDate = endDate;
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
    return _endDate;
  }

  /**
   * Gets the end date, exclusive.
   * 
   * @return the end date, not null
   */
  public LocalDate getEndDateExclusive() {
    if (_endDate.equals(LocalDate.MAX_DATE)) {
      return _endDate;
    }
    return _endDate.plusDays(1);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this range with the start date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withStartDate(DateAdjuster adjuster) {
    return new LocalDateRange(_startDate.with(adjuster), _endDate);
  }

  /**
   * Returns a copy of this range with the end date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withEndDate(DateAdjuster adjuster) {
    return new LocalDateRange(_startDate, _endDate.with(adjuster));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LocalDateRange) {
      LocalDateRange other = (LocalDateRange) obj;
      return _startDate.equals(other._startDate) && _endDate.equals(other._endDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _startDate.hashCode() ^ _endDate.hashCode();
  }

  @Override
  public String toString() {
    return "[" + _startDate + "," + _endDate + "]";
  }

}

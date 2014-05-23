/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.LocalDate;

/**
 * Simple implementation of a calendar using sets of dates for exceptions.
 */
public abstract class ExceptionCalendar extends AbstractCalendar {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Map of exception dates and whether they are working or non-working.
   */
  private final ConcurrentMap<LocalDate, Boolean> _workingDays = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  protected ExceptionCalendar(final String name) {
    super(name);
  }

  // -------------------------------------------------------------------------
  /**
   * Map of exception dates and whether they are working or non-working.
   * @return the map, not null
   */
  protected ConcurrentMap<LocalDate, Boolean> getWorkingDays() {
    return _workingDays;
  }

  @Override
  protected boolean isWorkingDayException(final LocalDate date) {
    final Boolean workingDay = getWorkingDays().get(date);
    if (workingDay == null) {
      // no exception
      return false;
    } else if (workingDay) {
      // this shouldn't happen if things are populated sensibly
      return false;
    } else {
      // it's not a working day, so exception
      return true;
    }
  }

  @Override
  protected boolean isNonWorkingDayException(final LocalDate date) {
    final Boolean workingDay = getWorkingDays().get(date);
    if (workingDay == null) {
      // no exception
      return false;
    } else if (workingDay) {
      // it is a working day, so exception
      return true;
    } else {
      // this shouldn't happen if things are populated sensibly
      return false;
    }
  }

  /**
   * Mark a day as an exception - it is a working day.
   * @param date  the working date to add, not null
   */
  protected void addWorkingDay(final LocalDate date) {
    getWorkingDays().put(date, true);
  }

  /**
   * Mark a day as an exception - it is a non-working day.
   * @param date  the non-working date to add, not null
   */
  protected void addNonWorkingDay(final LocalDate date) {
    getWorkingDays().put(date, false);
  }

}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.calendar.LocalDate;

/**
 * Simple implementation of a calendar using sets of dates for exceptions.
 * 
 * @author Andrew Griffin
 */
public abstract class ExceptionCalendar extends CalendarBase {
  
  private final ConcurrentMap<LocalDate,Boolean> _workingDays = new ConcurrentHashMap<LocalDate,Boolean> ();
  
  protected ExceptionCalendar (final String name) {
    super (name);
  }
  
  protected ConcurrentMap<LocalDate,Boolean> getWorkingDays () {
    return _workingDays;
  }
  
  @Override
  protected boolean isWorkingDayException (final LocalDate date) {
    final Boolean workingDay = getWorkingDays ().get (date);
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
  protected boolean isNonWorkingDayException (final LocalDate date) {
    final Boolean workingDay = getWorkingDays ().get (date);
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
   */
  protected void addWorkingDay (final LocalDate date) {
    getWorkingDays ().put (date, true);
  }
  
  /**
   * Mark a day as an exception - it is a non-working day.
   */
  protected void addNonWorkingDay (final LocalDate date) {
    getWorkingDays ().put (date, false);
  }

}
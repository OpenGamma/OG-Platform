/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import javax.time.calendar.LocalDate;
import javax.time.calendar.field.DayOfWeek;

/**
 * Utility class to simplify the Calendar interface for a common pattern of normal+exception data.
 * 
 * @author Andrew Griffin
 */
public abstract class CalendarBase implements Calendar {
  
  private final String _name;
  
  protected CalendarBase (final String name) {
    _name = name;
  }
  
  /**
   * Returns true if the date would be a working day if no exceptions apply. False if normally a non-working day
   * if no exceptions apply.
   */ 
  protected abstract boolean isNormallyWorkingDay (final LocalDate date);
  
  /**
   * Returns true if the date is a non-working day, but would be considered a working day by the isNormallyWorkingDay method.
   */ 
  protected boolean isWorkingDayException (final LocalDate date) {
    return false;
  }
  
  /**
   * Returns true if the date is a working day, but would be considered a non-working day by the isNormallyWorkingDay method.
   */
  protected boolean isNonWorkingDayException (final LocalDate date) {
    return false;
  }
  
  /**
   * Invokes isNormallyWorkingDay and then either isWorkingDayException or isNonWorkingDayException to identify special cases.
   */
  @Override
  public final boolean isWorkingDay (final LocalDate date) {
    if (isNormallyWorkingDay (date)) {
      return !isWorkingDayException (date);
    } else {
      return isNonWorkingDayException (date);
    }
  }
  
  @Override
  public String getConventionName () {
    return _name;
  }
  
}
/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import javax.time.calendar.LocalDate;

/**
 * Abstraction of a calendar interface for tracking working/non-working days (e.g. Bank Holidays)
 * to be used in conjunction with DayCount and BusinessDayConvention to calculate actual settlement
 * dates and other stuff.
 * 
 * @author Andrew Griffin
 */
public interface Calendar {
  
  /**
   * Returns true if the date is a working day under this calendar. False if it is a non-working day.
   */
  public boolean isWorkingDay (LocalDate date);
  
  /**
   * Returns a name/identifier for the calendar as recognised by CalendarFactory
   */
  public String getConventionName ();
  
}
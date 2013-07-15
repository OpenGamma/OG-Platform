/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import org.threeten.bp.LocalDate;

/**
 * Convention for working day calendars.
 * <p>
 * Abstraction of a calendar interface for tracking working/non-working days, such as Bank Holidays.
 * This is used in conjunction with DayCount and BusinessDayConvention to calculate settlement dates.
 */
public interface Calendar {

  /**
   * Checks if the specified date is a working date.
   * @param date  the date to check, not null
   * @return true if working date, false if non-working
   */
  boolean isWorkingDay(LocalDate date);

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  String getConventionName();

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.NamedInstance;

/**
 * Convention for working day calendars.
 * <p>
 * Abstraction of a calendar interface for tracking working/non-working days, such as Bank Holidays.
 * This is used in conjunction with DayCount and BusinessDayConvention to calculate settlement dates.
 */
@FromStringFactory(factory = CalendarFactory.class)
public interface Calendar extends NamedInstance {

  /**
   * Checks if the specified date is a working date.
   * 
   * @param date  the date to check, not null
   * @return true if working date, false if non-working
   */
  boolean isWorkingDay(LocalDate date);

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   * @deprecated use getName()
   */
  @Deprecated
  String getConventionName();

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  @ToString
  String getName();

}

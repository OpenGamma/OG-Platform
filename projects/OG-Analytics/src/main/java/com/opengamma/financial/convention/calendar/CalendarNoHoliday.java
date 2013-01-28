/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import org.threeten.bp.LocalDate;

/**
 * A calendar with no holiday (all days are working days).
 */
public class CalendarNoHoliday extends ExceptionCalendar {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   * @param name The calendar name. Not null.
   */
  public CalendarNoHoliday(String name) {
    super(name);
  }

  @Override
  protected boolean isNormallyWorkingDay(LocalDate date) {
    return true;
  }

}

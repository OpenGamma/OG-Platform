/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import javax.time.calendar.LocalDate;
import javax.time.calendar.DayOfWeek;

/**
 * A working day calendar based on a Monday to Friday working week.
 * <p>
 * Bank Holidays can be loaded into an instance using XMLCalendarLoader.
 */
public class MondayToFridayCalendar extends ExceptionCalendar {

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  public MondayToFridayCalendar(String name) {
    super(name);
  }

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   * @param xmlDataURI  the URI to find the holidays, not null
   */
  public MondayToFridayCalendar(String name, String xmlDataURI) {
    this(name);
    new XMLCalendarLoader(xmlDataURI).populateCalendar(this);
  }

  //-------------------------------------------------------------------------
  @Override
  protected boolean isNormallyWorkingDay(LocalDate date) {
    final DayOfWeek day = date.getDayOfWeek();
    if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
      return false;
    }
    return true;
  }

}

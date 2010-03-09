/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import javax.time.calendar.LocalDate;
import javax.time.calendar.DayOfWeek;

/**
 * Implementation of a M-F working week. Bank Holidays can be loaded into an instance
 * using XMLCalendarLoader.
 * 
 * @author Andrew Griffin
 */
public class MondayToFridayCalendar extends ExceptionCalendar {

  public MondayToFridayCalendar(String name) {
    super(name);
  }
  
  public MondayToFridayCalendar(String name, String xmlDataURI) {
    this (name);
    new XMLCalendarLoader (xmlDataURI).populateCalendar (this);
  }
  
  @Override
  protected boolean isNormallyWorkingDay(LocalDate date) {
    final DayOfWeek day = date.getDayOfWeek();
    if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
      return false;
    }
    return true;
  }
  
}
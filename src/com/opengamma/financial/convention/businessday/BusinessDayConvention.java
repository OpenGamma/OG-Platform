/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.field.DayOfWeek;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class BusinessDayConvention implements DateAdjuster {

  // TODO this doesn't have any concept of Holidays in here yet.
  // TODO the holiday also needs to tell us which days of the week are weekend -
  // for example, Israel or Middle Eastern countries do not have Saturday and
  // Sunday
  protected boolean isWeekendOrHoliday(final LocalDate date) {
    final DayOfWeek day = DayOfWeek.dayOfWeek(date);
    if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
      return false;
    }
    return true;
  }

  public ZonedDateTime adjustDate(final ZonedDateTime date) {
    return ZonedDateTime.dateTime(adjustDate(date.toLocalDate()), date.toLocalTime(), date.getZone());
  }

  public boolean isWeekendOrHoliday(final ZonedDateTime date) {
    return isWeekendOrHoliday(date.toLocalDate());
  }
  
  public abstract String getConventionName ();
  
}

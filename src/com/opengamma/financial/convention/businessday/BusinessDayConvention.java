/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class BusinessDayConvention {

  public abstract LocalDate adjustDate (Calendar workingDayCalendar, LocalDate date);

  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime date) {
    return ZonedDateTime.from(adjustDate(workingDayCalendar, date.toLocalDate()), date.toLocalTime(), date.getZone());
  }

  public abstract String getConventionName ();
  
  public DateAdjuster getDateAdjuster (final Calendar workingDayCalendar) {
    return new BusinessDayConventionWithCalendar (this, workingDayCalendar);
  }
  
}

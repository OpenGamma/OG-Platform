/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public class NoHolidayCalendar implements Calendar {

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    return true;
  }

  @Override
  public String getConventionName() {
    return "No holidays";
  }

}

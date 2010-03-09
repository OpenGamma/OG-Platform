/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Adjusts a date to the following business day.
 * 
 * @author emcleod
 */

public class FollowingBusinessDayConvention extends BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDays, LocalDate date) {
    while (!workingDays.isWorkingDay (date)) {
      date = date.plusDays (1);
    }
    return date;
  }
  
  public String getConventionName () {
    return "Following";
  }

}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Adjusts a date to the preceding business day.
 * 
 * @author emcleod
 */

public class PrecedingBusinessDayConvention extends BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDays, LocalDate date) {
    while (!workingDays.isWorkingDay (date)) {
      date = date.minusDays (1);
    }
    return date;
  }
  
  @Override
  public String getConventionName () {
    return "Preceding";
  }
  
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.core.convention.BusinessDayConvention;
import com.opengamma.core.convention.Calendar;

/**
 * The following business day convention.
 * <p>
 * This chooses the next working day following a non-working day.
 */
public class FollowingBusinessDayConvention extends BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDays, LocalDate date) {
    while (!workingDays.isWorkingDay(date)) {
      date = date.plusDays(1);
    }
    return date;
  }

  @Override
  public String getConventionName() {
    return "Following";
  }

}

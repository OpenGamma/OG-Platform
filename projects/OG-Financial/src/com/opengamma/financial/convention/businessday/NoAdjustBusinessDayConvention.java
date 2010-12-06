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
 * 
 */
public class NoAdjustBusinessDayConvention extends BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDayCalendar, final LocalDate date) {
    return date;
  }

  @Override
  public String getConventionName() {
    return "None";
  }

}

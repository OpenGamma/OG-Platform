/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Composes a BusinessDayConvention with a Calendar to give a DateAdjuster instance.
 */
/* package */ class BusinessDayConventionWithCalendar implements DateAdjuster {
  
  private final BusinessDayConvention _businessDayConvention;
  private final Calendar _calendar;
  
  protected BusinessDayConventionWithCalendar (final BusinessDayConvention businessDayConvention, final Calendar calendar) {
    _businessDayConvention = businessDayConvention;
    _calendar = calendar;
  }
  
  protected BusinessDayConvention getBusinessDayConvention () {
    return _businessDayConvention;
  }
  
  protected Calendar getCalendar () {
    return _calendar;
  }

  @Override
  public LocalDate adjustDate(LocalDate date) {
    return getBusinessDayConvention ().adjustDate (getCalendar (), date);
  }
  
}
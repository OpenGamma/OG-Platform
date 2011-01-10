/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The no adjustment business day convention.
 * <p>
 * This implementation always returns the input date, performing no adjustments.
 */
public class NoAdjustBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate adjustDate(final Calendar workingDayCalendar, final LocalDate date) {
    return date;
  }

  @Override
  public String getConventionName() {
    return "None";
  }

}

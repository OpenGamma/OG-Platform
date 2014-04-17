/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;

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
  public String getName() {
    return "None";
  }

}

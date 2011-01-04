/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The preceding business day convention.
 * <p>
 * This chooses the latest working day preceding a non-working day.
 */
public class PrecedingBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate adjustDate(final Calendar workingDays, final LocalDate date) {
    LocalDate result = date;
    while (!workingDays.isWorkingDay(result)) {
      result = result.minusDays(1);
    }
    return result;
  }

  @Override
  public String getConventionName() {
    return "Preceding";
  }

}

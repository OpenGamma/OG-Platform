/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;

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
  public String getName() {
    return "Preceding";
  }

}

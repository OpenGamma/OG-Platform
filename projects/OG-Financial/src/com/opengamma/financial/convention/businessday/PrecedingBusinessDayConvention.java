/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.core.convention.Calendar;

/**
 * The preceding business day convention.
 * <p>
 * This chooses the latest working day preceding a non-working day.
 */
public class PrecedingBusinessDayConvention extends AbstractBusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDays, LocalDate date) {
    while (!workingDays.isWorkingDay(date)) {
      date = date.minusDays(1);
    }
    return date;
  }

  @Override
  public String getConventionName() {
    return "Preceding";
  }

}

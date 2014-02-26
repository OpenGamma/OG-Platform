/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;

/**
 * 
 */
public final class InstrumentDefinitionUtils {

  private InstrumentDefinitionUtils() {
  }

  /**
   * @param obsStartDate The observation start date
   * @param obsEndDate The observation end date
   * @param calendar The holiday calendar
   * @param obsFreq The observation frequency
   * @return The number of expected business days between the start and end dates
   */
  public static int countExpectedGoodDays(final LocalDate obsStartDate, final LocalDate obsEndDate, final Calendar calendar, final PeriodFrequency obsFreq) {
    int nGood = 0;
    final Period period = obsFreq.getPeriod();
    LocalDate date = obsStartDate;
    while (!date.isAfter(obsEndDate)) {
      if (calendar.isWorkingDay(date)) {
        nGood++;
      }
      date = date.plus(period);
    }
    return nGood;
  }
}

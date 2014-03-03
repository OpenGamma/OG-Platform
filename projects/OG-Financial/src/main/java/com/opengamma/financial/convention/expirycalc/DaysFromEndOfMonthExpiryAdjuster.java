/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DaysFromEndOfMonthExpiryAdjuster implements ExchangeTradedInstrumentExpiryCalculator {
  /** A weekend calendar */
  private static final Calendar WEEKEND = new MondayToFridayCalendar("Weekend");
  /** The name of this adjuster */
  private static final String NAME = "DaysFromEndOfMonthExpiryAdjuster";
  /** The number of working days from last working day of the month */
  private final int _nWorkingDays;

  /**
   * @param nWorkingDays The number of working days 
   */
  public DaysFromEndOfMonthExpiryAdjuster(final int nWorkingDays) {
    ArgumentChecker.notNegative(nWorkingDays, "nWorkingDays");
    _nWorkingDays = nWorkingDays;
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than 0; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holidayCalendar");
    // is today after the adjusted date for this month
    LocalDate date = today.with(TemporalAdjusters.lastDayOfMonth());
    while (!WEEKEND.isWorkingDay(date)) {
      date = date.minusDays(1);
    }
    int i = 0;
    while (i < _nWorkingDays) {
      date = date.minusDays(1);
      if (WEEKEND.isWorkingDay(date)) {
        i++;
      }
    }
    if (today.isAfter(date)) {
      return getExpiryDate(n, today.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), WEEKEND);
    }
    date = today.plusMonths(n - 1).with(TemporalAdjusters.lastDayOfMonth());
    while (!WEEKEND.isWorkingDay(date)) {
      date = date.minusDays(1);
    }
    i = 0;
    while (i < _nWorkingDays) {
      date = date.minusDays(1);
      if (WEEKEND.isWorkingDay(date)) {
        i++;
      }
    }
    return date;
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    return today.plusMonths(n - 1);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _nWorkingDays;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DaysFromEndOfMonthExpiryAdjuster)) {
      return false;
    }
    final DaysFromEndOfMonthExpiryAdjuster other = (DaysFromEndOfMonthExpiryAdjuster) obj;
    if (_nWorkingDays != other._nWorkingDays) {
      return false;
    }
    return true;
  }

}

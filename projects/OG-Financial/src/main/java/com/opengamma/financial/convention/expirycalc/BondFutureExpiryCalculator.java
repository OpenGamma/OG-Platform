/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for bond futures.
 */
public final class BondFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "BondFutureOptionExpiryCalculator";
  /** Singleton. */
  private static final BondFutureExpiryCalculator INSTANCE = new BondFutureExpiryCalculator();
  /** Adjuster. */
  private static final TemporalAdjuster LAST_DAY_ADJUSTER = TemporalAdjusters.lastDayOfMonth();
  /** Adjuster. */
  private static final TemporalAdjuster PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER = TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY);
  /** Adjuster. */
  private static final TemporalAdjuster PREVIOUS_FRIDAY_ADJUSTER = TemporalAdjusters.previous(DayOfWeek.FRIDAY);

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static BondFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private BondFutureExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    final LocalDate lastFridayOfThisMonth = today.with(LAST_DAY_ADJUSTER).with(PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER);
    LocalDate lastDayOfMonth;
    LocalDate lastFridayOfMonth;
    if (today.isAfter(lastFridayOfThisMonth)) {
      lastDayOfMonth = today.plusMonths(3 * n).with(LAST_DAY_ADJUSTER);
      lastFridayOfMonth = lastDayOfMonth.with(PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER);
    } else {
      lastDayOfMonth = today.plusMonths(3 * (n - 1)).with(LAST_DAY_ADJUSTER);
      lastFridayOfMonth = lastDayOfMonth.with(PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER);
    }
    int nBusinessDays = 0;
    LocalDate date = lastFridayOfMonth.plusDays(1);
    while (!date.isAfter(lastDayOfMonth)) {
      if (holidayCalendar.isWorkingDay(date)) {
        nBusinessDays++;
      }
      if (nBusinessDays >= 2) {
        while (!holidayCalendar.isWorkingDay(lastFridayOfMonth)) {
          lastFridayOfMonth = lastFridayOfMonth.minusDays(1);
        }
        return lastFridayOfMonth;
      }
      date = date.plusDays(1);
    }
    LocalDate result = lastFridayOfMonth.with(PREVIOUS_FRIDAY_ADJUSTER);
    while (!holidayCalendar.isWorkingDay(result)) {
      result = result.minusDays(1);
    }
    return result;
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

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class BondFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "BondFutureOptionExpiryCalculator";
  private static final DateAdjuster LAST_DAY_ADJUSTER = DateAdjusters.lastDayOfMonth();
  private static final DateAdjuster PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER = DateAdjusters.previousOrCurrent(DayOfWeek.FRIDAY);
  private static final DateAdjuster PREVIOUS_FRIDAY_ADJUSTER = DateAdjusters.previous(DayOfWeek.FRIDAY);
  private static final BondFutureOptionExpiryCalculator INSTANCE = new BondFutureOptionExpiryCalculator();

  public static BondFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private BondFutureOptionExpiryCalculator() {
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    final LocalDate lastFridayOfThisMonth = PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER.adjustDate(LAST_DAY_ADJUSTER.adjustDate(today));
    LocalDate lastDayOfMonth;
    LocalDate lastFridayOfMonth;
    if (today.isAfter(lastFridayOfThisMonth)) {
      lastDayOfMonth = LAST_DAY_ADJUSTER.adjustDate(today.plusMonths(n));
      lastFridayOfMonth = PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER.adjustDate(lastDayOfMonth);
    } else {
      lastDayOfMonth = LAST_DAY_ADJUSTER.adjustDate(today.plusMonths(n - 1));
      lastFridayOfMonth = PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER.adjustDate(lastDayOfMonth);
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
    LocalDate result = PREVIOUS_FRIDAY_ADJUSTER.adjustDate(lastFridayOfMonth);
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

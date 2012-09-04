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
public final class IMMFutureAndFutureOptionQuarterlyExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "IMMFutureOptionQuarterlyExpiryCalculator";
  private static final DateAdjuster THIRD_WEDNESDAY_ADJUSTER = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  private static final DateAdjuster THIRD_MONDAY_ADJUSTER = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY);
  private static final DateAdjuster MONTH_ADJUSTER = HMUZAdjuster.getInstance();
  private static final int WORKING_DAYS_TO_SETTLE = 2;
  private static final IMMFutureAndFutureOptionQuarterlyExpiryCalculator INSTANCE = new IMMFutureAndFutureOptionQuarterlyExpiryCalculator();

  public static IMMFutureAndFutureOptionQuarterlyExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private IMMFutureAndFutureOptionQuarterlyExpiryCalculator() {
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    final LocalDate nextExpiryMonth = MONTH_ADJUSTER.adjustDate(today);
    LocalDate result;
    final LocalDate thirdWednesday = THIRD_WEDNESDAY_ADJUSTER.adjustDate(today);
    if (today.isAfter(adjustForSettlement(thirdWednesday, holidayCalendar))) {
      result = THIRD_WEDNESDAY_ADJUSTER.adjustDate(nextExpiryMonth.plusMonths(3 * n));
    } else {
      result = THIRD_WEDNESDAY_ADJUSTER.adjustDate(nextExpiryMonth.plusMonths(3 * (n - 1)));
    }
    return adjustForSettlement(result, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    final LocalDate nextExpiryMonth = MONTH_ADJUSTER.adjustDate(today);
    if (today.isAfter(THIRD_MONDAY_ADJUSTER.adjustDate(today))) {
      return nextExpiryMonth.plusMonths(3 * n);
    }
    return nextExpiryMonth.plusMonths(3 * (n - 1));
  }

  @Override
  public String getName() {
    return NAME;
  }

  private LocalDate adjustForSettlement(final LocalDate date, final Calendar holidayCalendar) {
    int days = 0;
    LocalDate result = date;
    while (days < WORKING_DAYS_TO_SETTLE) {
      result = result.minusDays(1);
      if (holidayCalendar.isWorkingDay(result)) {
        days++;
      }
    }
    return result;
  }
}

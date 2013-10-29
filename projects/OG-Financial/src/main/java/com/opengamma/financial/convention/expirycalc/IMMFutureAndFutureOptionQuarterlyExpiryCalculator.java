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

import com.opengamma.financial.convention.HMUZAdjuster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for IMM futures with two days spot lag.
 */
public final class IMMFutureAndFutureOptionQuarterlyExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "IMMFutureOptionQuarterlyExpiryCalculator";
  /** Singleton. */
  private static final IMMFutureAndFutureOptionQuarterlyExpiryCalculator INSTANCE = new IMMFutureAndFutureOptionQuarterlyExpiryCalculator();
  /** Adjuster. */
  private static final TemporalAdjuster THIRD_WEDNESDAY_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** Adjuster. */
  private static final TemporalAdjuster THIRD_MONDAY_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY);
  /** Adjuster. */
  private static final TemporalAdjuster MONTH_ADJUSTER = HMUZAdjuster.getInstance();
  /** Working days to settle. */
  private static final int WORKING_DAYS_TO_SETTLE = 2;

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static IMMFutureAndFutureOptionQuarterlyExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IMMFutureAndFutureOptionQuarterlyExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    final LocalDate nextExpiryMonth = today.with(MONTH_ADJUSTER);
    LocalDate result;
    if (!today.getMonth().equals(nextExpiryMonth.getMonth())) { // In a quarterly month
      result = nextExpiryMonth.plusMonths(3 * (n - 1)).with(THIRD_WEDNESDAY_ADJUSTER);
    } else {
      final LocalDate thirdWednesday = today.with(THIRD_WEDNESDAY_ADJUSTER);
      if (today.isAfter(adjustForSettlement(thirdWednesday, holidayCalendar))) {
        result = nextExpiryMonth.plusMonths(3 * n).with(THIRD_WEDNESDAY_ADJUSTER);
      } else {
        result = nextExpiryMonth.plusMonths(3 * (n - 1)).with(THIRD_WEDNESDAY_ADJUSTER);
      }
    }
    return adjustForSettlement(result, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    final LocalDate nextExpiryMonth = today.with(MONTH_ADJUSTER);
    if (today.isAfter(today.with(THIRD_MONDAY_ADJUSTER))) {
      return nextExpiryMonth.plusMonths(3 * n);
    }
    return nextExpiryMonth.plusMonths(3 * (n - 1));
  }

  private LocalDate adjustForSettlement(final LocalDate date, final Calendar holidayCalendar) { // Use ScheduleCalculator
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

  @Override
  public String getName() {
    return NAME;
  }

}

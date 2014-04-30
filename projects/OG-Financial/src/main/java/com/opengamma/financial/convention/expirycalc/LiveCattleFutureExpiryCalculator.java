/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for live cattle futures.
 */
public final class LiveCattleFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "LiveCattleFutureExpiryCalculator";
  /** Singleton. */
  private static final LiveCattleFutureExpiryCalculator INSTANCE = new LiveCattleFutureExpiryCalculator();
  /** Adjuster. */
  private static final TemporalAdjuster LAST_DAY_ADJUSTER = TemporalAdjusters.lastDayOfMonth();
  /** Months when futures expire. */
  private static final Month[] CATTLE_FUTURE_EXPIRY_MONTHS = {
    Month.FEBRUARY, Month.APRIL, Month.JUNE, Month.AUGUST, Month.OCTOBER, Month.DECEMBER
  };

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static LiveCattleFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private LiveCattleFutureExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Expiry date of Soybean Futures:
   * The last business day of the contract month.
   * See http://www.cmegroup.com/trading/agricultural/livestock/live-cattle_contractSpecs_futures.html
   * 
   * @param n  the n'th expiry date after today, greater than zero
   * @param today  the valuation date, not null
   * @param holidayCalendar  the holiday calendar, not null
   * @return the expiry date, not null
   */
  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");

    LocalDate expiryDate = getExpiryMonth(n, today);

    while (!holidayCalendar.isWorkingDay(expiryDate)) {
      expiryDate = expiryDate.minusDays(1);
    }
    return expiryDate;
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    LocalDate expiryDate = today;
    for (int m = n; m > 0; m--) {
      expiryDate = getNextExpiryMonth(expiryDate);
    }
    return expiryDate.with(LAST_DAY_ADJUSTER);
  }

  private LocalDate getNextExpiryMonth(final LocalDate dtCurrent) {
    Month mthCurrent = dtCurrent.getMonth();
    int idx = Arrays.binarySearch(CATTLE_FUTURE_EXPIRY_MONTHS, mthCurrent);
    if (Math.abs(idx) >= (CATTLE_FUTURE_EXPIRY_MONTHS.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, Month.FEBRUARY, dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(CATTLE_FUTURE_EXPIRY_MONTHS[idx + 1]);
    } else {
      return dtCurrent.with(CATTLE_FUTURE_EXPIRY_MONTHS[-idx - 1]); //check this
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

}

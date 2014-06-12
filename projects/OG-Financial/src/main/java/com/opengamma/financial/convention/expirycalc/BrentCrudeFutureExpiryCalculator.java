/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for Brent crude futures.
 */
public final class BrentCrudeFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "BrentCrudeFutureExpiryCalculator";
  /** Singleton. */
  private static final BrentCrudeFutureExpiryCalculator INSTANCE = new BrentCrudeFutureExpiryCalculator();

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static BrentCrudeFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private BrentCrudeFutureExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Expiry date of Brent crude Futures:
   * The business day preceding the 15th day of the contract month.
   * See http://www.cmegroup.com/trading/energy/crude-oil/brent-crude-oil-last-day_contractSpecs_futures.html#prodType=AME
   * Note: Logic to handle holidays in London is not handled currently.
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
    if (expiryDate.getDayOfMonth() > 15) {
      expiryDate = getNextExpiryMonth(expiryDate);
    }
    // set day to first possible - used in getExpiryDate()
    return LocalDate.of(expiryDate.getYear(), expiryDate.getMonth(), 14);
  }

  private LocalDate getNextExpiryMonth(final LocalDate dtCurrent) {
    return dtCurrent.plusMonths(1);
  }

  @Override
  public String getName() {
    return NAME;
  }

}

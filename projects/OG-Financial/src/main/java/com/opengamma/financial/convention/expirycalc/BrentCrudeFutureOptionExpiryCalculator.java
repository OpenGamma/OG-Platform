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
 * Expiry calculator for Brent crude future options.
 */
public final class BrentCrudeFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "BrentCrudeFutureOptionExpiryCalculator";
  /** Singleton. */
  private static final BrentCrudeFutureOptionExpiryCalculator INSTANCE = new BrentCrudeFutureOptionExpiryCalculator();

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static BrentCrudeFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private BrentCrudeFutureOptionExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Expiry date of Brent Crude Future Options:
   * 3 business days prior to the future expiry.
   * See http://www.cmegroup.com/trading/energy/crude-oil/brent-crude-oil-last-day_contractSpecs_options.html#prodType=AME
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

    LocalDate expiryDate = BrentCrudeFutureExpiryCalculator.getInstance().getExpiryDate(n, today, holidayCalendar);

    int nBusinessDays = 3;
    while (nBusinessDays > 0) {
      if (holidayCalendar.isWorkingDay(expiryDate)) {
        nBusinessDays--;
      }
      expiryDate = expiryDate.minusDays(1);
    }
    return expiryDate;
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    return BrentCrudeFutureExpiryCalculator.getInstance().getExpiryMonth(n, today);
  }

  @Override
  public String getName() {
    return NAME;
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for commodity futures.
 */
public final class CommodityFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "CommodityFutureExpiryCalculator";
  /** Singleton. */
  private static final CommodityFutureExpiryCalculator INSTANCE = new CommodityFutureExpiryCalculator();

  private static final Month[] COMMODITY_FUTURE_EXPIRY_MONTHS =
  {Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.SEPTEMBER, Month.NOVEMBER
  };

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static CommodityFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private CommodityFutureExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Expiry date of commodity Futures:
   * The business day preceding the 15th day of the contract month.
   * See http://www.cmegroup.com/trading/agricultural/grain-and-oilseed/soybean_contractSpecs_futures.html#prodType=AME
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
    final Month mthCurrent = dtCurrent.getMonth();
    final int idx = Arrays.binarySearch(COMMODITY_FUTURE_EXPIRY_MONTHS, mthCurrent);
    if (Math.abs(idx) >= (COMMODITY_FUTURE_EXPIRY_MONTHS.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, Month.JANUARY, dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(COMMODITY_FUTURE_EXPIRY_MONTHS[idx + 1]);
    } else {
      return dtCurrent.with(COMMODITY_FUTURE_EXPIRY_MONTHS[-idx + 1]);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

}

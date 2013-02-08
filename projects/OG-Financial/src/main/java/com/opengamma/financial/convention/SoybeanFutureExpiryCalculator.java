/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class SoybeanFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "SoybeanFutureExpiryCalculator";
  private static final SoybeanFutureExpiryCalculator INSTANCE = new SoybeanFutureExpiryCalculator();

  private static final Month[] SOYBEAN_FUTURE_EXPIRY_MONTHS =
  {Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.SEPTEMBER, Month.NOVEMBER
  };

  public static SoybeanFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private SoybeanFutureExpiryCalculator() {
  }

  /**
   * Expiry date of Soybean Futures:
   * The business day preceding the 15th day of the contract month.
   * See http://www.cmegroup.com/trading/agricultural/grain-and-oilseed/soybean_contractSpecs_futures.html#prodType=AME
   * @param n n'th expiry date after today
   * @param today valuation date
   * @param holidayCalendar holiday calendar
   * @return True expiry date of the option
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
  /**
   * Given a LocalDate representing the valuation date and
   * an integer representing the n'th expiry after that date,
   * returns a date in the expiry month
   * Used in BloombergFutureUtils.getExpiryCodeForSoybeanFutures()
   *
   */
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
    Month mthCurrent = dtCurrent.getMonth();
    int idx = Arrays.binarySearch(SOYBEAN_FUTURE_EXPIRY_MONTHS, mthCurrent);
    if (Math.abs(idx) >= (SOYBEAN_FUTURE_EXPIRY_MONTHS.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, Month.JANUARY, dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(SOYBEAN_FUTURE_EXPIRY_MONTHS[idx + 1]);
    } else {
      return dtCurrent.with(SOYBEAN_FUTURE_EXPIRY_MONTHS[-idx + 1]);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}

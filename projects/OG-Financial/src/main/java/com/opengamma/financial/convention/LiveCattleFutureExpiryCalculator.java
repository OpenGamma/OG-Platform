/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class LiveCattleFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "LiveCattleFutureExpiryCalculator";
  private static final LiveCattleFutureExpiryCalculator INSTANCE = new LiveCattleFutureExpiryCalculator();
  private static final TemporalAdjuster LAST_DAY_ADJUSTER = TemporalAdjusters.lastDayOfMonth();

  private static final Month[] CATTLE_FUTURE_EXPIRY_MONTHS =
  {Month.FEBRUARY, Month.APRIL, Month.JUNE, Month.AUGUST, Month.OCTOBER, Month.DECEMBER };

  public static LiveCattleFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private LiveCattleFutureExpiryCalculator() {
  }

  /**
   * Expiry date of Soybean Futures:
   * The last business day of the contract month.
   * See http://www.cmegroup.com/trading/agricultural/livestock/live-cattle_contractSpecs_futures.html
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
   *
   */
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

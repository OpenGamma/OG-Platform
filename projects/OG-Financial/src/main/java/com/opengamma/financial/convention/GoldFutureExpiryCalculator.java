/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Get expires for gold future contracts
 */
public final class GoldFutureExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "GoldFutureExpiryCalculator";
  private static final TemporalAdjuster LAST_DAY_ADJUSTER = TemporalAdjusters.lastDayOfMonth();
  private static final GoldFutureExpiryCalculator INSTANCE = new GoldFutureExpiryCalculator();

  public static GoldFutureExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private GoldFutureExpiryCalculator() {
  }

  /**
   * get trading months (not static as depends on current date)
   * @param now
   * @return the valid trading months
   */
  private Month[] getTradingMonths(final LocalDate now) {
    // this may need improvements as the year end approaches
    Set<Month> ret = new TreeSet<>();
    ret.add(now.getMonth()); // this month
    ret.add(now.getMonth().plus(1)); // next month
    ret.add(now.getMonth().plus(2)); // next 2 months
    //  February, April, August, and October in next 23 months
    ret.add(Month.FEBRUARY);
    ret.add(Month.APRIL);
    ret.add(Month.AUGUST);
    ret.add(Month.OCTOBER);
    // June and December falling in next 72 month period
    ret.add(Month.JUNE);
    ret.add(Month.DECEMBER);
    // assuming this gives enough valid dates so dont go round to next 12 month period
    return ret.toArray(new Month[0]);
  }

  /**
   * Expiry date of Soybean Futures:
   * The 3rd last business day of the month
   * See http://www.cmegroup.com/trading/metals/precious/gold_contract_specifications.html
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

    LocalDate expiryDate = getExpiryMonth(n, today).with(LAST_DAY_ADJUSTER);
    int nBusinessDays = 3;
    if (holidayCalendar.isWorkingDay(expiryDate)) {
      nBusinessDays--;
    }
    // go back to 3 business days
    while (nBusinessDays > 0) {
      expiryDate = expiryDate.minusDays(1);
      if (holidayCalendar.isWorkingDay(expiryDate)) {
        nBusinessDays--;
      }
    }
    return expiryDate;
  }

  /**
   * Get the month of the nth expiry
   *
   * @param n the nth future
   * @param today the date
   * @return a date in the expiry month
   */
  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    LocalDate expiryDate = today;
    Month[] validMonths = getTradingMonths(today);
    for (int m = n; m > 0; m--) {
      expiryDate = getNextExpiryMonth(validMonths, expiryDate);
    }
    return expiryDate;
  }

  private LocalDate getNextExpiryMonth(final Month[] validMonths, final LocalDate dtCurrent) {
    Month mthCurrent = dtCurrent.getMonth();
    int idx = Arrays.binarySearch(validMonths, mthCurrent);
    if (Math.abs(idx) >= (validMonths.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, validMonths[0], dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(validMonths[idx + 1]);
    } else {
      return dtCurrent.with(validMonths[-idx + 1]);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}

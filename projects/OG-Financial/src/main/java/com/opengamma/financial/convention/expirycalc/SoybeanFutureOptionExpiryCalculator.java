/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import java.util.Arrays;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for soybean future options.
 */
public final class SoybeanFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  public static final String NAME = "SoybeanFutureOptionExpiryCalculator";
  /** Singleton. */
  private static final SoybeanFutureOptionExpiryCalculator INSTANCE = new SoybeanFutureOptionExpiryCalculator();
  /** Adjuster. */
  private static final TemporalAdjuster LAST_DAY_ADJUSTER = TemporalAdjusters.lastDayOfMonth();
  /** Adjuster. */
  private static final TemporalAdjuster PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER = TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY);
  /** Adjuster. */
  private static final TemporalAdjuster PREVIOUS_FRIDAY_ADJUSTER = TemporalAdjusters.previous(DayOfWeek.FRIDAY);
  /** Months when futures expire. */
  private static final Month[] SOYBEAN_FUTURE_EXPIRY_MONTHS = {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.SEPTEMBER, Month.NOVEMBER
  };

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static SoybeanFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private SoybeanFutureOptionExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Expiry date of Soybean Future Options:
   * The last Friday which precedes by at least two business days the last business day of the month preceding the option month.
   * See http://www.cmegroup.com/trading/agricultural/grain-and-oilseed/soybean_contractSpecs_options.html#prodType=AME
   * TODO Confirm adjustment made if Friday is not a business day. We use the business day before
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

    final LocalDate expiryMonth = getExpiryMonth(n, today);
    final LocalDate actualExpiryMonth = expiryMonth.minusMonths(1);
    final LocalDate lastDayOfMonth = actualExpiryMonth.with(LAST_DAY_ADJUSTER);
    final LocalDate lastFridayOfMonth = lastDayOfMonth.with(PREVIOUS_OR_CURRENT_FRIDAY_ADJUSTER);
    LocalDate expiryDate = lastFridayOfMonth;

    int nBusinessDays = 0;
    LocalDate date = lastFridayOfMonth.plusDays(1);
    while (!date.isAfter(lastDayOfMonth)) {
      if (holidayCalendar.isWorkingDay(date)) {
        nBusinessDays++;
      }
      if (nBusinessDays >= 2) {
        while (!holidayCalendar.isWorkingDay(expiryDate)) {
          expiryDate = expiryDate.minusDays(1);
        }
        return expiryDate;
      }
      date = date.plusDays(1);
    }
    LocalDate result = expiryDate.with(PREVIOUS_FRIDAY_ADJUSTER);
    while (!holidayCalendar.isWorkingDay(result)) {
      result = result.minusDays(1);
    }
    return result;
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    // There are 3 serial options
    if (n < 4) {
      return today.plusMonths(n); //
    }
    int m = n - 3;
    LocalDate expiryDate = today.plusMonths(3);
    while (m > 0) {
      expiryDate = getNextExpiryMonth(expiryDate);
      m--;
    }
    return expiryDate;
  }

  private LocalDate getNextExpiryMonth(final LocalDate dtCurrent) {
    Month mthCurrent = dtCurrent.getMonth();
    int idx = Arrays.binarySearch(SOYBEAN_FUTURE_EXPIRY_MONTHS, mthCurrent);
    if (idx >= (SOYBEAN_FUTURE_EXPIRY_MONTHS.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, Month.JANUARY, dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(SOYBEAN_FUTURE_EXPIRY_MONTHS[idx + 1]);
    } else {
      return dtCurrent.with(SOYBEAN_FUTURE_EXPIRY_MONTHS[-1 - idx]);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

}

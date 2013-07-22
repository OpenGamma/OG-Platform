/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Arrays;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class LiveCattleFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {
  /** Name of the calculator */
  public static final String NAME = "SoybeanFutureOptionExpiryCalculator";
  private static final TemporalAdjuster FIRST_FRIDAY_ADJUSTER = TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY);
  private static final LiveCattleFutureOptionExpiryCalculator INSTANCE = new LiveCattleFutureOptionExpiryCalculator();

  // regular months
  private static final Month[] CATTLE_OPTION_EXPIRY_MONTHS =
  {Month.FEBRUARY, Month.APRIL, Month.JUNE, Month.AUGUST, Month.OCTOBER, Month.DECEMBER };

  public static LiveCattleFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  private LiveCattleFutureOptionExpiryCalculator() {
  }

  /**
   * Expiry date of Soybean Future Options:
   * The first Friday of the month.
   * See http://www.cmegroup.com/trading/agricultural/livestock/live-cattle_contractSpecs_options.html
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

    LocalDate expiry = getExpiryMonth(n, today).with(FIRST_FRIDAY_ADJUSTER);
    while (!holidayCalendar.isWorkingDay(expiry)) {
      expiry = expiry.minusDays(1);
    }
    return expiry;
  }

  @Override
  /**
   * Given a LocalDate representing the valuation date and
   * an integer representing the n'th expiry after that date,
   * returns a date in the expiry month
   * Used in BloombergFutureUtils.getExpiryCodeForSoybeanFutureOptions()
   *
   */
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero");
    ArgumentChecker.notNull(today, "today");
    // There are 1 serial options
    if (n < 2) {
      return today.plusMonths(n);
    }
    // now
    int m = n - 1;
    LocalDate expiryDate = today.plusMonths(1);
    while (m > 0) {
      expiryDate = getNextExpiryMonth(expiryDate);
      m--;
    }
    return expiryDate;
  }

  private LocalDate getNextExpiryMonth(final LocalDate dtCurrent) {
    Month mthCurrent = dtCurrent.getMonth();
    int idx = Arrays.binarySearch(CATTLE_OPTION_EXPIRY_MONTHS, mthCurrent);
    if (idx >= (CATTLE_OPTION_EXPIRY_MONTHS.length - 1)) {
      return LocalDate.of(dtCurrent.getYear() + 1, CATTLE_OPTION_EXPIRY_MONTHS[0], dtCurrent.getDayOfMonth());
    } else if (idx >= 0) {
      return dtCurrent.with(CATTLE_OPTION_EXPIRY_MONTHS[idx + 1]);
    } else {
      return dtCurrent.with(CATTLE_OPTION_EXPIRY_MONTHS[-1 - idx]);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}

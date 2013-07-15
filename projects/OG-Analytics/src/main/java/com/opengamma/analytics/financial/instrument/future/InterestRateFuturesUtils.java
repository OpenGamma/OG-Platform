/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.util.EnumSet;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities related to interest rate futures and their dates.
 */
public class InterestRateFuturesUtils {

  /**
   * Adjuster to the third Wednesday of a given month (most used reference date for interest rate futures).
   */
  private static final TemporalAdjuster THIRD_WED = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /**
   * The IMM Expiry months
   */
  private static final Set<Month> FUTURES_QUARTERS = EnumSet.of(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER);

  /**
   * Moves the date to the next IMM quarter month (at least one month in the future). The returned date is the same date in the month as the initial date.
   * @param date The reference date (as a ZonedDateTime).
   * @return The date in the next quarterly month.
   */
  public static ZonedDateTime nextQuarter(final ZonedDateTime date) {
    ZonedDateTime result = date;
    do {
      result = result.plusMonths(1);
    } while (!FUTURES_QUARTERS.contains(result.getMonth()));
    return result;
  }

  /**
   * Returns the next third Wednesday of a IMM quarterly month.
   * @param date The reference date.
   * @return The next quarterly date.
   */
  public static ZonedDateTime nextQuarterlyDate(final ZonedDateTime date) {
    if (FUTURES_QUARTERS.contains(date.getMonth()) && date.with(THIRD_WED).isAfter(date)) { // in a quarter
      return date.with(THIRD_WED);
    }
    return nextQuarter(date).with(THIRD_WED);
  }

  /**
   * Returns the n-th next third Wednesday of a IMM quarterly month.
   * @param nthFuture The number of the future. Should be >=1.
   * @param date The reference date.
   * @return The next quarterly date.
   */
  public static ZonedDateTime nextQuarterlyDate(final int nthFuture, final ZonedDateTime date) {
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    ZonedDateTime result = nextQuarterlyDate(date);
    for (int loopfut = 1; loopfut < nthFuture; loopfut++) {
      result = nextQuarterlyDate(result);
    }
    return result;
  }

}

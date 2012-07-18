/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities related to interest rate futures and their dates.
 */
public class InterestRateFuturesUtils {

  /**
   * Adjuster to the third Wednesday of a given month (most used reference date for interest rate futures).
   */
  private static final DateAdjuster THIRD_WED = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** 
   * The IMM Expiry months  
   */
  private static final Set<MonthOfYear> FUTURES_QUARTERS = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);

  /**
   * Moves the date to the next IMM quarter month (at least one month in the future). The returned date is the same date in the month as the initial date.
   * @param date The reference date (as a ZonedDateTime).
   * @return The date in the next quarterly month.
   */
  public static ZonedDateTime nextQuarter(ZonedDateTime date) {
    ZonedDateTime result = date;
    do {
      result = result.plusMonths(1);
    } while (!FUTURES_QUARTERS.contains(result.getMonthOfYear()));
    return result;
  }

  /**
   * Returns the next third Wednesday of a IMM quarterly month.
   * @param date The reference date.
   * @return The next quarterly date.
   */
  public static ZonedDateTime nextQuarterlyDate(ZonedDateTime date) {
    if (FUTURES_QUARTERS.contains(date.getMonthOfYear()) && date.with(THIRD_WED).isAfter(date)) { // in a quarter
      return date.with(THIRD_WED);
    } else {
      return nextQuarter(date).with(THIRD_WED);
    }
  }

  /**
   * Returns the n-th next third Wednesday of a IMM quarterly month.
   * @param nthFuture The number of the future. Should be >=1.
   * @param date The reference date.
   * @return The next quarterly date.
   */
  public static ZonedDateTime nextQuarterlyDate(final int nthFuture, ZonedDateTime date) {
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    ZonedDateTime result = nextQuarterlyDate(date);
    for (int loopfut = 1; loopfut < nthFuture; loopfut++) {
      result = nextQuarterlyDate(result);
    }
    return result;
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;

/**
 *  Utility Class for computing Expiries of Future Options from ordinals (i.e. nth future after valuationDate)
 *  For IR Options use: DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY), new NextExpiryAdjuster()
 *  For Equity Options use: new SaturdayAfterThirdFridayAdjuster(), new NextEquityExpiryAdjuster()
 */
public final class FutureOptionExpiries { //TODO

  /** Instance of {@code FutureOptionExpiries} used for Interest Rate Future Options. (Expiries on 3rd Wednesdays) */
  public static final FutureOptionExpiries IR = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.WEDNESDAY));
  /** Instance of {@code FutureOptionExpiries} used for Equity Future Options. (Expiries on Saturdays after 3rd Fridays) */
  public static final FutureOptionExpiries EQUITY = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1));

  /** The adjuster moves forward to the next IMM month, to the specified day in month*/
  private final NextExpiryAdjuster _nextExpiryAdjuster;
  /** _nextExpiryAdjuster.getDayOfMonthAdjuster() moves date to day within month. eg 3rd Wednesday */

  /**
   * Utility Class for computing Expiries of Future Options from ordinals (i.e. nth future after valuationDate)
   * @param nextExpiryAdjuster Examples: NextExpiryAdjuster, NextExpiryAdjuster
   */
  private FutureOptionExpiries(final NextExpiryAdjuster nextExpiryAdjuster) {
    Validate.notNull(nextExpiryAdjuster, "nextExpiryAdjuster was null. Example: NextExpiryAdjuster");
    _nextExpiryAdjuster = nextExpiryAdjuster;
  }

  /**
   * Create instance of {@code FutureOptionExpiries},
   * a utility Class for computing Expiries of Future Options from ordinals (i.e. nth option after valuationDate)
   * @param nextExpiryAdjuster Examples: NextExpiryAdjuster, NextExpiryAdjuster
   * @return the FutureOptionExpiries class, never null
   */
  public static FutureOptionExpiries of(final NextExpiryAdjuster nextExpiryAdjuster) {
    Validate.notNull(nextExpiryAdjuster, "nextExpiryAdjuster was null. Example: NextExpiryAdjuster");
    return new FutureOptionExpiries(nextExpiryAdjuster);
  }

  /**
   * Create instance of {@code FutureOptionExpiries},
   * a utility Class for computing Expiries of Future Options from ordinals (i.e. nth option after valuationDate). <p>
   * Specify the day and week in the month, plus an offset in number of days. <p>
   *  e.g. (3,DayOfWeek.FRIDAY,1) is the Saturday after the 3rd Friday in the month. This is different from the 3rd Saturday.
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   * @param offset Integer offset, positive or negative from the result of week,day.
   * @return New instance of FutureOptionExpiries
   */
  public static FutureOptionExpiries of(final int week, final DayOfWeek day, final int offset) {
    final NextExpiryAdjuster nextExpiryAdjuster = new NextExpiryAdjuster(week, day, offset);
    return new FutureOptionExpiries(nextExpiryAdjuster);
  }

  /**
   * Compute time between now and future or future option's settlement date,
   * typically two business days before the third wednesday of the expiry month.
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public Double getFutureOptionTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getFutureOptionExpiry(n, today);
    final LocalDate previousMonday = expiry;
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }

  /**
   * Compute time between now and future or future option's settlement date,
   * typically two business days before the third wednesday of the expiry month.
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public Double getFutureTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getQuarterlyExpiry(n, today);
    final LocalDate previousMonday = expiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }

  public LocalDate getFutureOptionExpiry(final int nthFuture, final LocalDate valDate) {
    Validate.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    if (nthFuture <= 6) { // We look for expiries in the first 6 serial months after curveDate
      return getMonthlyExpiry(nthFuture, valDate);
    }
    // And for Quarterly expiries thereafter
    final int nthExpiryAfterSixMonths = nthFuture - 6;
    final LocalDate sixMonthsForward = getMonthlyExpiry(6, valDate);
    return getQuarterlyExpiry(nthExpiryAfterSixMonths, sixMonthsForward);
  }

  public LocalDate getMonthlyExpiry(final int nthMonth, final LocalDate valDate) {
    Validate.isTrue(nthMonth > 0, "nthFuture must be greater than 0.");
    LocalDate expiry = valDate.with(_nextExpiryAdjuster.getDayOfMonthAdjuster()); // Compute the expiry of valuationDate's month
    if (!expiry.isAfter(valDate)) { // If it is not strictly after valuationDate...
      expiry = (valDate.plusMonths(1)).with(_nextExpiryAdjuster.getDayOfMonthAdjuster());  // nextExpiry is third Wednesday of next month
    }
    if (nthMonth > 1) {
      expiry = (expiry.plusMonths(nthMonth - 1)).with(_nextExpiryAdjuster.getDayOfMonthAdjuster());
    }
    return expiry;
  }


  public LocalDate getQuarterlyExpiry(final int nthFuture, final LocalDate valDate) {
    Validate.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    LocalDate expiry = valDate.with(_nextExpiryAdjuster);
    for (int i = 1; i < nthFuture; i++) {
      expiry = (expiry.plusDays(7)).with(_nextExpiryAdjuster);
    }
    return expiry;
  }
}



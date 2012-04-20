/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;

/**
 * Utility Class for computing Expiries of IR Future Options from ordinals (i.e. nth future after valuationDate)
 */
public class IRFutureOptionUtils {
  private static final DateAdjuster NEXT_EXPIRY_ADJUSTER = new NextExpiryAdjuster();
  private static final DateAdjuster THIRD_WED_ADJUSTER = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);

  /**
   * Compute time between now and future or future option's settlement date, 
   * typically two business days before the third wednesday of the expiry month. 
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public static Double getFutureOptionTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getFutureOptionExpiry(n, today);
    final LocalDate previousMonday = expiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }
 
  /**
   * Compute time between now and future or future option's settlement date, 
   * typically two business days before the third wednesday of the expiry month. 
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public static Double getFutureTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getQuarterlyExpiry(n, today);
    final LocalDate previousMonday = expiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }
  public static LocalDate getFutureOptionExpiry(final int nthFuture, final LocalDate valDate) {
    Validate.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    if (nthFuture <= 6) { // We look for expiries in the first 6 serial months after curveDate
      return getMonthlyExpiry(nthFuture, valDate);
    } else {  // And for Quarterly expiries thereafter
      final int nthExpiryAfterSixMonths = nthFuture - 6;
      final LocalDate sixMonthsForward = valDate.plusMonths(6);
      return getQuarterlyExpiry(nthExpiryAfterSixMonths, sixMonthsForward);
    }
  }
 
  public static LocalDate getMonthlyExpiry(final int nthMonth, final LocalDate valDate) {
    Validate.isTrue(nthMonth > 0, "nthFuture must be greater than 0.");
    LocalDate expiry = valDate.with(THIRD_WED_ADJUSTER); // Compute the 3rd Wednesday of valuationDate's month
    if (!expiry.isAfter(valDate)) { // If it is not strictly after valuationDate...
      expiry = (valDate.plusMonths(1)).with(THIRD_WED_ADJUSTER);  // nextExpiry is third Wednesday of next month
    }
    if (nthMonth > 1) { 
      expiry = (expiry.plusMonths(nthMonth - 1)).with(THIRD_WED_ADJUSTER);
    }
    return expiry;
  }
   
   
  public static LocalDate getQuarterlyExpiry(final int nthFuture, final LocalDate valDate) {
    Validate.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    LocalDate expiry = valDate.with(NEXT_EXPIRY_ADJUSTER);
    for (int i = 1; i < nthFuture; i++) {
      expiry = (expiry.plusDays(7)).with(NEXT_EXPIRY_ADJUSTER);
    }
    return expiry;
  }
}

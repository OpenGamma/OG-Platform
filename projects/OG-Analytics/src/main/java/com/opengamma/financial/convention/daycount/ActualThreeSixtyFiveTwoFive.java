/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.JulianFields;

/**
 * The 'Actual/365.25' day count.
 */
public class ActualThreeSixtyFiveTwoFive extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long firstJulianDate = firstDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = secondDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    return (secondJulianDate - firstJulianDate) / 365.25;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }
  
  @Override
  public String getName() {
    return "Actual/365.25";
  }

}

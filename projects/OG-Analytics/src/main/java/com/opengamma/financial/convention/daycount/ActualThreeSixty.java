/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;

/**
 * The 'Actual/360' day count.
 */
public class ActualThreeSixty extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long firstJulianDate = firstDate.toModifiedJulianDays();
    final long secondJulianDate = secondDate.toModifiedJulianDays();
    return (secondJulianDate - firstJulianDate) / 360.;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public String getConventionName() {
    return "Actual/360";
  }

}

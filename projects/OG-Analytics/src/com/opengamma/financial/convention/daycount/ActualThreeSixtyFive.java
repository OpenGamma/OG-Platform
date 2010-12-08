/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * The 'Actual/365' day count.
 */
public class ActualThreeSixtyFive extends ActualTypeDayCount {

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDateTime, final ZonedDateTime secondDateTime) {
    testDates(firstDateTime, secondDateTime);
    final long firstJulianDate = firstDateTime.toLocalDate().toModifiedJulianDays();
    final long secondJulianDate = secondDateTime.toLocalDate().toModifiedJulianDays();
    return (secondJulianDate - firstJulianDate) / 365.;
  }

  @Override
  public double getAccruedInterest(
      final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate,
      final double coupon, final int paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public String getConventionName() {
    return "Actual/365";
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class ActualThreeSixtyFive extends ActualTypeDayCount {

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    testDates(firstDate, secondDate);
    final long firstJulianDate = firstDate.toLocalDate().toModifiedJulianDays();
    final long secondJulianDate = secondDate.toLocalDate().toModifiedJulianDays();
    return (secondJulianDate - firstJulianDate) / 365.;
  }

  @Override
  public String getConventionName() {
    return "Actual/365";
  }

}

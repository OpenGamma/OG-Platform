/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

/**
 * The '30E/360 ISDA' day count.
 */
public class ThirtyEThreeSixtyISDA extends ThirtyThreeSixtyTypeDayCount {

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new NotImplementedException("Need to know whether the second date is the maturity");
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    throw new NotImplementedException("Need to know whether the second date is the maturity");
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final double coupon, final boolean isMaturity) {
    return coupon * getDayCountFraction(previousCouponDate, date, isMaturity);
  }

  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final boolean isMaturity) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthOfYear().getValue();
    final double m2 = secondDate.getMonthOfYear().getValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (d1 == firstDate.getMonthOfYear().getLastDayOfMonth(firstDate.toLocalDate().isLeapYear())) {
      d1 = 30;
    }
    if (!isMaturity) {
      if (d2 == secondDate.getMonthOfYear().getLastDayOfMonth(secondDate.toLocalDate().isLeapYear())) {
        d2 = 30;
      }
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getConventionName() {
    return "30E/360 ISDA";
  }

}

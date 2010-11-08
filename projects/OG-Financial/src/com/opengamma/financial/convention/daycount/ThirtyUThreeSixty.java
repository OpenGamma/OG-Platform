/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ThirtyUThreeSixty extends ThirtyThreeSixtyTypeDayCount {

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear,
      final boolean isEOMConvention) {
    return coupon * getDayCountFraction(previousCouponDate, date, isEOMConvention);
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return getDayCountFraction(firstDate, secondDate, false);
  }

  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final boolean isEOMConvention) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthOfYear().getValue();
    final double m2 = secondDate.getMonthOfYear().getValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (isEOMConvention && m1 == 2 && isLastDayOfFebruary(firstDate) && isLastDayOfFebruary(secondDate)) {
      d2 = 30;
      d1 = 30;
    } else {
      if (d1 > 29 && d2 == 31) {
        d2 = 30;
      }
      if (d1 == 31) {
        d1 = 30;
      }
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getConventionName() {
    return "30U/360";
  }

  private boolean isLastDayOfFebruary(final ZonedDateTime date) {
    return DateUtil.isLeapYear(date) ? date.getDayOfMonth() == 29 : date.getDayOfMonth() == 28;
  }
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;

/**
 * 
 */
public class TwentyEightThreeSixty extends StatelessDayCount {
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthValue();
    final double m2 = secondDate.getMonthValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (d1 > 28) {
      d1 = 28;
    }
    if (d2 > 28) {
      d2 = 28;
    }
    return (360 * (y2 - y1) + 28 * (m2 - m1) + (d2 - d1)) / 360;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  @Override
  public String getConventionName() {
    return "28/360";
  }

}

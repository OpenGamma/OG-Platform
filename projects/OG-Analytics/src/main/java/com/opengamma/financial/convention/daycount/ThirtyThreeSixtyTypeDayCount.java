/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;

/**
 * Base class for '30/360' style day counts.
 */
public abstract class ThirtyThreeSixtyTypeDayCount extends StatelessDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  /**
   * Calculates the year fraction assuming 30 days per month and 360 days per year.
   * @param d1 The first day of the month
   * @param d2 The second day of the month
   * @param m1 The first month of the year (where January is 1)
   * @param m2 The second month of the year (where January is 1)
   * @param y1 The first year
   * @param y2 The second year
   * @return The year fraction
   */
  protected double getYears(final double d1, final double d2, final double m1, final double m2, final double y1, final double y2) {
    double endYear = y2;
    double deltaMonth;
    if (m1 > m2) {
      endYear -= 1;
      deltaMonth = 12 - Math.abs(m2 - m1);
    } else {
      deltaMonth = m2 - m1;
    }
    return (360 * (endYear - y1) + 30 * deltaMonth + (d2 - d1)) / 360;
  }

}

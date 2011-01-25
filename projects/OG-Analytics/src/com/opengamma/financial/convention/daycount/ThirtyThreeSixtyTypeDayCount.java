/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Base class for '30/360' style day counts.
 */
public abstract class ThirtyThreeSixtyTypeDayCount extends StatelessDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  protected double getYears(final double d1, final double d2, final double m1, final double m2, final double y1, final double y2) {
    return (360 * (y2 - y1) + 30 * (m2 - m1) + (d2 - d1)) / 360;
  }

}

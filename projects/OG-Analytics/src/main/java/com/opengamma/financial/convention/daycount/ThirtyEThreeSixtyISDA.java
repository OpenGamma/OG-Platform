/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

/**
 * The '30E/360 ISDA' day count.
 */
public class ThirtyEThreeSixtyISDA extends ThirtyThreeSixtyTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new NotImplementedException("Need to know whether the second date is the maturity");
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    throw new NotImplementedException("Need to know whether the second date is the maturity");
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final double coupon, final boolean isMaturity) {
    return coupon * getDayCountFraction(previousCouponDate.toLocalDate(), date.toLocalDate(), isMaturity);
  }

  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final double coupon, final boolean isMaturity) {
    return coupon * getDayCountFraction(previousCouponDate, date, isMaturity);
  }

  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final boolean isMaturity) {
    Validate.notNull(firstDate);
    Validate.notNull(secondDate);
    return getDayCountFraction(firstDate.toLocalDate(), secondDate.toLocalDate(), isMaturity);
  }

  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate, final boolean isMaturity) {
    testDates(firstDate, secondDate);
    int d1 = firstDate.getDayOfMonth();
    int d2 = secondDate.getDayOfMonth();
    final int m1 = firstDate.getMonthValue();
    final int m2 = secondDate.getMonthValue();
    final int y1 = firstDate.getYear();
    final int y2 = secondDate.getYear();
    if (d1 == firstDate.lengthOfMonth()) {
      d1 = 30;
    }
    if (!isMaturity) {
      if (d2 == secondDate.lengthOfMonth()) {
        d2 = 30;
      }
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getName() {
    return "30E/360 ISDA";
  }

}

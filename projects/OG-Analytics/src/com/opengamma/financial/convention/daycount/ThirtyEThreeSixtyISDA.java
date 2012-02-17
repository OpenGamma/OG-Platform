/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

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
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthOfYear().getValue();
    final double m2 = secondDate.getMonthOfYear().getValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (d1 == firstDate.getMonthOfYear().getLastDayOfMonth(firstDate.isLeapYear())) {
      d1 = 30;
    }
    if (!isMaturity) {
      if (d2 == secondDate.getMonthOfYear().getLastDayOfMonth(secondDate.isLeapYear())) {
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

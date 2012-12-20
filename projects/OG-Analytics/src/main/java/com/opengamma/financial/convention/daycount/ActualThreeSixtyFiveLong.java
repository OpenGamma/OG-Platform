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
 * The 'Actual/365L' day count.
 */
public class ActualThreeSixtyFiveLong extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new NotImplementedException("Need information on payment frequency to get day count");
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    Validate.notNull(previousCouponDate);
    Validate.notNull(date);
    Validate.notNull(nextCouponDate);
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    testDates(previousCouponDate, date, nextCouponDate);
    double daysPerYear;
    if (paymentsPerYear == 1) {
      if (nextCouponDate.isLeapYear()) {
        final LocalDate feb29 = LocalDate.of(nextCouponDate.getYear(), 2, 29);
        if (!nextCouponDate.isBefore(feb29) && previousCouponDate.isBefore(feb29)) {
          daysPerYear = 366;
        } else {
          daysPerYear = 365;
        }
      } else if (previousCouponDate.isLeapYear()) {
        final LocalDate feb29 = LocalDate.of(previousCouponDate.getYear(), 2, 29);
        if (!nextCouponDate.isBefore(feb29) && previousCouponDate.isBefore(feb29)) {
          daysPerYear = 366;
        } else {
          daysPerYear = 365;
        }
      } else {
        daysPerYear = 365;
      }
    } else {
      daysPerYear = nextCouponDate.isLeapYear() ? 366 : 365;
    }
    final long firstJulianDate = previousCouponDate.toModifiedJulianDays();
    final long secondJulianDate = date.toLocalDate().toModifiedJulianDays();
    return coupon * (secondJulianDate - firstJulianDate) / daysPerYear;
  }

  @Override
  public String getConventionName() {
    return "Actual/365L";
  }

}

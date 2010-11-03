/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ActualActualISDA extends ActualTypeDayCount {

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    testDates(firstDate, secondDate);
    final int y1 = firstDate.getYear();
    final int y2 = secondDate.getYear();
    if (y1 == y2) {
      final double basis = DateUtil.isLeapYear(firstDate) ? 366 : 365;
      final long firstJulian = firstDate.toLocalDate().toModifiedJulianDays();
      final long secondJulian = secondDate.toLocalDate().toModifiedJulianDays();
      return (secondJulian - firstJulian) / basis;
    }
    final long firstNewYearJulian = LocalDate.of(y1 + 1, 1, 1).toModifiedJulianDays();
    final long firstJulian = firstDate.toLocalDate().toModifiedJulianDays();
    final long secondNewYearJulian = LocalDate.of(y2, 1, 1).toModifiedJulianDays();
    final long secondJulian = secondDate.toLocalDate().toModifiedJulianDays();
    final double firstBasis = DateUtil.isLeapYear(firstDate) ? 366 : 365;
    final double secondBasis = DateUtil.isLeapYear(secondDate) ? 366 : 365;
    return (firstNewYearJulian - firstJulian) / firstBasis + (secondJulian - secondNewYearJulian) / secondBasis + (y2 - y1 - 1);
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual ISDA";
  }

}

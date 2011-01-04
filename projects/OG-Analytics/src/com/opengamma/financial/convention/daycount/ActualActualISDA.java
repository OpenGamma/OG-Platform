/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

/**
 * The 'Actual/Actual ISDA' day count.
 */
public class ActualActualISDA extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDateTime, final ZonedDateTime secondDateTime) {
    testDates(firstDateTime, secondDateTime);
    final LocalDate firstDate = firstDateTime.toLocalDate();
    final LocalDate secondDate = secondDateTime.toLocalDate();
    final int y1 = firstDate.getYear();
    final int y2 = secondDate.getYear();
    if (y1 == y2) {
      final double basis = firstDate.isLeapYear() ? 366 : 365;
      final long firstJulian = firstDate.toModifiedJulianDays();
      final long secondJulian = secondDate.toModifiedJulianDays();
      return (secondJulian - firstJulian) / basis;
    }
    final long firstNewYearJulian = LocalDate.of(y1 + 1, 1, 1).toModifiedJulianDays();
    final long firstJulian = firstDate.toModifiedJulianDays();
    final long secondNewYearJulian = LocalDate.of(y2, 1, 1).toModifiedJulianDays();
    final long secondJulian = secondDate.toModifiedJulianDays();
    final double firstBasis = firstDate.isLeapYear() ? 366 : 365;
    final double secondBasis = secondDate.isLeapYear() ? 366 : 365;
    return (firstNewYearJulian - firstJulian) / firstBasis + (secondJulian - secondNewYearJulian) / secondBasis + (y2 - y1 - 1);
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual ISDA";
  }

}

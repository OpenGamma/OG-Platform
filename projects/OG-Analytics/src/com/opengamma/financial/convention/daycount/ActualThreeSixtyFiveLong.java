/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

/**
 * The 'Actual/365L' day count.
 */
public class ActualThreeSixtyFiveLong extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new NotImplementedException("Need information on payment frequency to get day count");
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    testDates(previousCouponDate, date, nextCouponDate);
    final LocalDate previous = previousCouponDate.toLocalDate();
    final LocalDate next = nextCouponDate.toLocalDate();
    double daysPerYear;
    if (paymentsPerYear == 1) {
      if (next.isLeapYear()) {
        final LocalDate feb29 = LocalDate.of(next.getYear(), 2, 29);
        if (!next.isBefore(feb29) && previous.isBefore(feb29)) {
          daysPerYear = 366;
        } else {
          daysPerYear = 365;
        }
      } else if (previous.isLeapYear()) {
        final LocalDate feb29 = LocalDate.of(previous.getYear(), 2, 29);
        if (!next.isBefore(feb29) && previous.isBefore(feb29)) {
          daysPerYear = 366;
        } else {
          daysPerYear = 365;
        }
      } else {
        daysPerYear = 365;
      }
    } else {
      daysPerYear = next.isLeapYear() ? 366 : 365;
    }
    final long firstJulianDate = previous.toModifiedJulianDays();
    final long secondJulianDate = date.toLocalDate().toModifiedJulianDays();
    return coupon * (secondJulianDate - firstJulianDate) / daysPerYear;
  }

  @Override
  public String getConventionName() {
    return "Actual/365L";
  }

}

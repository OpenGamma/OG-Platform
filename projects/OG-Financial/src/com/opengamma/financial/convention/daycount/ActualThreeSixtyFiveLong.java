/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ActualThreeSixtyFiveLong extends ActualTypeDayCount {

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    testDates(previousCouponDate, date, nextCouponDate);
    final LocalDate previous = previousCouponDate.toLocalDate();
    final LocalDate next = nextCouponDate.toLocalDate();
    double daysPerYear;
    if (paymentsPerYear == 1) {
      if (DateUtil.isLeapYear(next)) {
        final LocalDate feb29 = LocalDate.of(next.getYear(), 2, 29);
        if (!next.isBefore(feb29) && previous.isBefore(feb29)) {
          daysPerYear = 366;
        } else {
          daysPerYear = 365;
        }
      } else if (DateUtil.isLeapYear(previous)) {
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
      daysPerYear = DateUtil.isLeapYear(next) ? 366 : 365;
    }
    final long firstJulianDate = previous.toModifiedJulianDays();
    final long secondJulianDate = date.toLocalDate().toModifiedJulianDays();
    return coupon * (secondJulianDate - firstJulianDate) / daysPerYear;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new NotImplementedException("Need information on payment frequency to get day count");
  }

  @Override
  public String getConventionName() {
    return "Actual/365L";
  }

}

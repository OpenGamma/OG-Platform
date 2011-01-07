/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.analytics.timeseries.ScheduleFactory;

/**
 * The 'Actual/Actual AFB' day count.
 */
public class ActualActualAFB extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    testDates(firstDate, secondDate);
    final LocalDate start = firstDate.toLocalDate();
    final LocalDate end = secondDate.toLocalDate();
    final long daysBetween = end.toModifiedJulianDays() - start.toModifiedJulianDays();
    final LocalDate oneYear = start.plusYears(1);
    if (end.isBefore(oneYear) || oneYear.equals(end)) {
      final double daysInYear = end.isLeapYear() && end.getMonthOfYear().getValue() > 2 ? 366 : 365;
      return daysBetween / daysInYear;
    }
    final ZonedDateTime[] schedule = ScheduleFactory.getSchedule(firstDate, secondDate, 1, true, true, false);
    ZonedDateTime d = schedule[0];
    if (d.toLocalDate().isLeapYear() && d.getMonthOfYear() == MonthOfYear.FEBRUARY && d.getDayOfMonth() == 28) {
      d = d.plusDays(1);
    }
    return schedule.length + getDayCountFraction(firstDate, d);
  }

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual AFB";
  }

}

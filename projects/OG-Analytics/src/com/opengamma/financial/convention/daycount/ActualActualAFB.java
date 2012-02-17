/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import com.opengamma.financial.schedule.ScheduleFactory;

/**
 * The 'Actual/Actual AFB' day count.
 */
public class ActualActualAFB extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long daysBetween = secondDate.toModifiedJulianDays() - firstDate.toModifiedJulianDays();
    final LocalDate oneYear = firstDate.plusYears(1);
    if (secondDate.isBefore(oneYear) || oneYear.equals(secondDate)) {
      final double daysInYear = secondDate.isLeapYear() && secondDate.getMonthOfYear().getValue() > 2 ? 366 : 365;
      return daysBetween / daysInYear;
    }
    final LocalDate[] schedule = ScheduleFactory.getSchedule(firstDate, secondDate, 1, true, true, false);
    LocalDate d = schedule[0];
    if (d.isLeapYear() && d.getMonthOfYear() == MonthOfYear.FEBRUARY && d.getDayOfMonth() == 28) {
      d = d.plusDays(1);
    }
    return schedule.length + getDayCountFraction(firstDate, d);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual AFB";
  }

}

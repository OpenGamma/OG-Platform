/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.analytics.financial.schedule.ScheduleFactory;

/**
 * The 'Actual/Actual AFB' day count.
 */
public class ActualActualAFB extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long daysBetween = firstDate.periodUntil(secondDate, DAYS);
    final LocalDate oneYear = firstDate.plusYears(1);
    if (secondDate.isBefore(oneYear) || oneYear.equals(secondDate)) {
      final double daysInYear = secondDate.isLeapYear() && secondDate.getMonthValue() > 2 ? 366 : 365;
      return daysBetween / daysInYear;
    }
    final LocalDate[] schedule = ScheduleFactory.getSchedule(firstDate, secondDate, 1, true, true, false);
    LocalDate d = schedule[0];
    if (d.isLeapYear() && d.getMonth() == Month.FEBRUARY && d.getDayOfMonth() == 28) {
      d = d.plusDays(1);
    }
    return schedule.length - 1.0 + getDayCountFraction(firstDate, d);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return coupon * getDayCountFraction(previousCouponDate, date);
  }

  @Override
  public String getName() {
    return "Actual/Actual AFB";
  }

}

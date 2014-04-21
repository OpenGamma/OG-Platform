/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.JulianFields;

/**
 * 
 */
public class ActualNL extends ActualTypeDayCount {
  private static final long serialVersionUID = 1L;

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    final long firstJulianDate = firstDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulianDate = secondDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    if (firstDate.getYear() == secondDate.getYear()) {
      if (firstDate.isLeapYear()) {
        final LocalDate leapDate = getLeapDateOfYear(firstDate.getYear());
        if (firstDate.isBefore(leapDate) && (secondDate.isAfter(leapDate) || secondDate.equals(leapDate))) {
          return (secondJulianDate - firstJulianDate - 1) / 365.;
        }
      }
      return (secondJulianDate - firstJulianDate) / 365.;
    }
    int numberOfLeapDays = 0;
    LocalDate previousDate = firstDate;
    LocalDate date = firstDate.plusYears(1);
    while (date.isBefore(secondDate) || date.equals(secondDate)) {
      if (date.isLeapYear() && date.isAfter(getLeapDateOfYear(date.getYear()))) {
        numberOfLeapDays++;
      } else if (previousDate.isLeapYear() && previousDate.isBefore(getLeapDateOfYear(previousDate.getYear()))) {
        numberOfLeapDays++;
      }
      previousDate = date;
      date = date.plusYears(1);
    }
    return (secondJulianDate - firstJulianDate - numberOfLeapDays) / 365.;
  }

  @Override
  public String getName() {
    return "Actual/NL";
  }

  private LocalDate getLeapDateOfYear(final int year) {
    return LocalDate.of(year, 2, 29);
  }
}

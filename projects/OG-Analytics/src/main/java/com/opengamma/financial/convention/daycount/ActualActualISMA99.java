/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.JulianFields;

/**
 * The 'Actual/Actual/ISMA-99' day count.
 */
public class ActualActualISMA99 extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;


  private double getBasis(LocalDate min) {
    LocalDate feb29 = null;
    if (min.isLeapYear()) {
      feb29 = LocalDate.of(min.getYear(), Month.FEBRUARY, 29);
    }
    LocalDate max = min.plusYears(1);
    if (feb29 == null && max.isLeapYear()) {
      feb29 = LocalDate.of(max.getYear(), Month.FEBRUARY, 29);
    }
    return feb29 == null || min.isAfter(feb29) || max.isBefore(feb29) ? 365 : 366;
  }

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);

    LocalDate secondDateRoundedByYear = firstDate.plusYears(secondDate.getYear() - firstDate.getYear());

    if (secondDateRoundedByYear.isAfter(secondDate)) {
      secondDateRoundedByYear = secondDateRoundedByYear.minusYears(1);
    }

    double basis = getBasis(secondDateRoundedByYear);

    int years = secondDateRoundedByYear.getYear() - firstDate.getYear();

    final long firstJulian  = years == 0 ? firstDate.getLong(JulianFields.MODIFIED_JULIAN_DAY) : secondDateRoundedByYear.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long secondJulian = secondDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);

    return years + (secondJulian - firstJulian) / basis;
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public String getName() {
    return "Actual/Actual/ISMA-99";
  }

}

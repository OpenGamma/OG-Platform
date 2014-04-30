/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The Business/252 day count. The number of good business days between two days is counted and then divided by 252.
 * @deprecated This day count is incorrectly named; it should be "Business/252"
 */
@Deprecated
public class ActualTwoFiveTwo extends StatelessDayCount {
  private static final long serialVersionUID = 1L;
  private static final DayCount DC = DayCounts.BUSINESS_252;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    return DC.getDayCountFraction(firstDate, secondDate);
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return DC.getDayCountFraction(firstDate, secondDate);
  }

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate, final Calendar calendar) {
    return DC.getDayCountFraction(firstDate, secondDate, calendar);
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    return DC.getDayCountFraction(firstDate, secondDate, calendar);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon,
      final double paymentsPerYear) {
    return DC.getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear);
  }

  @Override
  public String getName() {
    return "Actual/252";
  }

}

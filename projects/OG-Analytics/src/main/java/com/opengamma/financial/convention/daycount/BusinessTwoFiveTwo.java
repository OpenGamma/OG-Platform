/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * The Business/252 day count. The number of good business days between two days is counted and then divided by 252.
 */
public class BusinessTwoFiveTwo extends StatelessDayCount {

  private static final double TWO_FIVE_TWO = 252.0;
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new UnsupportedOperationException("Must supply a calendar to calculate the day-count fraction");
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new UnsupportedOperationException("Must supply a calendar to calculate the day-count fraction");
  }

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate, final Calendar calendar) {
    // Arguments are checked in BusinessDays
    return BusinessDays.getDaysBetween(firstDate, secondDate, calendar) / TWO_FIVE_TWO;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    ArgumentChecker.notNull(firstDate, "first date");
    ArgumentChecker.notNull(secondDate, "second date");
    return getDayCountFraction(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "Business/252";
  }

}

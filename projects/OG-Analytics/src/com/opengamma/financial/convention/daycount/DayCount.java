/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * Convention for calculating the day count.
 */
public abstract class DayCount {

  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   * 
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  public abstract double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate);

  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   * 
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    Validate.notNull(firstDate);
    Validate.notNull(secondDate);
    return getDayCountFraction(firstDate.toLocalDate(), secondDate.toLocalDate());
  }

  /**
   * Calculates the accrued interest for the coupon according to the convention.
   * 
   * @param previousCouponDate  the previous coupon date, not null
   * @param date  the evaluated coupon date, not null
   * @param nextCouponDate  the next coupon date, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the accrued interest
   */
  public abstract double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear);

  /**
   * Calculates the accrued interest for the coupon according to the convention.
   * 
   * @param previousCouponDate  the previous coupon date, not null
   * @param date  the evaluated coupon date, not null
   * @param nextCouponDate  the next coupon date, not null
   * @param coupon  the coupon value
   * @param paymentsPerYear  the number of payments per year, one, two, three, four, six or twelve
   * @return the accrued interest
   */
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear) {
    Validate.notNull(previousCouponDate);
    Validate.notNull(date);
    Validate.notNull(nextCouponDate);
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear);
  }

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  public abstract String getConventionName();

}

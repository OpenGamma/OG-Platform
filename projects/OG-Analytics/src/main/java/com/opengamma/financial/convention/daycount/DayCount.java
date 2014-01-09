/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.NamedInstance;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Interface of a Day count convention
 */
@FromStringFactory(factory = DayCountFactory.class)
public interface DayCount extends NamedInstance {

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
  double getDayCountFraction(LocalDate firstDate, LocalDate secondDate);

  /**
   * Gets the day count between the specified dates using the supplied calendar to provide business days
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   *
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @param calendar  a calendar
   * @return the day count fraction
   */
  double getDayCountFraction(LocalDate firstDate, LocalDate secondDate, Calendar calendar);

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
  double getDayCountFraction(ZonedDateTime firstDate, ZonedDateTime secondDate);

  /**
   * Gets the day count between the specified dates using the supplied calendar to provide business days
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   *
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @param calendar  a calendar
   * @return the day count fraction
   */
  double getDayCountFraction(ZonedDateTime firstDate, ZonedDateTime secondDate, Calendar calendar);

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
  double getAccruedInterest(LocalDate previousCouponDate, LocalDate date, LocalDate nextCouponDate, double coupon, double paymentsPerYear);

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
  double getAccruedInterest(ZonedDateTime previousCouponDate, ZonedDateTime date, ZonedDateTime nextCouponDate, double coupon, double paymentsPerYear);

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   * @deprecated use getName()
   */
  @Deprecated
  String getConventionName();

  /**
   * Gets the name of the convention.
   *
   * @return the name, not null
   */
  @ToString
  @Override
  String getName();

}
